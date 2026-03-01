package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Product;
import com.quantum.edu.catalogue.domain.ProductModule;
import com.quantum.edu.catalogue.dto.CreateModuleRequest;
import com.quantum.edu.catalogue.dto.ModuleResponse;
import com.quantum.edu.catalogue.repository.ProductModuleRepository;
import com.quantum.edu.catalogue.repository.ProductRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductModuleService {

    private final ProductModuleRepository moduleRepository;
    private final ProductRepository productRepository;

    public ProductModuleService(ProductModuleRepository moduleRepository,
                                ProductRepository productRepository) {
        this.moduleRepository = moduleRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ModuleResponse create(Long productId, CreateModuleRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));

        ProductModule module = ProductModule.builder()
                .product(product)
                .title(request.getTitle())
                .orderIndex(request.getOrderIndex())
                .build();

        module = moduleRepository.save(module);
        return toResponse(module);
    }

    @Transactional
    public ModuleResponse update(Long moduleId, CreateModuleRequest request) {
        ProductModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.MODULE_NOT_FOUND));

        module.setTitle(request.getTitle());
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }

        module = moduleRepository.save(module);
        return toResponse(module);
    }

    @Transactional
    public void delete(Long moduleId) {
        ProductModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.MODULE_NOT_FOUND));
        moduleRepository.delete(module);
    }

    @Transactional(readOnly = true)
    public List<ModuleResponse> listByProduct(Long productId) {
        return moduleRepository.findByProductIdOrderByOrderIndexAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<ModuleResponse> reorder(Long productId, List<Long> moduleIds) {
        List<ProductModule> modules = moduleRepository.findByProductIdOrderByOrderIndexAsc(productId);

        for (int i = 0; i < moduleIds.size(); i++) {
            Long moduleId = moduleIds.get(i);
            ProductModule module = modules.stream()
                    .filter(m -> m.getId().equals(moduleId))
                    .findFirst()
                    .orElseThrow(() -> new InternalException(InternalErrorCode.MODULE_NOT_FOUND));
            module.setOrderIndex(i);
        }

        moduleRepository.saveAll(modules);
        return moduleRepository.findByProductIdOrderByOrderIndexAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ModuleResponse toResponse(ProductModule module) {
        return ModuleResponse.builder()
                .id(module.getId())
                .productId(module.getProduct().getId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .createdAt(module.getCreatedAt())
                .build();
    }
}

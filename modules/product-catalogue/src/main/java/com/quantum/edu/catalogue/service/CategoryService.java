package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.Category;
import com.quantum.edu.catalogue.dto.CategoryResponse;
import com.quantum.edu.catalogue.dto.CreateCategoryRequest;
import com.quantum.edu.catalogue.dto.UpdateCategoryRequest;
import com.quantum.edu.catalogue.repository.CategoryRepository;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InternalException(InternalErrorCode.CATEGORY_NOT_FOUND));
        }

        String slug = SlugUtil.generate(request.getName(), categoryRepository::existsBySlug);
        Category category = new Category(request.getName(), slug, parent);
        category = categoryRepository.save(category);
        return toResponse(category, false);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.CATEGORY_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName());
            String slug = SlugUtil.generate(request.getName(), s -> {
                return categoryRepository.findBySlug(s)
                        .map(c -> !c.getId().equals(id))
                        .orElse(false);
            });
            category.setSlug(slug);
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        category = categoryRepository.save(category);
        return toResponse(category, false);
    }

    @Transactional
    public void deactivate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new InternalException(InternalErrorCode.CATEGORY_NOT_FOUND));
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream()
                .map(c -> toResponse(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByActiveTrue().stream()
                .map(c -> toResponse(c, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getTree(boolean activeOnly) {
        List<Category> roots = activeOnly
                ? categoryRepository.findByParentIsNullAndActiveTrueOrderByNameAsc()
                : categoryRepository.findByParentIsNullOrderByNameAsc();

        Set<Long> activeIds = activeOnly
                ? new HashSet<>(categoryRepository.findByActiveTrue().stream().map(Category::getId).toList())
                : null;

        return roots.stream()
                .map(root -> toTreeResponse(root, activeIds))
                .toList();
    }

    private CategoryResponse toTreeResponse(Category category, Set<Long> activeIds) {
        List<CategoryResponse> children = category.getChildren().stream()
                .filter(c -> activeIds == null || activeIds.contains(c.getId()))
                .map(c -> toTreeResponse(c, activeIds))
                .toList();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(category.getLevel())
                .active(category.isActive())
                .children(children.isEmpty() ? null : children)
                .build();
    }

    public static CategoryResponse toResponse(Category category, boolean includeChildren) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(category.getLevel())
                .active(category.isActive())
                .build();
    }
}

package com.quantum.edu.catalogue.service;

import com.quantum.edu.catalogue.domain.*;
import com.quantum.edu.catalogue.dto.*;
import com.quantum.edu.catalogue.repository.*;
import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductContentService {

    private final ProductRepository productRepository;
    private final ProductContentRepository contentRepository;
    private final LessonRepository lessonRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;

    public ProductContentService(ProductRepository productRepository,
                                 ProductContentRepository contentRepository,
                                 LessonRepository lessonRepository,
                                 AssessmentRepository assessmentRepository,
                                 AssessmentQuestionRepository questionRepository) {
        this.productRepository = productRepository;
        this.contentRepository = contentRepository;
        this.lessonRepository = lessonRepository;
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public ContentResponse addContent(Long productId, CreateContentRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));

        if (contentRepository.existsByProductIdAndOrderIndex(productId, request.getOrderIndex())) {
            throw new InternalException(InternalErrorCode.DUPLICATE_ORDER_INDEX);
        }

        ProductContent content = new ProductContent(product, request.getContentType(),
                request.getTitle(), request.getOrderIndex());
        if (request.getMandatory() != null) {
            content.setMandatory(request.getMandatory());
        }
        content = contentRepository.save(content);

        if (request.getContentType() == ProductContent.ContentType.LESSON) {
            Lesson lesson = new Lesson(content,
                    request.getLessonType() != null ? request.getLessonType() : LessonType.VIDEO);
            lesson.setVideoUrl(request.getVideoUrl());
            lesson.setPdfUrl(request.getPdfUrl());
            lesson.setDurationSeconds(request.getDurationSeconds());
            lessonRepository.save(lesson);
        } else if (request.getContentType() == ProductContent.ContentType.ASSESSMENT) {
            int passPercent = request.getPassPercentage() != null ? request.getPassPercentage() : 70;
            assessmentRepository.save(new Assessment(content, passPercent));
        }

        return toContentResponse(content);
    }

    @Transactional
    public ContentResponse updateContent(Long productId, Long contentId, UpdateContentRequest request) {
        ProductContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.CONTENT_NOT_FOUND));
        if (!content.getProduct().getId().equals(productId)) {
            throw new InternalException(InternalErrorCode.CONTENT_NOT_FOUND);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) content.setTitle(request.getTitle());
        if (request.getMandatory() != null) content.setMandatory(request.getMandatory());
        contentRepository.save(content);

        if (content.getContentType() == ProductContent.ContentType.LESSON) {
            lessonRepository.findByProductContentId(contentId).ifPresent(lesson -> {
                if (request.getLessonType() != null) lesson.setLessonType(request.getLessonType());
                if (request.getVideoUrl() != null) lesson.setVideoUrl(request.getVideoUrl());
                if (request.getPdfUrl() != null) lesson.setPdfUrl(request.getPdfUrl());
                if (request.getDurationSeconds() != null) lesson.setDurationSeconds(request.getDurationSeconds());
                lessonRepository.save(lesson);
            });
        } else if (content.getContentType() == ProductContent.ContentType.ASSESSMENT) {
            assessmentRepository.findByProductContentId(contentId).ifPresent(assessment -> {
                if (request.getPassPercentage() != null) assessment.setPassPercentage(request.getPassPercentage());
                assessmentRepository.save(assessment);
            });
        }

        return toContentResponse(content);
    }

    @Transactional
    public void deleteContent(Long productId, Long contentId) {
        ProductContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.CONTENT_NOT_FOUND));
        if (!content.getProduct().getId().equals(productId)) {
            throw new InternalException(InternalErrorCode.CONTENT_NOT_FOUND);
        }

        if (content.getContentType() == ProductContent.ContentType.LESSON) {
            lessonRepository.findByProductContentId(contentId).ifPresent(lessonRepository::delete);
        } else if (content.getContentType() == ProductContent.ContentType.ASSESSMENT) {
            assessmentRepository.findByProductContentId(contentId).ifPresent(assessmentRepository::delete);
        }
        contentRepository.delete(content);
    }

    @Transactional
    public List<ContentResponse> reorder(Long productId, ReorderContentRequest request) {
        productRepository.findById(productId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.PRODUCT_NOT_FOUND));

        List<ProductContent> toReorder = new ArrayList<>();
        int tempOffset = -10000;
        for (int i = 0; i < request.getItems().size(); i++) {
            ReorderContentRequest.ContentOrder item = request.getItems().get(i);
            ProductContent content = contentRepository.findById(item.getContentId())
                    .orElseThrow(() -> new InternalException(InternalErrorCode.CONTENT_NOT_FOUND));
            if (!content.getProduct().getId().equals(productId)) {
                throw new InternalException(InternalErrorCode.CONTENT_NOT_FOUND);
            }
            content.setOrderIndex(tempOffset - i);
            toReorder.add(content);
        }
        contentRepository.saveAllAndFlush(toReorder);

        for (int i = 0; i < request.getItems().size(); i++) {
            toReorder.get(i).setOrderIndex(request.getItems().get(i).getOrderIndex());
        }
        contentRepository.saveAllAndFlush(toReorder);

        return listContent(productId);
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> listContent(Long productId) {
        return contentRepository.findByProductIdOrderByOrderIndexAsc(productId).stream()
                .map(this::toContentResponse)
                .toList();
    }

    @Transactional
    public QuestionResponse addQuestion(Long contentId, CreateQuestionRequest request) {
        Assessment assessment = assessmentRepository.findByProductContentId(contentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.ASSESSMENT_NOT_FOUND));

        AssessmentQuestion question = new AssessmentQuestion(assessment, request.getQuestionText());
        question = questionRepository.save(question);

        List<AssessmentOption> options = new ArrayList<>();
        for (CreateQuestionRequest.OptionRequest optReq : request.getOptions()) {
            options.add(new AssessmentOption(question, optReq.getOptionText(), optReq.getCorrect()));
        }
        question.getOptions().addAll(options);
        question = questionRepository.save(question);

        return toQuestionResponse(question);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long contentId, Long questionId, CreateQuestionRequest request) {
        Assessment assessment = assessmentRepository.findByProductContentId(contentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.ASSESSMENT_NOT_FOUND));

        AssessmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.QUESTION_NOT_FOUND));
        if (!question.getAssessment().getId().equals(assessment.getId())) {
            throw new InternalException(InternalErrorCode.QUESTION_NOT_FOUND);
        }

        question.setQuestionText(request.getQuestionText());
        question.getOptions().clear();
        questionRepository.save(question);

        for (CreateQuestionRequest.OptionRequest optReq : request.getOptions()) {
            question.getOptions().add(new AssessmentOption(question, optReq.getOptionText(), optReq.getCorrect()));
        }
        question = questionRepository.save(question);

        return toQuestionResponse(question);
    }

    @Transactional
    public void deleteQuestion(Long contentId, Long questionId) {
        Assessment assessment = assessmentRepository.findByProductContentId(contentId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.ASSESSMENT_NOT_FOUND));

        AssessmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new InternalException(InternalErrorCode.QUESTION_NOT_FOUND));
        if (!question.getAssessment().getId().equals(assessment.getId())) {
            throw new InternalException(InternalErrorCode.QUESTION_NOT_FOUND);
        }
        questionRepository.delete(question);
    }

    private ContentResponse toContentResponse(ProductContent content) {
        ContentResponse.LessonDetail lessonDetail = null;
        ContentResponse.AssessmentDetail assessmentDetail = null;

        if (content.getContentType() == ProductContent.ContentType.LESSON) {
            lessonDetail = lessonRepository.findByProductContentId(content.getId())
                    .map(lesson -> ContentResponse.LessonDetail.builder()
                            .id(lesson.getId())
                            .lessonType(lesson.getLessonType().name())
                            .videoUrl(lesson.getVideoUrl())
                            .pdfUrl(lesson.getPdfUrl())
                            .durationSeconds(lesson.getDurationSeconds())
                            .build())
                    .orElse(null);
        } else if (content.getContentType() == ProductContent.ContentType.ASSESSMENT) {
            assessmentDetail = assessmentRepository.findByProductContentId(content.getId())
                    .map(a -> ContentResponse.AssessmentDetail.builder()
                            .id(a.getId())
                            .passPercentage(a.getPassPercentage())
                            .questionCount(a.getQuestions().size())
                            .build())
                    .orElse(null);
        }

        return ContentResponse.builder()
                .id(content.getId())
                .contentType(content.getContentType().name())
                .title(content.getTitle())
                .orderIndex(content.getOrderIndex())
                .mandatory(content.isMandatory())
                .lesson(lessonDetail)
                .assessment(assessmentDetail)
                .build();
    }

    private QuestionResponse toQuestionResponse(AssessmentQuestion question) {
        List<QuestionResponse.OptionResponse> options = question.getOptions().stream()
                .map(o -> QuestionResponse.OptionResponse.builder()
                        .id(o.getId())
                        .optionText(o.getOptionText())
                        .correct(o.isCorrect())
                        .build())
                .toList();

        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .build();
    }
}

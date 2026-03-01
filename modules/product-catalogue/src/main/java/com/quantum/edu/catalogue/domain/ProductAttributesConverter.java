package com.quantum.edu.catalogue.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductAttributesConverter implements AttributeConverter<ProductAttributes, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(ProductAttributes attributes) {
        if (attributes == null) return null;
        try {
            return MAPPER.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize ProductAttributes", e);
        }
    }

    @Override
    public ProductAttributes convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, ProductAttributes.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize ProductAttributes", e);
        }
    }
}

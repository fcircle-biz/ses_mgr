package com.ses_mgr.admin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON形式の属性をMap<String, Object>に変換するためのコンバーター
 * Converter to transform JSON attributes to Map<String, Object> and vice versa
 */
@Converter
public class AttributeConverter implements jakarta.persistence.AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * エンティティ属性を列のデータに変換
     * Convert entity attribute to database column data
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert Map to JSON string", e);
        }
    }

    /**
     * 列のデータをエンティティ属性に変換
     * Convert database column data to entity attribute
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to convert JSON string to Map", e);
        }
    }
}
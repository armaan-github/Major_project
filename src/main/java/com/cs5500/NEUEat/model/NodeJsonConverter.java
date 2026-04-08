package com.cs5500.NEUEat.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class NodeJsonConverter implements AttributeConverter<Node, String> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

  @Override
  public String convertToDatabaseColumn(Node attribute) {
    if (attribute == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize Node", e);
    }
  }

  @Override
  public Node convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return new Node();
    }
    try {
      return OBJECT_MAPPER.readValue(dbData, Node.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to deserialize Node", e);
    }
  }
}

package org.ossfmct.projects.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.type.TypeReference;
import org.ossfmct.projects.submissions.enums.SubmissionStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Convertors {

    @Converter(autoApply = false)
    public class StatusHistoryConverter implements AttributeConverter<Map<String, SubmissionStatus>, String> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(Map<String, SubmissionStatus> map) {
            try {
                return map == null ? null : mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<String, SubmissionStatus> convertToEntityAttribute(String json) {
            try {
                if (json == null) return new HashMap<>();
                return mapper.readValue(json, new TypeReference<Map<String, SubmissionStatus>>() {});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Converter(autoApply = false)
    public class MapToJsonConverter implements AttributeConverter<Map<?, ?>, String> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(Map<?, ?> map) {
            try {
                return map == null ? null : mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting map to JSON", e);
            }
        }

        @Override
        public Map<?, ?> convertToEntityAttribute(String json) {
            try {
                return json == null ? new HashMap<>() : mapper.readValue(json, HashMap.class);
            } catch (IOException e) {
                throw new RuntimeException("Error converting JSON to map", e);
            }
        }
    }

    @Converter(autoApply = false)
    public class ListToJsonConverter implements AttributeConverter<List<String>, String> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(List<String> list) {
            try {
                return list == null ? null : mapper.writeValueAsString(list);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting list to JSON", e);
            }
        }

        @Override
        public List<String> convertToEntityAttribute(String json) {
            try {
                return json == null ? new ArrayList<>() : mapper.readValue(json, new TypeReference<List<String>>() {});
            } catch (IOException e) {
                throw new RuntimeException("Error converting JSON to list", e);
            }
        }
    }
}
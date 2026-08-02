package com.example.star.service;

import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.repository.RuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class RuleService {
    private final RuleRepository ruleRepository;
    private final ObjectMapper objectMapper;

    public RuleService(RuleRepository ruleRepository, ObjectMapper objectMapper) {
        this.ruleRepository = ruleRepository;
        this.objectMapper = objectMapper;
    }

    public RuleResponse createRule(RuleRequest request) throws Exception {
        UUID id = UUID.randomUUID();
        String json = objectMapper.writeValueAsString(request);
        ruleRepository.save(id, json);
        return new RuleResponse(id, request.getProductName(), request.getProductId(), request.getProductText(), null);
    }

    public List<RuleResponse> getAllRules() throws Exception {
        List<String> jsonList = ruleRepository.findAll();
        return jsonList.stream()
                .map(json -> {
                    try {
                        RuleRequest request = objectMapper.readValue(json, RuleRequest.class);
                        return new RuleResponse(UUID.randomUUID(), request.getProductName(), request.getProductId(), request.getProductText(), null);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    public void deleteRule(UUID id) {
        ruleRepository.deleteById(id);
    }
    public List<Map<String, Object>> getAllRulesRaw() {
        List<String> jsonList = ruleRepository.findAll();
        return jsonList.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, Map.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }
}
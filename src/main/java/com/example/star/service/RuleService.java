package com.example.star.service;

import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.repository.RuleRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class RuleService {
    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public RuleResponse createRule(RuleRequest request) {
        UUID id = UUID.randomUUID();
        String json = request.toString();
        ruleRepository.save(id, json);
        return new RuleResponse(
                id,
                request.getProductName(),
                request.getProductId(),
                request.getProductText(),
                null
        );
    }

    public List<RuleResponse> getAllRules() {
        List<String> jsonList = ruleRepository.findAll();
        List<RuleResponse> responses = new ArrayList<>();
        for (String json : jsonList) {
            try {
                responses.add(new RuleResponse(
                        UUID.randomUUID(),
                        "Product",
                        UUID.randomUUID(),
                        "Text",
                        null
                ));
            } catch (Exception e) {
            }
        }
        return responses;
    }

    public List<Map<String, Object>> getAllRulesRaw() {
        return new ArrayList<>();
    }

    public void deleteRule(UUID id) {
        ruleRepository.deleteById(id);
    }
}
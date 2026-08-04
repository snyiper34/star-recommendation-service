package com.example.star.service;

import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.entity.RuleEntity;
import com.example.star.entity.RuleQueryEntity;
import com.example.star.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleService {
    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        List<RuleQueryEntity> queries = request.getRule().stream()
                .map(q -> new RuleQueryEntity(
                        q.getQuery(),
                        String.join("|", q.getArguments()),
                        q.isNegate(),
                        request.getRule().indexOf(q)
                ))
                .collect(Collectors.toList());

        RuleEntity entity = new RuleEntity(
                request.getProductName(),
                request.getProductId(),
                request.getProductText(),
                queries
        );

        RuleEntity saved = ruleRepository.save(entity);

        List<RuleResponse.RuleQueryDto> responseQueries = saved.getQueries().stream()
                .map(q -> new RuleResponse.RuleQueryDto(
                        q.getQueryType(),
                        List.of(q.getArguments().split("\\|")),
                        q.getNegate()
                ))
                .collect(Collectors.toList());

        return new RuleResponse(
                saved.getId(),
                saved.getProductName(),
                saved.getProductId(),
                saved.getProductText(),
                responseQueries
        );
    }

    public List<RuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(entity -> {
                    List<RuleResponse.RuleQueryDto> queries = entity.getQueries().stream()
                            .map(q -> new RuleResponse.RuleQueryDto(
                                    q.getQueryType(),
                                    List.of(q.getArguments().split("\\|")),
                                    q.getNegate()
                            ))
                            .collect(Collectors.toList());
                    return new RuleResponse(
                            entity.getId(),
                            entity.getProductName(),
                            entity.getProductId(),
                            entity.getProductText(),
                            queries
                    );
                })
                .collect(Collectors.toList());
    }
    public List<Map<String, Object>> getAllRulesRaw() {
        List<RuleEntity> entities = ruleRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (RuleEntity entity : entities) {
            Map<String, Object> ruleMap = new HashMap<>();
            ruleMap.put("productName", entity.getProductName());
            ruleMap.put("productId", entity.getProductId().toString());
            ruleMap.put("productText", entity.getProductText());

            List<Map<String, Object>> queries = entity.getQueries().stream()
                    .map(q -> {
                        Map<String, Object> queryMap = new HashMap<>();
                        queryMap.put("query", q.getQueryType());
                        queryMap.put("arguments", List.of(q.getArguments().split("\\|")));
                        queryMap.put("negate", q.getNegate());
                        return queryMap;
                    })
                    .collect(Collectors.toList());

            ruleMap.put("rule", queries);
            result.add(ruleMap);
        }

        return result;
    }

    public void deleteRule(UUID id) {
        ruleRepository.deleteById(id);
    }
}
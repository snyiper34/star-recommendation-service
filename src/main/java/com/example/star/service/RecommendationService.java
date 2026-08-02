package com.example.star.service;

import com.example.star.dto.RecommendationDto;
import com.example.star.repository.RecommendationRepository;
import com.example.star.service.rules.RecommendationRuleSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecommendationService {
    private final List<RecommendationRuleSet> ruleSets;
    private final RecommendationRepository repository;
    private final RuleService ruleService;
    private final ObjectMapper objectMapper;

    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                 RecommendationRepository repository,
                                 RuleService ruleService,
                                 ObjectMapper objectMapper) {
        this.ruleSets = ruleSets;
        this.repository = repository;
        this.ruleService = ruleService;
        this.objectMapper = objectMapper;
    }

    public List<RecommendationDto> getRecommendations(UUID userId) {
        List<RecommendationDto> recommendations = new ArrayList<>();


        for (RecommendationRuleSet ruleSet : ruleSets) {
            ruleSet.check(userId).ifPresent(recommendations::add);
        }


        List<Map<String, Object>> dynamicRules = ruleService.getAllRulesRaw();
        for (Map<String, Object> rule : dynamicRules) {
            if (evaluateDynamicRule(userId, rule)) {
                String productName = (String) rule.get("productName");
                String productId = (String) rule.get("productId");
                String productText = (String) rule.get("productText");
                recommendations.add(new RecommendationDto(productName, UUID.fromString(productId), productText));
            }
        }

        return recommendations;
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateDynamicRule(UUID userId, Map<String, Object> rule) {
        List<Map<String, Object>> queries = (List<Map<String, Object>>) rule.get("rule");
        if (queries == null) return false;

        for (Map<String, Object> query : queries) {
            String queryType = (String) query.get("query");
            List<String> arguments = (List<String>) query.get("arguments");
            boolean negate = (boolean) query.get("negate");

            boolean result = evaluateQuery(userId, queryType, arguments);
            if (negate) result = !result;
            if (!result) return false;
        }
        return true;
    }

    private boolean evaluateQuery(UUID userId, String queryType, List<String> args) {
        return switch (queryType) {
            case "USER_OF" -> repository.hasProductByType(userId, args.get(0));
            case "ACTIVE_USER_OF" -> repository.getTransactionCountByType(userId, args.get(0)) >= 5;
            case "TRANSACTION_SUM_COMPARE" -> {
                long sum = repository.getSumByProductAndTransactionType(userId, args.get(0), args.get(1));
                yield compare(sum, args.get(2), Long.parseLong(args.get(3)));
            }
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> {
                long depositSum = repository.getSumByProductAndTransactionType(userId, args.get(0), "DEPOSIT");
                long withdrawSum = repository.getSumByProductAndTransactionType(userId, args.get(0), "WITHDRAW");
                yield compare(depositSum, args.get(1), withdrawSum);
            }
            default -> throw new IllegalArgumentException("Unknown query type: " + queryType);
        };
    }

    private boolean compare(long left, String operator, long right) {
        return switch (operator) {
            case ">" -> left > right;
            case "<" -> left < right;
            case "=" -> left == right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
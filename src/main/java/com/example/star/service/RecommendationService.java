package com.example.star.service;

import com.example.star.dto.RecommendationDto;
import com.example.star.repository.RecommendationRepository;
import com.example.star.service.rules.RecommendationRuleSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RecommendationService {
    private final List<RecommendationRuleSet> ruleSets;
    private final RecommendationRepository repository;
    private final RuleService ruleService;
    private final ObjectMapper objectMapper;

    private final Cache<String, Boolean> userOfCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private final Cache<String, Boolean> activeUserOfCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    private final Cache<String, Long> transactionSumCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

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

            boolean result = evaluateQueryWithCache(userId, queryType, arguments);
            if (negate) result = !result;
            if (!result) return false;
        }
        return true;
    }

    private boolean evaluateQueryWithCache(UUID userId, String queryType, List<String> args) {
        String key = userId.toString() + "|" + queryType + "|" + String.join("|", args);

        return switch (queryType) {
            case "USER_OF" -> userOfCache.get(key, k -> {
                System.out.println("Cache MISS for USER_OF: " + key);
                return repository.hasProductByType(userId, args.get(0));
            });
            case "ACTIVE_USER_OF" -> activeUserOfCache.get(key, k -> {
                System.out.println("Cache MISS for ACTIVE_USER_OF: " + key);
                return repository.getTransactionCountByType(userId, args.get(0)) >= 5;
            });
            case "TRANSACTION_SUM_COMPARE" -> {
                String operator = args.get(2);
                long constant = Long.parseLong(args.get(3));
                Long cachedSum = transactionSumCache.get(key, k -> {
                    System.out.println("Cache MISS for TRANSACTION_SUM_COMPARE: " + key);
                    return repository.getSumByProductAndTransactionType(userId, args.get(0), args.get(1));
                });
                yield compare(cachedSum, operator, constant);
            }
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> {
                String keyDeposit = userId + "|DEPOSIT|" + args.get(0);
                String keyWithdraw = userId + "|WITHDRAW|" + args.get(0);
                Long depositSum = transactionSumCache.get(keyDeposit, k -> {
                    System.out.println("Cache MISS for DEPOSIT: " + keyDeposit);
                    return repository.getSumByProductAndTransactionType(userId, args.get(0), "DEPOSIT");
                });
                Long withdrawSum = transactionSumCache.get(keyWithdraw, k -> {
                    System.out.println("Cache MISS for WITHDRAW: " + keyWithdraw);
                    return repository.getSumByProductAndTransactionType(userId, args.get(0), "WITHDRAW");
                });
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
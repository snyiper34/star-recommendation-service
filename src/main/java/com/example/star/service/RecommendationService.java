package com.example.star.service;

import com.example.star.dto.RecommendationDto;
import com.example.star.repository.RecommendationRepository;
import com.example.star.service.rules.RecommendationRuleSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для получения рекомендаций банковских продуктов.
 * Объединяет фиксированные и динамические правила.
 */
@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final List<RecommendationRuleSet> ruleSets;
    private final RecommendationRepository repository;
    private final RuleService ruleService;
    private final ObjectMapper objectMapper;

    // ===== КЕШИРОВАНИЕ =====

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

    /**
     * Конструктор сервиса рекомендаций.
     *
     * @param ruleSets    список фиксированных правил
     * @param repository  репозиторий для работы с транзакциями
     * @param ruleService сервис для работы с динамическими правилами
     * @param objectMapper маппер для JSON
     */
    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                 RecommendationRepository repository,
                                 RuleService ruleService,
                                 ObjectMapper objectMapper) {
        this.ruleSets = ruleSets;
        this.repository = repository;
        this.ruleService = ruleService;
        this.objectMapper = objectMapper;
    }

    /**
     * Получить список рекомендаций для пользователя.
     *
     * @param userId идентификатор пользователя (UUID)
     * @return список рекомендаций (может быть пустым)
     */
    public List<RecommendationDto> getRecommendations(UUID userId) {
        logger.info("Getting recommendations for user: {}", userId);
        List<RecommendationDto> recommendations = new ArrayList<>();

        // 1. Фиксированные правила
        logger.debug("Checking fixed rules for user: {}", userId);
        for (RecommendationRuleSet ruleSet : ruleSets) {
            ruleSet.check(userId).ifPresent(recommendations::add);
        }

        // 2. Динамические правила
        logger.debug("Checking dynamic rules for user: {}", userId);
        List<Map<String, Object>> dynamicRules = ruleService.getAllRulesRaw();
        for (Map<String, Object> rule : dynamicRules) {
            if (evaluateDynamicRule(userId, rule)) {
                String productName = (String) rule.get("productName");
                String productId = (String) rule.get("productId");
                String productText = (String) rule.get("productText");
                recommendations.add(new RecommendationDto(productName, UUID.fromString(productId), productText));
                logger.info("Dynamic rule matched for user {}: {}", userId, productName);
            }
        }

        logger.info("Found {} recommendations for user {}", recommendations.size(), userId);
        return recommendations;
    }

    /**
     * Проверить, выполняются ли условия динамического правила для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param rule   динамическое правило в виде Map
     * @return true — если все условия выполнены, false — если нет
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateDynamicRule(UUID userId, Map<String, Object> rule) {
        List<Map<String, Object>> queries = (List<Map<String, Object>>) rule.get("rule");
        if (queries == null) {
            logger.warn("Rule has no queries for user: {}", userId);
            return false;
        }

        for (Map<String, Object> query : queries) {
            String queryType = (String) query.get("query");
            List<String> arguments = (List<String>) query.get("arguments");
            boolean negate = (boolean) query.get("negate");

            boolean result = evaluateQueryWithCache(userId, queryType, arguments);
            if (negate) result = !result;
            if (!result) {
                logger.debug("Query failed for user {}: {} with args {}", userId, queryType, arguments);
                return false;
            }
        }
        return true;
    }

    /**
     * Выполнить запрос к базе знаний с кешированием результата.
     *
     * @param userId    идентификатор пользователя
     * @param queryType тип запроса
     * @param args      аргументы запроса
     * @return результат выполнения запроса
     */
    private boolean evaluateQueryWithCache(UUID userId, String queryType, List<String> args) {
        String key = userId.toString() + "|" + queryType + "|" + String.join("|", args);

        return switch (queryType) {
            case "USER_OF" -> userOfCache.get(key, k -> {
                logger.debug("Cache MISS for USER_OF: {}", key);
                return repository.hasProductByType(userId, args.get(0));
            });
            case "ACTIVE_USER_OF" -> activeUserOfCache.get(key, k -> {
                logger.debug("Cache MISS for ACTIVE_USER_OF: {}", key);
                return repository.getTransactionCountByType(userId, args.get(0)) >= 5;
            });
            case "TRANSACTION_SUM_COMPARE" -> {
                String operator = args.get(2);
                long constant = Long.parseLong(args.get(3));
                Long cachedSum = transactionSumCache.get(key, k -> {
                    logger.debug("Cache MISS for TRANSACTION_SUM_COMPARE: {}", key);
                    return repository.getSumByProductAndTransactionType(userId, args.get(0), args.get(1));
                });
                yield compare(cachedSum, operator, constant);
            }
            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> {
                String keyDeposit = userId + "|DEPOSIT|" + args.get(0);
                String keyWithdraw = userId + "|WITHDRAW|" + args.get(0);
                Long depositSum = transactionSumCache.get(keyDeposit, k -> {
                    logger.debug("Cache MISS for DEPOSIT: {}", keyDeposit);
                    return repository.getSumByProductAndTransactionType(userId, args.get(0), "DEPOSIT");
                });
                Long withdrawSum = transactionSumCache.get(keyWithdraw, k -> {
                    logger.debug("Cache MISS for WITHDRAW: {}", keyWithdraw);
                    return repository.getSumByProductAndTransactionType(userId, args.get(0), "WITHDRAW");
                });
                yield compare(depositSum, args.get(1), withdrawSum);
            }
            default -> {
                logger.error("Unknown query type: {}", queryType);
                throw new IllegalArgumentException("Unknown query type: " + queryType);
            }
        };
    }

    /**
     * Сравнить два числа с заданным оператором.
     *
     * @param left     левая часть
     * @param operator оператор сравнения
     * @param right    правая часть
     * @return true — если сравнение истинно, false — если нет
     */
    private boolean compare(long left, String operator, long right) {
        return switch (operator) {
            case ">" -> left > right;
            case "<" -> left < right;
            case "=" -> left == right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            default -> {
                logger.error("Unknown operator: {}", operator);
                throw new IllegalArgumentException("Unknown operator: " + operator);
            }
        };
    }
}
package com.example.star.service;

import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.entity.RuleEntity;
import com.example.star.entity.RuleQueryEntity;
import com.example.star.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления динамическими правилами рекомендаций.
 * Предоставляет CRUD-операции для правил.
 */
@Service
public class RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Создать новое динамическое правило.
     *
     * @param request DTO с данными правила
     * @return созданное правило с присвоенным ID
     */
    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        logger.info("Creating new rule for product: {}", request.getProductName());
        logger.debug("Rule request: {}", request);

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
        logger.info("Rule created successfully with id: {}", saved.getId());

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

    /**
     * Получить все динамические правила в виде DTO.
     *
     * @return список всех правил
     */
    public List<RuleResponse> getAllRules() {
        logger.info("Getting all rules");
        List<RuleResponse> rules = ruleRepository.findAll().stream()
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
        logger.debug("Found {} rules", rules.size());
        return rules;
    }

    /**
     * Получить все динамические правила в сыром виде (Map).
     * Используется для проверки условий в RecommendationService.
     *
     * @return список правил в виде Map
     */
    public List<Map<String, Object>> getAllRulesRaw() {
        logger.debug("Getting all rules raw");
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

        logger.debug("Found {} raw rules", result.size());
        return result;
    }

    /**
     * Удалить правило по ID.
     *
     * @param id идентификатор правила
     */
    public void deleteRule(UUID id) {
        logger.warn("Deleting rule with id: {}", id);
        if (!ruleRepository.existsById(id)) {
            logger.error("Rule not found with id: {}", id);
            throw new RuntimeException("Rule not found with id: " + id);
        }
        ruleRepository.deleteById(id);
        logger.info("Rule deleted successfully with id: {}", id);
    }
}
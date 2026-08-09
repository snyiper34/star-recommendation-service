package com.example.star.controller;

import com.example.star.dto.RuleListResponse;
import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления динамическими правилами рекомендаций.
 * Предоставляет CRUD-операции через REST API.
 */
@RestController
@RequestMapping("/rule")
public class RuleController {

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * Создать новое динамическое правило.
     *
     * @param request тело запроса с данными правила
     * @return созданное правило с присвоенным ID
     */
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@RequestBody RuleRequest request) {
        logger.info("Received POST /rule request for product: {}", request.getProductName());
        RuleResponse response = ruleService.createRule(request);
        logger.info("Rule created successfully with id: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Получить список всех динамических правил.
     *
     * @return список правил
     */
    @GetMapping
    public ResponseEntity<RuleListResponse> getAllRules() {
        logger.info("Received GET /rule request");
        List<RuleResponse> rules = ruleService.getAllRules();
        logger.debug("Returning {} rules", rules.size());
        return ResponseEntity.ok(new RuleListResponse(rules));
    }

    /**
     * Удалить правило по ID.
     *
     * @param id идентификатор правила
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        logger.info("Received DELETE /rule/{} request", id);
        ruleService.deleteRule(id);
        logger.info("Rule deleted successfully with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
package com.example.star.controller;

import com.example.star.dto.RuleListResponse;
import com.example.star.dto.RuleRequest;
import com.example.star.dto.RuleResponse;
import com.example.star.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rule")
public class RuleController {
    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@RequestBody RuleRequest request) throws Exception {
        RuleResponse response = ruleService.createRule(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RuleListResponse> getAllRules() throws Exception {
        List<RuleResponse> rules = ruleService.getAllRules();
        return ResponseEntity.ok(new RuleListResponse(rules));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
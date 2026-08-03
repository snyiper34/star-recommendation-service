package com.example.star.dto;

import java.util.List;
import java.util.UUID;

public class RuleResponse {
    private UUID id;
    private String productName;
    private UUID productId;
    private String productText;
    private List<RuleQueryDto> rule;

    public static class RuleQueryDto {
        private String query;
        private List<String> arguments;
        private boolean negate;

        public RuleQueryDto(String query, List<String> arguments, boolean negate) {
            this.query = query;
            this.arguments = arguments;
            this.negate = negate;
        }

        public String getQuery() { return query; }
        public List<String> getArguments() { return arguments; }
        public boolean isNegate() { return negate; }
    }

    public RuleResponse(UUID id, String productName, UUID productId, String productText, List<RuleQueryDto> rule) {
        this.id = id;
        this.productName = productName;
        this.productId = productId;
        this.productText = productText;
        this.rule = rule;
    }

    public UUID getId() { return id; }
    public String getProductName() { return productName; }
    public UUID getProductId() { return productId; }
    public String getProductText() { return productText; }
    public List<RuleQueryDto> getRule() { return rule; }
}
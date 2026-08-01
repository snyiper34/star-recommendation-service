package com.example.star.dto;

import java.util.List;
import java.util.UUID;

public class RuleRequest {
    private String productName;
    private UUID productId;
    private String productText;
    private List<RuleQueryDto> rule;


    public static class RuleQueryDto {
        private String query;
        private List<String> arguments;
        private boolean negate;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public List<String> getArguments() { return arguments; }
        public void setArguments(List<String> arguments) { this.arguments = arguments; }
        public boolean isNegate() { return negate; }
        public void setNegate(boolean negate) { this.negate = negate; }
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductText() { return productText; }
    public void setProductText(String productText) { this.productText = productText; }
    public List<RuleQueryDto> getRule() { return rule; }
    public void setRule(List<RuleQueryDto> rule) { this.rule = rule; }
}
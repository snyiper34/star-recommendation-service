package com.example.star.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule_query")
public class RuleQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "query_type", nullable = false)
    private String queryType;

    @Column(name = "arguments", columnDefinition = "JSONB")
    private String arguments;

    @Column(name = "negate")
    private Boolean negate = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleEntity rule;


    public RuleQuery() {
    }

    public RuleQuery(String queryType, List<String> arguments, Boolean negate, Integer sortOrder) {
        this.queryType = queryType;
        this.arguments = arguments != null ? String.join("|", arguments) : "";
        this.negate = negate != null ? negate : false;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public Boolean getNegate() {
        return negate;
    }

    public void setNegate(Boolean negate) {
        this.negate = negate;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public void setRule(RuleEntity rule) {
        this.rule = rule;
    }


    public List<String> getArgumentsList() {
        return arguments != null && !arguments.isEmpty() ? List.of(arguments.split("\\|")) : List.of();
    }
}
package com.example.star.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule_entity")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_text", nullable = false, columnDefinition = "TEXT")
    private String productText;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RuleQueryEntity> queries;


    public RuleEntity() {}

    public RuleEntity(String productName, UUID productId, String productText, List<RuleQueryEntity> queries) {
        this.productName = productName;
        this.productId = productId;
        this.productText = productText;
        this.queries = queries;
        if (queries != null) {
            queries.forEach(q -> q.setRule(this));
        }
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductText() { return productText; }
    public void setProductText(String productText) { this.productText = productText; }

    public List<RuleQueryEntity> getQueries() { return queries; }
    public void setQueries(List<RuleQueryEntity> queries) { this.queries = queries; }
}
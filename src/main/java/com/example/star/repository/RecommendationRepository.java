package com.example.star.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RecommendationRepository {
    private final JdbcTemplate jdbcTemplate;

    // Кеши
    private final Map<String, Boolean> cacheBoolean = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheLong = new ConcurrentHashMap<>();

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String buildKey(UUID userId, String productType) {
        return userId + ":" + productType;
    }

    private String buildKey(UUID userId, String productType, String transactionType) {
        return userId + ":" + productType + ":" + transactionType;
    }


    public boolean hasProductByType(UUID userId, String productType) {
        String key = buildKey(userId, productType);
        return cacheBoolean.computeIfAbsent(key, k -> {
            String sql = "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ?";
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, productType));
        });
    }

    public boolean hasDebitProduct(UUID userId) {
        return hasProductByType(userId, "DEBIT");
    }

    public boolean hasInvestProduct(UUID userId) {
        return hasProductByType(userId, "INVEST");
    }

    public boolean hasCreditProduct(UUID userId) {
        return hasProductByType(userId, "CREDIT");
    }

    public int getTransactionCountByType(UUID userId, String productType) {
        String key = buildKey(userId, productType);
        boolean hasTransactions = cacheBoolean.computeIfAbsent(key, k -> {
            String sql = "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ?";
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, productType));
        });
        return hasTransactions ? 1 : 0;
    }

    public long getSumByProductAndTransactionType(UUID userId, String productType, String transactionType) {
        String key = buildKey(userId, productType, transactionType);
        return cacheLong.computeIfAbsent(key, k -> {
            String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ? AND t.type = ?";
            Long sum = jdbcTemplate.queryForObject(sql, Long.class, userId, productType, transactionType);
            return sum != null ? sum : 0L;
        });
    }

    public long getSumDepositByProductType(UUID userId, String productType) {
        return getSumByProductAndTransactionType(userId, productType, "DEPOSIT");
    }

    public long getSumWithdrawalByProductType(UUID userId, String productType) {
        return getSumByProductAndTransactionType(userId, productType, "WITHDRAWAL");
    }


    public void clearCache() {
        cacheBoolean.clear();
        cacheLong.clear();
    }
}
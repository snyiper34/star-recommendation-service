package com.example.star.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public class RecommendationRepository {
    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean hasDebitProduct(UUID userId) {
        String sql = "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = 'DEBIT'";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId));
    }

    public boolean hasInvestProduct(UUID userId) {
        String sql = "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = 'INVEST'";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId));
    }

    public boolean hasCreditProduct(UUID userId) {
        String sql = "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = 'CREDIT'";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId));
    }

    public long getSumDepositByProductType(UUID userId, String productType) {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ? AND t.type = 'DEPOSIT'";
        return jdbcTemplate.queryForObject(sql, Long.class, userId, productType);
    }

    public long getSumWithdrawalByProductType(UUID userId, String productType) {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ? AND t.type = 'WITHDRAWAL'";
        return jdbcTemplate.queryForObject(sql, Long.class, userId, productType);
    }


    public boolean hasProductByType(UUID userId, String productType) {
        String sql = "SELECT COUNT(*) > 0 FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ?";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, productType));
    }

    public int getTransactionCountByType(UUID userId, String productType) {
        String sql = "SELECT COUNT(*) FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, productType);
    }

    public long getSumByProductAndTransactionType(UUID userId, String productType, String transactionType) {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t JOIN products p ON t.product_id = p.id WHERE t.user_id = ? AND p.type = ? AND t.type = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, userId, productType, transactionType);
    }
}
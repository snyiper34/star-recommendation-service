package com.example.star.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public class RuleRepository {
    private final JdbcTemplate jdbcTemplate;

    public RuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(UUID id, String json) {
        String sql = "INSERT INTO dynamic_rules (id, rule_json) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, json);
    }

    public List<String> findAll() {
        return jdbcTemplate.queryForList("SELECT rule_json FROM dynamic_rules", String.class);
    }

    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM dynamic_rules WHERE id = ?", id);
    }
}
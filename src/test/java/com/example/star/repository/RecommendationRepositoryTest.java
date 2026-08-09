package com.example.star.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Тесты RecommendationRepository")
class RecommendationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RecommendationRepository repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new RecommendationRepository(jdbcTemplate);
        userId = UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

        // Очистка и подготовка тестовых данных
        jdbcTemplate.execute("DELETE FROM transactions");
        jdbcTemplate.execute("DELETE FROM products");

        jdbcTemplate.execute(
                "INSERT INTO products (id, name, type) VALUES ('123e4567-e89b-12d3-a456-426614174000', 'Test Debit', 'DEBIT')"
        );

        jdbcTemplate.execute(
                "INSERT INTO transactions (id, user_id, product_id, type, amount) VALUES " +
                        "('11111111-1111-1111-1111-111111111111', '" + userId + "', '123e4567-e89b-12d3-a456-426614174000', 'DEPOSIT', 50000)"
        );
    }

    @Test
    @DisplayName("hasDebitProduct — должен вернуть true, если у пользователя есть DEBIT-продукт")
    void hasDebitProduct_shouldReturnTrue_whenUserHasDebitProduct() {
        boolean result = repository.hasDebitProduct(userId);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasDebitProduct — должен вернуть false, если у пользователя нет DEBIT-продукта")
    void hasDebitProduct_shouldReturnFalse_whenUserHasNoDebitProduct() {
        UUID newUserId = UUID.randomUUID();
        boolean result = repository.hasDebitProduct(newUserId);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getTransactionCountByType — должен вернуть количество транзакций")
    void getTransactionCountByType_shouldReturnTransactionCount() {
        int count = repository.getTransactionCountByType(userId, "DEBIT");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("getSumDepositByProductType — должен вернуть сумму пополнений")
    void getSumDepositByProductType_shouldReturnSumDeposit() {
        long sum = repository.getSumDepositByProductType(userId, "DEBIT");
        assertThat(sum).isEqualTo(50000);
    }
}
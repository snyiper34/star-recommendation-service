package com.example.star.service.rules;

import com.example.star.dto.RecommendationDto;
import com.example.star.repository.RecommendationRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleCreditRule implements RecommendationRuleSet {

    private static final UUID PRODUCT_ID = UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");
    private static final String PRODUCT_NAME = "Простой кредит";
    private static final String PRODUCT_TEXT = "Откройте мир выгодных кредитов с нами!\n\nИщете способ быстро и без лишних хлопот получить нужную сумму? Тогда наш выгодный кредит — именно то, что вам нужно! Мы предлагаем низкие процентные ставки, гибкие условия и индивидуальный подход к каждому клиенту.\n\nПочему выбирают нас:\n- Быстрое рассмотрение заявки. Мы ценим ваше время, поэтому процесс рассмотрения заявки занимает всего несколько часов.\n- Удобное оформление. Подать заявку на кредит можно онлайн на нашем сайте или в мобильном приложении.\n- Широкий выбор кредитных продуктов. Мы предлагаем кредиты на различные цели: покупку недвижимости, автомобиля, образование, лечение и многое другое.\n\nНе упустите возможность воспользоваться выгодными условиями кредитования от нашей компании!";

    private final RecommendationRepository repository;

    public SimpleCreditRule(RecommendationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {
        boolean hasCredit = repository.hasCreditProduct(userId);
        long debitDeposit = repository.getSumDepositByProductType(userId, "DEBIT");
        long debitWithdrawal = repository.getSumWithdrawalByProductType(userId, "DEBIT");

        boolean condition2 = debitDeposit > debitWithdrawal;
        boolean condition3 = debitWithdrawal > 100000;

        if (!hasCredit && condition2 && condition3) {
            return Optional.of(new RecommendationDto(PRODUCT_NAME, PRODUCT_ID, PRODUCT_TEXT));
        }
        return Optional.empty();
    }
}
package com.example.star.service.rules;

import com.example.star.dto.RecommendationDto;
import com.example.star.repository.RecommendationRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class TopSavingRule implements RecommendationRuleSet {

    private static final UUID PRODUCT_ID = UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");
    private static final String PRODUCT_NAME = "Top Saving";
    private static final String PRODUCT_TEXT = "Откройте свою собственную «Копилку» с нашим банком! «Копилка» — это уникальный банковский инструмент, который поможет вам легко и удобно накапливать деньги на важные цели. Больше никаких забытых чеков и потерянных квитанций — всё под контролем!\n\nПреимущества «Копилки»:\n- Накопление средств на конкретные цели. Установите лимит и срок накопления, и банк будет автоматически переводить определенную сумму на ваш счет.\n- Прозрачность и контроль. Отслеживайте свои доходы и расходы, контролируйте процесс накопления и корректируйте стратегию при необходимости.\n- Безопасность и надежность. Ваши средства находятся под защитой банка, а доступ к ним возможен только через мобильное приложение или интернет-банкинг.\n\nНачните использовать «Копилку» уже сегодня и станьте ближе к своим финансовым целям!";

    private final RecommendationRepository repository;

    public TopSavingRule(RecommendationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RecommendationDto> check(UUID userId) {

        boolean hasDebit = repository.hasDebitProduct(userId);
        long debitDeposit = repository.getSumDepositByProductType(userId, "DEBIT");
        long savingDeposit = repository.getSumDepositByProductType(userId, "SAVING");
        long debitWithdrawal = repository.getSumWithdrawalByProductType(userId, "DEBIT");

        boolean condition2 = (debitDeposit >= 50000) || (savingDeposit >= 50000);
        boolean condition3 = debitDeposit > debitWithdrawal;

        if (hasDebit && condition2 && condition3) {
            return Optional.of(new RecommendationDto(PRODUCT_NAME, PRODUCT_ID, PRODUCT_TEXT));
        }

        return Optional.empty();
    }
}
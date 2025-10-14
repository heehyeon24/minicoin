package study.cryptochain.minicoin.model;

import java.math.BigDecimal;

public record Transaction(
        String txId,
        String sender,
        String recipient,
        BigDecimal amount
) {
}

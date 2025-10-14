package study.cryptochain.minicoin.web.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import study.cryptochain.minicoin.model.Transaction;

public record TransactionResponse(
        @Schema(description = "Unique transaction identifier")
        String txId,
        @Schema(description = "Sender address")
        String sender,
        @Schema(description = "Recipient address")
        String recipient,
        @Schema(description = "Amount transferred in MiniCoin units")
        BigDecimal amount
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.txId(),
                transaction.sender(),
                transaction.recipient(),
                transaction.amount()
        );
    }
}

package study.cryptochain.minicoin.web.dto.node;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import study.cryptochain.minicoin.model.Transaction;

public record NodeTransactionPayload(
        String txId,
        String sender,
        String recipient,
        BigDecimal amount
) {

    @JsonCreator
    public NodeTransactionPayload(
            @JsonProperty("txId") String txId,
            @JsonProperty("sender") String sender,
            @JsonProperty("recipient") String recipient,
            @JsonProperty("amount") BigDecimal amount
    ) {
        this.txId = txId;
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    public static NodeTransactionPayload from(Transaction transaction) {
        return new NodeTransactionPayload(
                transaction.txId(),
                transaction.sender(),
                transaction.recipient(),
                transaction.amount()
        );
    }

    public Transaction toTransaction() {
        return new Transaction(txId, sender, recipient, amount);
    }
}

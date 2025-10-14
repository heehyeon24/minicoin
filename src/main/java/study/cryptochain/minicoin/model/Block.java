package study.cryptochain.minicoin.model;

import java.time.Instant;
import java.util.List;

public record Block(
        int index,
        Instant timestamp,
        String previousHash,
        String hash,
        long nonce,
        List<Transaction> transactions
) {

    public Block {
        transactions = List.copyOf(transactions);
    }
}

package study.cryptochain.minicoin.web.dto.node;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import study.cryptochain.minicoin.model.Block;

public record NodeBlockPayload(
        int index,
        Instant timestamp,
        String previousHash,
        String hash,
        long nonce,
        List<NodeTransactionPayload> transactions
) {

    @JsonCreator
    public NodeBlockPayload(
            @JsonProperty("index") int index,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("previousHash") String previousHash,
            @JsonProperty("hash") String hash,
            @JsonProperty("nonce") long nonce,
            @JsonProperty("transactions") List<NodeTransactionPayload> transactions
    ) {
        this.index = index;
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.hash = hash;
        this.nonce = nonce;
        this.transactions = transactions == null ? List.of() : List.copyOf(transactions);
    }

    public static NodeBlockPayload from(Block block) {
        return new NodeBlockPayload(
                block.index(),
                block.timestamp(),
                block.previousHash(),
                block.hash(),
                block.nonce(),
                block.transactions().stream()
                        .map(NodeTransactionPayload::from)
                        .toList()
        );
    }

    public Block toBlock() {
        return new Block(
                index,
                timestamp,
                previousHash,
                hash,
                nonce,
                transactions.stream()
                        .map(NodeTransactionPayload::toTransaction)
                        .toList()
        );
    }
}

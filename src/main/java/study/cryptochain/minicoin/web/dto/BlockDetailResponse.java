package study.cryptochain.minicoin.web.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import study.cryptochain.minicoin.model.Block;

public record BlockDetailResponse(
        @Schema(description = "Block height within the chain")
        int index,
        @Schema(description = "Timestamp when the block was mined")
        Instant timestamp,
        @Schema(description = "Current block hash")
        String hash,
        @Schema(description = "Hash of the previous block")
        String previousHash,
        @Schema(description = "Nonce that satisfied the Proof-of-Work condition")
        long nonce,
        @Schema(description = "Transactions recorded in the block")
        List<TransactionResponse> transactions
) {

    public static BlockDetailResponse from(Block block) {
        List<TransactionResponse> transactionResponses = block.transactions().stream()
                .map(TransactionResponse::from)
                .toList();
        return new BlockDetailResponse(
                block.index(),
                block.timestamp(),
                block.hash(),
                block.previousHash(),
                block.nonce(),
                transactionResponses
        );
    }
}

package study.cryptochain.minicoin.web.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import study.cryptochain.minicoin.model.Block;

public record BlockSummaryResponse(
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
        @Schema(description = "Number of transactions included in the block")
        int transactionCount
) {

    public static BlockSummaryResponse from(Block block) {
        return new BlockSummaryResponse(
                block.index(),
                block.timestamp(),
                block.hash(),
                block.previousHash(),
                block.nonce(),
                block.transactions().size()
        );
    }
}

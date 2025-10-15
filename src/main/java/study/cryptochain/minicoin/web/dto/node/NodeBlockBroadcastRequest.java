package study.cryptochain.minicoin.web.dto.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import study.cryptochain.minicoin.model.Block;

public record NodeBlockBroadcastRequest(
        String originNodeId,
        NodeBlockPayload block
) {

    @JsonCreator
    public NodeBlockBroadcastRequest(
            @JsonProperty("originNodeId") String originNodeId,
            @JsonProperty("block") NodeBlockPayload block
    ) {
        this.originNodeId = originNodeId;
        this.block = block;
    }

    public Block toBlock() {
        return block.toBlock();
    }
}

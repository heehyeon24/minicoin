package study.cryptochain.minicoin.web.dto.node;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import study.cryptochain.minicoin.model.Block;

public record NodeChainResponse(
        String nodeId,
        List<NodeBlockPayload> blocks
) {

    @JsonCreator
    public NodeChainResponse(
            @JsonProperty("nodeId") String nodeId,
            @JsonProperty("blocks") List<NodeBlockPayload> blocks
    ) {
        this.nodeId = nodeId;
        this.blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public static NodeChainResponse from(String nodeId, List<Block> blocks) {
        return new NodeChainResponse(
                nodeId,
                blocks.stream()
                        .map(NodeBlockPayload::from)
                        .toList()
        );
    }

    public List<Block> toBlocks() {
        return blocks.stream()
                .map(NodeBlockPayload::toBlock)
                .toList();
    }
}

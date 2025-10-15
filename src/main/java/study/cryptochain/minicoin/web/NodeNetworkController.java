package study.cryptochain.minicoin.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import study.cryptochain.minicoin.config.NodeProperties;
import study.cryptochain.minicoin.model.Block;
import study.cryptochain.minicoin.service.BlockchainService;
import study.cryptochain.minicoin.service.NodeSynchronizationService;
import study.cryptochain.minicoin.web.dto.node.NodeBlockBroadcastRequest;
import study.cryptochain.minicoin.web.dto.node.NodeBlockPayload;
import study.cryptochain.minicoin.web.dto.node.NodeChainResponse;

@RestController
@RequestMapping("/api/nodes")
@Tag(name = "Node", description = "노드 간 블록체인 동기화를 위한 엔드포인트")
public class NodeNetworkController {

    private final BlockchainService blockchainService;
    private final NodeSynchronizationService nodeSynchronizationService;
    private final NodeProperties nodeProperties;

    public NodeNetworkController(BlockchainService blockchainService,
                                 NodeSynchronizationService nodeSynchronizationService,
                                 NodeProperties nodeProperties) {
        this.blockchainService = blockchainService;
        this.nodeSynchronizationService = nodeSynchronizationService;
        this.nodeProperties = nodeProperties;
    }

    @GetMapping("/chain")
    @Operation(summary = "현재 노드의 전체 블록체인 데이터를 반환합니다.")
    public NodeChainResponse getChain() {
        return NodeChainResponse.from(nodeProperties.getId(), blockchainService.getBlocks());
    }

    @PostMapping("/blocks")
    @Operation(summary = "새 블록 브로드캐스트를 수신하여 체인에 반영합니다.")
    public ResponseEntity<Void> receiveBlock(@RequestBody NodeBlockBroadcastRequest request) {
        if (request == null || request.block() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block payload is required");
        }
        Block block = request.toBlock();
        boolean appended = nodeSynchronizationService.handleIncomingBlock(block);
        if (!appended) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Block rejected by validation");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sync")
    @Operation(summary = "즉시 동기화를 수행하고 최신 체인을 반환합니다.")
    public NodeChainResponse triggerSync() {
        nodeSynchronizationService.synchronizeWithPeers();
        return NodeChainResponse.from(nodeProperties.getId(), blockchainService.getBlocks());
    }

    @PostMapping("/broadcast")
    @Operation(summary = "주어진 블록을 피어 노드로 브로드캐스트합니다.")
    public ResponseEntity<Void> broadcast(@RequestBody NodeBlockPayload payload) {
        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block payload is required");
        }
        nodeSynchronizationService.broadcastBlock(payload.toBlock());
        return ResponseEntity.accepted().build();
    }
}

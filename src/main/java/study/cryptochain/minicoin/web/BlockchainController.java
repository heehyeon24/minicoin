package study.cryptochain.minicoin.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import study.cryptochain.minicoin.service.BlockchainService;
import study.cryptochain.minicoin.web.dto.BlockDetailResponse;
import study.cryptochain.minicoin.web.dto.BlockSummaryResponse;

@RestController
@RequestMapping("/api/blocks")
@Tag(name = "Blockchain", description = "블록체인 구조 확인을 위한 REST API")
public class BlockchainController {

    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping
    @Operation(summary = "체인에 포함된 모든 블록 요약 정보를 조회합니다.")
    public List<BlockSummaryResponse> getBlocks() {
        return blockchainService.getBlocks()
                .stream()
                .map(BlockSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{hash}")
    @Operation(summary = "해당 해시를 가진 블록의 세부 정보를 조회합니다.")
    public BlockDetailResponse getBlockByHash(@PathVariable String hash) {
        return blockchainService.findBlockByHash(hash)
                .map(BlockDetailResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found: " + hash));
    }
}

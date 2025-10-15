package study.cryptochain.minicoin.service;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import study.cryptochain.minicoin.config.NodeProperties;
import study.cryptochain.minicoin.model.Block;
import study.cryptochain.minicoin.web.dto.node.NodeBlockBroadcastRequest;
import study.cryptochain.minicoin.web.dto.node.NodeBlockPayload;
import study.cryptochain.minicoin.web.dto.node.NodeChainResponse;

@Service
public class NodeSynchronizationService {

    private static final Logger log = LoggerFactory.getLogger(NodeSynchronizationService.class);

    private final BlockchainService blockchainService;
    private final NodeProperties nodeProperties;
    private final RestTemplate restTemplate;

    public NodeSynchronizationService(BlockchainService blockchainService,
                                      NodeProperties nodeProperties,
                                      @Qualifier("nodeRestTemplate") RestTemplate nodeRestTemplate) {
        this.blockchainService = blockchainService;
        this.nodeProperties = nodeProperties;
        this.restTemplate = nodeRestTemplate;
    }

    @Scheduled(fixedDelayString = "${minicoin.node.sync-interval:PT30S}")
    public void scheduledSync() {
        synchronizeWithPeers();
    }

    public void synchronizeWithPeers() {
        if (nodeProperties.getPeers().isEmpty()) {
            return;
        }
        nodeProperties.getPeers().forEach(this::fetchChainFromPeer);
    }

    public boolean handleIncomingBlock(Block block) {
        boolean appended = blockchainService.addBlock(block);
        if (!appended) {
            log.debug("Rejected broadcast block {}. Triggering full sync.", block.hash());
            synchronizeWithPeers();
        }
        return appended;
    }

    public void broadcastBlock(Block block) {
        if (nodeProperties.getPeers().isEmpty()) {
            return;
        }
        NodeBlockBroadcastRequest request = new NodeBlockBroadcastRequest(
                nodeProperties.getId(),
                NodeBlockPayload.from(block)
        );
        nodeProperties.getPeers().forEach(peer -> sendBroadcast(peer, request));
    }

    private void fetchChainFromPeer(URI peer) {
        for (int attempt = 1; attempt <= nodeProperties.getSyncRetryAttempts(); attempt++) {
            try {
                NodeChainResponse response = restTemplate.getForObject(
                        peer.resolve("/api/nodes/chain"),
                        NodeChainResponse.class
                );
                if (response == null || response.blocks().isEmpty()) {
                    log.debug("Peer {} returned empty chain.", peer);
                    return;
                }
                var candidateChain = response.toBlocks();
                if (!blockchainService.isValidChain(candidateChain)) {
                    log.warn("Discarded invalid chain received from {}", peer);
                    return;
                }
                boolean replaced = blockchainService.replaceChainIfValid(candidateChain);
                if (replaced) {
                    log.info("Replaced local chain with peer {} chain ({} blocks).",
                            peer, response.blocks().size());
                } else {
                    log.debug("Peer {} chain not adopted (length {}).", peer, response.blocks().size());
                }
                return;
            } catch (RestClientException ex) {
                log.warn("Failed to fetch chain from {} (attempt {}): {}", peer, attempt, ex.getMessage());
            }
        }
    }

    private void sendBroadcast(URI peer, NodeBlockBroadcastRequest request) {
        for (int attempt = 1; attempt <= nodeProperties.getSyncRetryAttempts(); attempt++) {
            try {
                ResponseEntity<Void> response = restTemplate.postForEntity(
                        peer.resolve("/api/nodes/blocks"),
                        request,
                        Void.class
                );
                if (response.getStatusCode().is2xxSuccessful()) {
                    return;
                }
            } catch (RestClientException ex) {
                log.warn("Failed to broadcast block to {} (attempt {}): {}", peer, attempt, ex.getMessage());
            }
        }
    }
}

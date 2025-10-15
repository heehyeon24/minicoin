package study.cryptochain.minicoin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import study.cryptochain.minicoin.model.Block;
import study.cryptochain.minicoin.model.Transaction;
import study.cryptochain.minicoin.web.dto.node.NodeChainResponse;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "minicoin.node.peers[0]=http://peer-1",
        "minicoin.node.sync-retry-attempts=1"
})
class NodeSynchronizationServiceTest {

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private NodeSynchronizationService nodeSynchronizationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("nodeRestTemplate")
    private RestTemplate nodeRestTemplate;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        this.server = MockRestServiceServer.createServer(nodeRestTemplate);
    }

    @Test
    void synchronizeWithPeers_adoptsLongerValidChain() throws Exception {
        List<Block> localChain = blockchainService.getBlocks();
        Block lastBlock = localChain.getLast();
        Instant newTimestamp = lastBlock.timestamp().plusSeconds(90);
        List<Transaction> newTransactions = List.of(
                new Transaction("tx-9999", "miner", "dave", new BigDecimal("3.0"))
        );
        long nonce = 8192L;
        String newHash = calculateHash(
                lastBlock.index() + 1,
                newTimestamp,
                lastBlock.hash(),
                nonce,
                newTransactions
        );
        Block extendedBlock = new Block(
                lastBlock.index() + 1,
                newTimestamp,
                lastBlock.hash(),
                newHash,
                nonce,
                newTransactions
        );

        List<Block> remoteChain = new ArrayList<>(localChain);
        remoteChain.add(extendedBlock);
        NodeChainResponse response = NodeChainResponse.from("peer-1", remoteChain);
        DefaultResponseCreator httpResponse = org.springframework.test.web.client.response.MockRestResponseCreators
                .withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON);

        server.expect(ExpectedCount.once(),
                org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("http://peer-1/api/nodes/chain"))
                .andRespond(httpResponse);

        nodeSynchronizationService.synchronizeWithPeers();

        server.verify();
        assertThat(blockchainService.getBlocks())
                .hasSize(remoteChain.size())
                .last()
                .extracting(Block::hash)
                .isEqualTo(newHash);
    }

    private String calculateHash(int index,
                                 Instant timestamp,
                                 String previousHash,
                                 long nonce,
                                 List<Transaction> transactions) {
        String input = index
                + timestamp.toString()
                + previousHash
                + nonce
                + transactions.stream()
                .map(tx -> tx.txId() + tx.sender() + tx.recipient() + tx.amount())
                .reduce("", String::concat);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

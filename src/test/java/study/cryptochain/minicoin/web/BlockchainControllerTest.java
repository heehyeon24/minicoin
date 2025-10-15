package study.cryptochain.minicoin.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import study.cryptochain.minicoin.model.Block;
import study.cryptochain.minicoin.model.Transaction;
import study.cryptochain.minicoin.service.BlockchainService;

@SpringBootTest
@AutoConfigureMockMvc
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlockchainService blockchainService;

    @Test
    void getBlockByHashReturnsBlockDetails() throws Exception {
        Block expectedBlock = blockchainService.getBlocks().get(0);
        Transaction expectedTransaction = expectedBlock.transactions().get(0);

        mockMvc.perform(get("/api/blocks/{hash}", expectedBlock.hash()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.index").value(expectedBlock.index()))
                .andExpect(jsonPath("$.timestamp").value(expectedBlock.timestamp().toString()))
                .andExpect(jsonPath("$.hash").value(expectedBlock.hash()))
                .andExpect(jsonPath("$.previousHash").value(expectedBlock.previousHash()))
                .andExpect(jsonPath("$.nonce").value(expectedBlock.nonce()))
                .andExpect(jsonPath("$.transactions", hasSize(expectedBlock.transactions().size())))
                .andExpect(jsonPath("$.transactions[0].txId").value(expectedTransaction.txId()))
                .andExpect(jsonPath("$.transactions[0].sender").value(expectedTransaction.sender()))
                .andExpect(jsonPath("$.transactions[0].recipient").value(expectedTransaction.recipient()))
                .andExpect(jsonPath("$.transactions[0].amount").value(expectedTransaction.amount().doubleValue()));
    }
}

package study.cryptochain.minicoin.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Service;

import study.cryptochain.minicoin.model.Block;
import study.cryptochain.minicoin.model.Transaction;

@Service
public class BlockchainService {

    private final List<Block> chain;
    private final Block genesisBlock;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public BlockchainService() {
        List<Block> initialChain = initializeChain();
        this.genesisBlock = initialChain.getFirst();
        this.chain = new ArrayList<>(initialChain);
    }

    public List<Block> getBlocks() {
        lock.readLock().lock();
        try {
            return List.copyOf(chain);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Block> findBlockByHash(String hash) {
        lock.readLock().lock();
        try {
            return chain.stream()
                    .filter(block -> block.hash().equalsIgnoreCase(hash))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addBlock(Block candidate) {
        lock.writeLock().lock();
        try {
            Block lastBlock = chain.getLast();
            if (candidate.index() != lastBlock.index() + 1) {
                return false;
            }
            if (!candidate.previousHash().equals(lastBlock.hash())) {
                return false;
            }
            if (!calculateHash(candidate).equals(candidate.hash())) {
                return false;
            }
            chain.add(copyBlock(candidate));
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean replaceChainIfValid(List<Block> candidateChain) {
        if (!isValidChain(candidateChain)) {
            return false;
        }
        lock.writeLock().lock();
        try {
            if (candidateChain.size() <= chain.size()) {
                return false;
            }
            chain.clear();
            candidateChain.stream()
                    .map(this::copyBlock)
                    .forEach(chain::add);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isValidChain(List<Block> candidateChain) {
        if (candidateChain == null || candidateChain.isEmpty()) {
            return false;
        }
        if (!candidateChain.getFirst().equals(genesisBlock)) {
            return false;
        }
        for (int i = 1; i < candidateChain.size(); i++) {
            Block previous = candidateChain.get(i - 1);
            Block current = candidateChain.get(i);
            if (current.index() != previous.index() + 1) {
                return false;
            }
            if (!current.previousHash().equals(previous.hash())) {
                return false;
            }
            if (!calculateHash(current).equals(current.hash())) {
                return false;
            }
        }
        return true;
    }

    private List<Block> initializeChain() {
        List<Block> blocks = new ArrayList<>();

        Block genesisBlock = buildBlock(
                0,
                Instant.parse("2024-01-01T00:00:00Z"),
                "0000000000000000000000000000000000000000000000000000000000000000",
                1000L,
                List.of(
                        new Transaction("tx-genesis-001", "network", "alice", new BigDecimal("50.0"))
                )
        );
        blocks.add(genesisBlock);

        Block secondBlock = buildBlock(
                1,
                Instant.parse("2024-01-01T00:03:30Z"),
                genesisBlock.hash(),
                2048L,
                List.of(
                        new Transaction("tx-0001", "alice", "bob", new BigDecimal("12.5")),
                        new Transaction("tx-0002", "mining-pool-1", "alice", new BigDecimal("6.25"))
                )
        );
        blocks.add(secondBlock);

        Block thirdBlock = buildBlock(
                2,
                Instant.parse("2024-01-01T00:07:45Z"),
                secondBlock.hash(),
                4096L,
                List.of(
                        new Transaction("tx-0003", "bob", "carol", new BigDecimal("2.0")),
                        new Transaction("tx-0004", "carol", "exchange", new BigDecimal("1.5")),
                        new Transaction("tx-0005", "mining-pool-1", "bob", new BigDecimal("6.25"))
                )
        );
        blocks.add(thirdBlock);

        return blocks;
    }

    private Block copyBlock(Block block) {
        return new Block(
                block.index(),
                block.timestamp(),
                block.previousHash(),
                block.hash(),
                block.nonce(),
                block.transactions()
        );
    }

    private Block buildBlock(int index,
                             Instant timestamp,
                             String previousHash,
                             long nonce,
                             List<Transaction> transactions) {
        String hash = calculateHash(index, timestamp, previousHash, nonce, transactions);
        return new Block(index, timestamp, previousHash, hash, nonce, transactions);
    }

    private String calculateHash(Block block) {
        return calculateHash(
                block.index(),
                block.timestamp(),
                block.previousHash(),
                block.nonce(),
                block.transactions()
        );
    }

    private String calculateHash(int index,
                                 Instant timestamp,
                                 String previousHash,
                                 long nonce,
                                 List<Transaction> transactions) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = index
                    + timestamp.toString()
                    + previousHash
                    + nonce
                    + transactions.stream()
                    .map(tx -> tx.txId() + tx.sender() + tx.recipient() + tx.amount())
                    .reduce("", String::concat);
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

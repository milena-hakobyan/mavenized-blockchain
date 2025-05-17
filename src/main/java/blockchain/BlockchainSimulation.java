package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BlockchainSimulation {
    public void startMining() {
        ExecutorService miningExecutor = Executors.newFixedThreadPool(5);
        ExecutorService transactionExecutor = Executors.newSingleThreadExecutor();
        TransactionGenerator txGenerator = new TransactionGenerator();

        Blockchain blockchain = Blockchain.getInstance();

        try {
            for (int i = 0; i < 15; i++) {
                blockchain.clearPendingTransactions();

                List<Transaction> txCopy;

                if (i == 0) {
                    // Genesis block: no transactions
                    txCopy = new ArrayList<>();
                } else {
                    blockchain.startAcceptingTransactions();
                    transactionExecutor.submit(txGenerator);
                    Thread.sleep(500); // Give the TransactionGenerator time to add transactions
                    blockchain.stopAcceptingTransactions();

                    txCopy = new ArrayList<>(blockchain.getPendingTransactions());
                }

                String prevHash = (i == 0) ? "0" : blockchain.getLastBlock().getHash();

                List<Callable<Block>> callables = new ArrayList<>();
                for (int m = 0; m < 5; m++) {
                    int minerId = m + 1;

                    callables.add(() -> {
                        List<Transaction> blockTx = new ArrayList<>(txCopy); // defensive copy per miner
                        Block block = new Block(prevHash, blockchain.getLeadingZeros(), minerId, blockTx);
                        block.mine(blockchain.getId());
                        return block;
                    });
                }

                Block block = miningExecutor.invokeAny(callables);

                if (blockchain.validateBlock(block)) {
                    blockchain.addBlock(block, block.getGenerationTime());
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Mining interrupted", e);
        } finally {
            shutdownExecutor(transactionExecutor);
            shutdownExecutor(miningExecutor);

            // Print the final blockchain
            blockchain.printBlockChain();
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}


package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BlockchainSimulation {
    public void startMining() {
        ExecutorService miningExecutor = Executors.newFixedThreadPool(5);
        ExecutorService transactionExecutor = Executors.newSingleThreadExecutor();

        Blockchain blockchain = Blockchain.getInstance();

        try {
            for (int i = 0; i < 15; i++) {
                blockchain.clearPendingTransactions();

                List<String> txCopy;

                if (i == 0) {
                    // Genesis block: no transactions
                    txCopy = new ArrayList<>();
                    txCopy.add("No transactions");
                } else {
                    blockchain.startAcceptingMessages();
                    transactionExecutor.submit(new TransactionGenerator());
                    Thread.sleep(500); // Give the TransactionGenerator time to add transactions
                    blockchain.stopAcceptingMessages();

                    txCopy = new ArrayList<>(blockchain.getPendingTransactions());
                }

                String prevHash = (i == 0) ? "0" : blockchain.getLastBlock().getHash();

                List<Callable<Block>> callables = new ArrayList<>();
                for (int m = 0; m < 5; m++) {
                    int minerId = m + 1;
                    List<String> blockTx = new ArrayList<>(txCopy); // defensive copy per miner
                    callables.add(() -> new Block(prevHash, blockchain.getN(), minerId, blockTx));
                }

                long startTime = System.currentTimeMillis();
                Block block = miningExecutor.invokeAny(callables);
                long endTime = System.currentTimeMillis();

                if (blockchain.validateBlock(block)) {
                    blockchain.addBlock(block, endTime - startTime);
                    blockchain.rewardMiner("miner" + block.getMinerId());
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new RuntimeException("Mining interrupted", e);
        } finally {
            // Gracefully shutdown transaction executor
            transactionExecutor.shutdown();
            try {
                if (!transactionExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.out.println("Transaction executor didn't terminate in time. Forcing shutdown...");
                    transactionExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                transactionExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Gracefully shutdown mining executor
            miningExecutor.shutdown();
            try {
                if (!miningExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Mining executor didn't terminate in time. Forcing shutdown...");
                    miningExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                miningExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Print the final blockchain
            blockchain.printBlockChain();
        }
    }
}

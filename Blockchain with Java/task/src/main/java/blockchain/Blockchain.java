package blockchain;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blockchain {
    private static Blockchain INSTANCE = new Blockchain();
    private final double MAX_CREATION_TIME = 2.0;
    private final double MIN_CREATION_TIME = 0.002;
    private int id;
    private final List<Block> blockList;
    private final List<Transaction> pendingTransactions;
    private final Map<PublicKey, Client> registeredClients;
    private final Map<Integer, Miner> minerIdToMiner; // Added
    private int leadingZeros;
    private volatile boolean acceptingTransactions;

    private Blockchain() {
        id = 1;
        leadingZeros = 0;
        blockList = new ArrayList<>();
        pendingTransactions = new ArrayList<>();
        registeredClients = new HashMap<>();
        minerIdToMiner = new HashMap<>(); // Initialize
        acceptingTransactions = true;
    }

    public static Blockchain getInstance() {
        return INSTANCE;
    }

    public int getLeadingZeros() {
        return this.leadingZeros;
    }

    public int getId() {
        return id;
    }

    public void incrementId() {
        this.id++;
    }

    public synchronized List<Transaction> getPendingTransactions() {
        return this.pendingTransactions;
    }

    public synchronized void clearPendingTransactions() {
        pendingTransactions.clear();
    }

    public synchronized void startAcceptingTransactions() {
        acceptingTransactions = true;
    }

    public synchronized void stopAcceptingTransactions() {
        acceptingTransactions = false;
    }

    public boolean isAcceptingTransactions() {
        return acceptingTransactions;
    }

    public synchronized void registerClient(Client client) {
        PublicKey key = client.getPublicKey();
        if (!registeredClients.containsKey(key)) {
            registeredClients.put(key, client);
        }
    }

    // Register a miner and map its ID
    public synchronized void registerMiner(int id, Miner miner) {
        if (!minerIdToMiner.containsKey(id)) {
            minerIdToMiner.put(id, miner);
            registerClient(miner); // also as regular client
        }
    }

    public synchronized void addTransaction(Transaction tx) {
        if (!tx.isReward()) {
            if (!tx.isValid()) {
                System.out.println("Invalid signature — transaction rejected.");
                return;
            }

            long maxId = blockList.isEmpty() ? 0 : getLastBlock().getMaxTransactionId();
            if (tx.getId() <= maxId) {
                System.out.println("Transaction ID not strictly greater than last seen — transaction rejected.");
                return;
            }

            Client sender = registeredClients.get(tx.getSenderKey());
            Client receiver = registeredClients.get(tx.getReceiverKey());

            if (sender == null || receiver == null) {
                System.out.println("Unknown sender or receiver — transaction rejected.");
                return;
            }

            if (sender.getBalance() >= tx.getAmount()) {
                sender.subtractBalance(tx.getAmount());
                receiver.addBalance(tx.getAmount());
                pendingTransactions.add(tx);
            }
        } else {
            if (tx.getReceiverKey() == null) {
                System.out.println("Reward transaction missing receiver — rejected.");
                return;
            }

            Client receiver = registeredClients.get(tx.getReceiverKey());
            if (receiver == null) {
                System.out.println("Unknown reward recipient — transaction rejected.");
                return;
            }

            receiver.addBalance(tx.getAmount());
            pendingTransactions.add(tx);
        }
    }



    private synchronized void rewardMiner(int id) {
        Miner miner = minerIdToMiner.get(id);
        if (miner != null) {
            miner.reward();
        } else {
            System.out.println("Unknown miner ID — reward skipped.");
        }
    }

    private synchronized String adjustDifficulty(double creationTime) {
        if (creationTime > MAX_CREATION_TIME && leadingZeros > 0) {
            leadingZeros--;
            return "N was decreased by 1";
        } else if (creationTime > MIN_CREATION_TIME) {
            return "N stays the same";
        } else {
            leadingZeros++;
            return "N was increased to " + leadingZeros;
        }
    }


    public synchronized void addBlock(Block b, double creationTime) {
        if (validateBlock(b)) {
            blockList.add(b);

            String difficultyMessage = adjustDifficulty(creationTime);
            b.setDifficultyMessage(difficultyMessage);

            rewardMiner(b.getMinerId());

            incrementId();
        }
    }
    public synchronized boolean validateBlock(Block b) {
        Block last = getLastBlock();

        if (last == null) {
            if (!b.getPrevHash().equals("0") || leadingZeros != 0) return false;

            for (Transaction tx : b.getTransactions()) {
                if (!tx.isValid() || tx.getId() <= 0) return false;
            }
            return b.getHash().startsWith("0".repeat(leadingZeros));
        }

        if (!b.getPrevHash().equals(last.getHash()) || !b.getHash().startsWith("0".repeat(leadingZeros))) {
            return false;
        }

        long prevMaxId = last.getMaxTransactionId();
        for (Transaction tx : b.getTransactions()) {
            if (!tx.isValid() || tx.getId() <= prevMaxId)
                return false;
        }

        return true;
    }

    public synchronized Block getLastBlock() {
        if (blockList.isEmpty()) {
            return null;
        }
        return blockList.getLast();
    }

    public void printBlockChain() {
        blockList.forEach(Block::printBlock);
    }
}

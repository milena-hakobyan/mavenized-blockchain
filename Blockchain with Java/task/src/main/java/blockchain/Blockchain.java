package blockchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blockchain{
    //implementing Singleton pattern
    private static Blockchain INSTANCE;
    protected static int id;
    private final ArrayList<Block> list;
    private final List<Message> pendingMessages;
    private final List<String> pendingTransactions;
    private Map<String, Integer> userToCoinsMap;
    private int N;
    private volatile boolean acceptingMessages;

    public Blockchain (){
        id=0;
        N=0;
        list = new ArrayList<>();
        pendingMessages = new ArrayList<>();
        pendingTransactions = new ArrayList<>();
        userToCoinsMap = new HashMap<>();
        acceptingMessages = true;
    }

    public static Blockchain getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new Blockchain();
        }
        return INSTANCE;
    }

    public int getN(){return this.N;}


    public synchronized List<Message> getPendingMessages(){
        return this.pendingMessages;
    }

    public synchronized List<String> getPendingTransactions(){ return  this.pendingTransactions;}

    public synchronized void addPendingMessage(Message m) {
        //get the current maximum message ID in the blockchain
        long maxId = list.isEmpty() ? 0 : getLastBlock().getMaxMessageId();

        //only add the message if it's valid and the ID is strictly greater than the max
        if (verifyMessage(m) && m.getId() > maxId) {
            pendingMessages.add(m);
        } else {
            System.out.println("Rejected invalid message: " + m);
        }
    }

    public synchronized void addPendingTransaction(String s){
        pendingTransactions.add(s);
    }

    public void clearPendingMessages(){pendingMessages.clear();}

    public synchronized void clearPendingTransactions(){pendingTransactions.clear();}


    //these are utility methods that allow to be able to execute the minerExecutor and the messageExecutor in parallel
    public void startAcceptingMessages(){
        acceptingMessages = true;
    }

    public void stopAcceptingMessages(){
        acceptingMessages = false;
    }

    public boolean isAcceptingMessages(){
        return acceptingMessages;
    }


    public synchronized void addTransaction(String sender, String receiver, int amount){
        userToCoinsMap.putIfAbsent(sender, 100);
        userToCoinsMap.putIfAbsent(receiver, 100);

        int senderBalance = userToCoinsMap.get(sender);
        if(senderBalance >= amount){
            userToCoinsMap.put(sender, senderBalance - amount);
            userToCoinsMap.put(receiver, userToCoinsMap.get(receiver) + amount);
        }
    }

    public synchronized void rewardMiner(String miner){
        userToCoinsMap.putIfAbsent(miner, 100);
        userToCoinsMap.put(miner, userToCoinsMap.get(miner)+100);
    }


    //after the winner miner submits a block, blockchain should validate it
    // (prevHash is the hash of the current last and starts with N zeros)
    public synchronized void addBlock(Block b, long creationTime){
        if(validateBlock(b)) {
            list.add(b);
            b.setId(++Blockchain.id);
            if(creationTime > 60 && N > 0){
                N--;
                b.setDifficultyMessage("N was decreased by 1");
            }
            else if(creationTime > 5){
                b.setDifficultyMessage("N stays the same");
            }
            else {
                N++;
                b.setDifficultyMessage("N was increased to " + N);
            }
        }
    }

    public boolean verifyMessage(Message msg) {
        return msg.isValid();
    }

    public boolean validateBlock(Block b) {
        Block last = getLastBlock();

        if (last == null) {
            //must be the first block, prevHash should be "0", and difficulty N should be 0
            if (!b.getPrevHash().equals("0") || N != 0) return false;

            //also verify messages (ID > 0, signature valid)
//            for (Message m : b.getMessages()) {
//                if (!verifyMessage(m) || m.getId() <= 0) return false;
//            }
            return b.getHash().startsWith("0".repeat(N));
        }

        //a block's prevhash should match the current last block's hash,
        // and the block should start with the specified num of zeros
        if (!b.getPrevHash().equals(last.getHash()) || !b.getHash().startsWith("0".repeat(N))) {
            return false;
        }

//        long prevMaxId = last.getMaxMessageId();
//
//        //check messages: valid signature, and ID strictly greater than previous max
//        for (Message m : b.getMessages()) {
//            if (!verifyMessage(m) || m.getId() <= prevMaxId) {
//                return false;
//            }
//        }

        return true;
    }


    public Block getLastBlock() {
        if(list.isEmpty()){
            return null;
        }
        return list.get(list.size()-1);
    }


    public boolean validateBlockchain() {
        if (list.isEmpty()) return true;

        for (int i = 0; i < list.size(); i++) {
            Block block = list.get(i);

            if (i == 0) {
                // Genesis block: must have prevHash "0" and difficulty N = 0
                if (!block.getPrevHash().equals("0") || block.getId() != 1 || !block.getHash().startsWith("0".repeat(0))) {
                    return false;
                }
            } else {
                if (!validateBlock(block)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void printBlockChain(){
       list.stream().forEach(Block::printBlock);
    }
}
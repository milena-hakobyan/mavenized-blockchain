package blockchain;

import java.util.Date;
import java.util.List;

public class Block {
    private int id;
    private final int minerId;
    private final String hash; //the hash of a block is a hash of all fields of a block
    private final String prevHash;
    private final long timeStamp;//every block should contain a timestamp representing the time the block was created
    private int magic;
    private double generationTime;
    private final int numOfZeros;
    private List<Message> messages;
    private final List<String> transactions;
    private String difficultyMessage="";

    public Block(String prevBlockHash, int numOfZeros, int minerId, List<String> transactions){
        this.numOfZeros = numOfZeros;
        this.id = 0;
        this.prevHash = prevBlockHash;
        this.minerId = minerId;
        this.timeStamp = new Date().getTime();
        this.magic = 0;
        this.transactions = transactions;
        this.hash = generateHashCode();
    }

    public String getHash() {
        return hash;
    }

    public int getId() { return id;}

    public int getMinerId() { return minerId;}


    public String getPrevHash() { return prevHash;}

    public synchronized List<Message> getMessages(){return this.messages;}

    public long getTimeStamp() { return timeStamp;}

    public void setId(int id){  this.id = id;}

    public void setDifficultyMessage(String s){ difficultyMessage = s;}

    public String generateHashCode() {
        if(numOfZeros==0){
            long startTime = System.currentTimeMillis(); // Start timing

            String hash = StringUtil.applySha256(prevHash + this.id + this.timeStamp + magic + "miner" + minerId + " gets 100 VC");

            long endTime = System.currentTimeMillis(); // End timing
            generationTime = (endTime - startTime) / 1000.0;
            return hash;
        }
        String target = "0".repeat(numOfZeros); // e.g. "0000", if numZeros==4
        String temp = "";

        long startTime = System.currentTimeMillis(); // Start timing

        while (!temp.startsWith(target)) {
            temp = StringUtil.applySha256(prevHash + this.id + this.timeStamp + magic + "miner" + minerId + " gets 100 VC");
            magic++;
        }

        long endTime = System.currentTimeMillis(); // End timing
        generationTime = (endTime - startTime) / 1000.0;

        return temp;
    }

    public long getMaxMessageId() {
        return messages.stream()
                .mapToLong(Message::getId)
                .max()
                .orElse(0);
    }
    
    //printing a block with the given format
    protected void printBlock() {
        System.out.println("Block:");
        System.out.println("Created by: miner" + minerId);
        System.out.println("miner" + minerId + " gets 100 VC");
        System.out.println("Id: " + id);
        System.out.println("Timestamp: " + getTimeStamp());
        System.out.println("Magic number: " + magic);
        System.out.println("Hash of the previous block:\n" + getPrevHash());
        System.out.println("Hash of the block:\n" + getHash());
        System.out.println("Block data: ");
        transactions.forEach(System.out::println);
        System.out.println("Block was generating for " + generationTime + " seconds");
        System.out.println(difficultyMessage);
        System.out.println();
    }
}
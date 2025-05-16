package blockchain;

import java.security.*;
import java.util.Base64;

public class Client {
    protected static final int INITIAL_BALANCE = 100; // default balance for clients

    private final String name;
    private final KeyPair keyPair;
    private int balance;

    public Client(String name) {
        this.name = name;
        this.keyPair = generateKeyPair();
        this.balance = INITIAL_BALANCE;
    }

    public Client(String name, int initialBalance) {
        this.name = name;
        this.keyPair = generateKeyPair();
        this.balance = initialBalance;
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public byte[] sign(long id, PublicKey receiverKey, int amount) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update((id + Base64.getEncoder().encodeToString(receiverKey.getEncoded())
                + amount).getBytes());
        return signature.sign();
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }

    public int getBalance() {
        return balance;
    }

    public void addBalance(int amount) {
        this.balance += amount;
    }

    public void subtractBalance(int amount) {
        this.balance -= amount;
    }
}

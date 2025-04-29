package blockchain;

import java.security.*;

public class Client{
    private final String name;
    private final KeyPair keyPair;

    public Client(String name) {
        this.name = name;
        this.keyPair = generateKeyPair(); // Generate in memory
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }


    public byte[] sign(long id, String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update((id + message).getBytes()); // must match the `isValid` logic!
        return signature.sign();
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048); // 2048-bit key size is secure and common
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }
}
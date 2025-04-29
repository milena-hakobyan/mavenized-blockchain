package blockchain;

import java.security.PublicKey;
import java.security.Signature;

public class Message {
    private final long id;
    private final String sender;
    private final String text;
    private final PublicKey publicKey;
    private final byte[] signature;

    public Message(long id, String sender, String text, PublicKey publicKey, byte[] signature) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public boolean isValid() {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update((id + text).getBytes()); // sign both id and text!
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getSender() {
        return sender;
    }

    public byte[] getSignature() {
        return signature;
    }
}
package blockchain;

import java.security.*;
import java.util.Base64;

public class Transaction {
    private final long id;
    private final int amount;
    private final PublicKey senderKey;
    private final PublicKey receiverKey;
    private final byte[] signature;
    private final String text;
    private final boolean isReward;

    public Transaction(long id, int amount, PublicKey senderKey, PublicKey receiverKey, byte[] signature, String text, boolean isReward) {
        this.id = id;
        this.amount = amount;
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.signature = signature;
        this.text = text;
        this.isReward = isReward;
    }

    public boolean isValid() {
        if (isReward)
            return true;
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(senderKey);
            sig.update((id + Base64.getEncoder().encodeToString(receiverKey.getEncoded())
                    + amount).getBytes());
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }


    public long getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public String getText() {
        return text;
    }

    public PublicKey getReceiverKey() {
        return receiverKey;
    }

    public PublicKey getSenderKey() {
        return senderKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    public boolean isReward() {
        return isReward;
    }
}
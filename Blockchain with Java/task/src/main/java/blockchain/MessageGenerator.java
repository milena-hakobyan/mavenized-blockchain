package blockchain;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class MessageGenerator implements Runnable{
    private Blockchain blockchain = Blockchain.getInstance();
    private static final AtomicLong MESSAGE_ID = new AtomicLong();

    private final List<String> clientNames = List.of("Chill guy","Regina Phalange", "Elen","Milena","Michael Scarn");
    private final List<String> messages = List.of(
            "Hello everyone!",
            "How's it going?",
            "What's up??",
            "Can anyone see this?",
            "I love this chat!",
            "What are you mining?",
            "The fastest miner wins:D",
            "Let's gooo!",
            "Can't believe it's still Tuesday:'(",
            "Anyone up for a chat;)",
            "I think my computer's overheating... 🔥",
            "Who's winning the race? 🏁",
            "Is this thing on? Testing 1, 2, 3...",
            "When does the next block get mined? ⏳",
            "Just waiting for the next block... 😴",
            "Who needs a snack break during mining? 🍕",
            "Mining's harder than I thought! 😅",
            "I swear I'm faster than my internet connection!",
            "Blockchain or bust! 🚀",
            "Wanna join my mining pool? 🤝",
            "I think I found the magic number! 💎",
            "Hashing away at it... ⏳",
            "What are we mining for, anyway? 🧐",
            "It's a beautiful day to mine some blocks! 🌞",
            "I'm not saying I' mthe best miner, but... 😏"
    );

    @Override
    public void run() {
        Random random = new Random();
        while (blockchain.isAcceptingMessages()) {
            try {
                Thread.sleep(100); //generate a new message every 200ms
                Client client = new Client(clientNames.get(random.nextInt(clientNames.size())));
                String msg = messages.get(random.nextInt(messages.size()));
                long id = MESSAGE_ID.incrementAndGet();
                byte[] signature = client.sign(id, msg);
                Message message = new Message(id, client.getName(), msg, client.getPublicKey(), signature);
                blockchain.addPendingMessage(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //good practice
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
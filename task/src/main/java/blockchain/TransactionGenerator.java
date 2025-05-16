package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class TransactionGenerator implements Runnable {
    Blockchain blockchain = Blockchain.getInstance();
    protected static final AtomicLong TRANSACTION_ID = new AtomicLong();

    List<String> people = List.of("Alice", "Bob", "Charlie", "David", "Eve");
    List<String> institutions = List.of(
            "CarShop", "BeautyShop", "FastFood", "ShoeStore", "ElectronicsMart",
            "GamingShop", "CoffeeCorner", "BookHouse", "FurnitureDepot", "ClothingStore",
            "PharmacyPlus", "ToyPlanet", "GymZone", "CinemaCity", "PetStore",
            "MusicWorld", "TravelAgency", "BakeryBites", "FlowerShop", "HardwareHub"
    );
    List<String> miners = List.of("miner1", "miner2", "miner3", "miner4", "miner5");
    // Combine all possible senders/receivers:
    private final List<Client> clients = new ArrayList<>();

    public TransactionGenerator() {
        // Register regular clients (people + institutions)
        Stream.of(people, institutions)
                .flatMap(List::stream)
                .map(Client::new)
                .forEach(client -> {
                    clients.add(client);
                    blockchain.registerClient(client);
                });
        // Register miners separately with IDs (1 to 5)
        for (int i = 0; i < miners.size(); i++) {
            String name = miners.get(i);
            Miner miner = new Miner(name);
            clients.add(miner);
            blockchain.registerMiner(i + 1, miner);  // Miner ID = i + 1
        }
    }


    @Override
    public void run() {
        while (blockchain.isAcceptingTransactions()) {
            Random random = new Random();
            try {
                Thread.sleep(100); //generate a new transaction every 100ms


                Client sender = clients.get(random.nextInt(clients.size()));
                Client receiver = clients.get(random.nextInt(clients.size()));


                long id = TRANSACTION_ID.incrementAndGet();
                int amount = random.nextInt(101);
                byte[] signature = sender.sign(id, receiver.getPublicKey(), amount);
                String txText = sender.getName() + " sent " + amount + "VC to " + receiver.getName();
                Transaction transaction = new Transaction(id, amount, sender.getPublicKey(), receiver.getPublicKey(), signature, txText, false);

                if (!sender.getPublicKey().equals(receiver.getPublicKey())) {
                    blockchain.addTransaction(transaction);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}

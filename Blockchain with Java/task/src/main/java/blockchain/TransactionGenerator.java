package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionGenerator implements Runnable{
    Blockchain blockchain = Blockchain.getInstance();
    List<String> people = List.of("Alice", "Bob", "Charlie", "David", "Eve");
    List<String> institutions = List.of(
            "CarShop", "BeautyShop", "FastFood", "ShoeStore", "ElectronicsMart",
            "GamingShop", "CoffeeCorner", "BookHouse", "FurnitureDepot", "ClothingStore",
            "PharmacyPlus", "ToyPlanet", "GymZone", "CinemaCity", "PetStore",
            "MusicWorld", "TravelAgency", "BakeryBites", "FlowerShop", "HardwareHub"
    );
    List<String> miners = List.of("miner1", "miner2", "miner3", "miner4", "miner5");
    // Combine all possible senders/receivers:
    List<String> allEntities = Stream.of(people, institutions, miners)
            .flatMap(List::stream)
            .collect(Collectors.toList());


    @Override
    public void run() {
        while(blockchain.isAcceptingMessages()) {
            Random random = new Random();
            try {
                Thread.sleep(200); //generate a new message every 200ms
                String sender = allEntities.get(random.nextInt(allEntities.size()));
                String receiver = allEntities.get(random.nextInt(allEntities.size()));
                int amount = random.nextInt(101);
                if (!sender.equals(receiver)) {
                    blockchain.addTransaction(sender, receiver, amount);
                    blockchain.addPendingTransaction(sender + " sent " + amount + "VC to " + receiver);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}

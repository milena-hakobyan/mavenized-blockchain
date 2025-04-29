package blockchain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
       BlockchainSimulation blockchainSimulation = new BlockchainSimulation();
       blockchainSimulation.startMining();
    }
}
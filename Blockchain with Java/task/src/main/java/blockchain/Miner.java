package blockchain;

public class Miner extends Client {
    public Miner(String name) {
        super(name);
    }

    public void reward() {
        this.addBalance(INITIAL_BALANCE);
    }
}

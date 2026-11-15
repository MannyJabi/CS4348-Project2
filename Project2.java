
public class Project2 {

    // Represents a bank teller
    static class Teller extends Thread {

        private final int tellerId;

        // Assign an ID to each teller
        public Teller(int id) {
            tellerId = id;
        }

        // Run the teller thread
        @Override
        public void run() {
            System.out.println("Teller " + tellerId + " []: ready to serve");
        }
    }

    public static void main(String[] args) {

        // Start the bank simulation
        System.out.println("Bank simulation starting...");

        // Create three teller threads
        Teller[] tellers = new Teller[3];

        // Start each teller
        for (int i = 0; i < 3; i++) {
            tellers[i] = new Teller(i);
            tellers[i].start();
        }

        // Wait for all tellers to finish
        for (int i = 0; i < 3; i++) {
            try {
                tellers[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

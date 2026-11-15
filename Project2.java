
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

    // Represents a bank customer
    static class Customer extends Thread {

        private final int customerId;

        // Assign an ID to each customer
        public Customer(int id) {
            customerId = id;
        }

        // Run the customer thread
        @Override
        public void run() {
            System.out.println("Customer " + customerId + " []: arrives at the bank");
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

        // Create 50 customer threads
        Customer[] customers = new Customer[50];

        // Start each customer
        for (int i = 0; i < 50; i++) {
            customers[i] = new Customer(i);
            customers[i].start();
        }

        // Wait for all customers to finish
        for (int i = 0; i < 50; i++) {
            try {
                customers[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
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

        // End the bank simulation
        System.out.println("Bank simulation finished.");
    }
}

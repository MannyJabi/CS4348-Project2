
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Project2 {

    // Only two customers can use the door at once
    static Semaphore door = new Semaphore(2);

    // Wait until all three tellers are ready
    static Semaphore bankOpen = new Semaphore(0);

    // Represents a bank teller
    static class Teller extends Thread {

        private final int tellerId;

        // Assign an ID to each teller
        public Teller(int id) {
            tellerId = id;
        }

        @Override
        public void run() {
            System.out.println("Teller " + tellerId + " []: ready to serve");

            // Signal that this teller is ready
            bankOpen.release();
        }
    }

    // Represents a bank customer
    static class Customer extends Thread {

        private final int customerId;

        // Assign an ID to each customer
        public Customer(int id) {
            customerId = id;
        }

        @Override
        public void run() {
            try {
                // Wait a random amount of time
                int delay = ThreadLocalRandom.current().nextInt(101);
                System.out.println("Customer " + customerId + " []: going to bank");
                Thread.sleep(delay);

                // Wait for space at the door
                door.acquire();
                try {
                    System.out.println("Customer " + customerId + " []: entering bank");
                } finally {
                    // Free the door for another customer
                    door.release();
                }

                System.out.println("Customer " + customerId + " []: getting in line");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("Bank simulation starting...");

        // Create and start three tellers
        Teller[] tellers = new Teller[3];

        for (int i = 0; i < 3; i++) {
            tellers[i] = new Teller(i);
            tellers[i].start();
        }

        try {
            // Wait for all three tellers
            bankOpen.acquire(3);
            System.out.println("Bank is now open.");

            // Create and start 50 customers
            Customer[] customers = new Customer[50];

            for (int i = 0; i < 50; i++) {
                customers[i] = new Customer(i);
                customers[i].start();
            }

            // Wait for all customers
            for (int i = 0; i < 50; i++) {
                customers[i].join();
            }

            // Wait for all tellers
            for (int i = 0; i < 3; i++) {
                tellers[i].join();
            }

            System.out.println("Bank simulation finished.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

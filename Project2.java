
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Project2 {

    // Only two customers can use the door at once
    static Semaphore door = new Semaphore(2);

    // Wait until all three tellers are ready
    static Semaphore bankOpen = new Semaphore(0);

    // Line of customers waiting for a teller
    static BlockingQueue<Customer> customerLine = new LinkedBlockingQueue<>();

    // Represents a bank teller
    static class Teller extends Thread {

        private final int tellerId;

        public Teller(int id) {
            tellerId = id;
        }

        @Override
        public void run() {
            System.out.println("Teller " + tellerId + " []: ready to serve");

            // Signal that the teller is ready
            bankOpen.release();

            // Serve customers until the bank closes
            while (true) {
                try {
                    System.out.println("Teller " + tellerId + " []: waiting for a customer");

                    // Wait for the next customer
                    Customer customer = customerLine.take();

                    // Stop when there are no more customers
                    if (customer.customerId == -1) {
                        break;
                    }

                    // Assign this teller to the customer
                    customer.tellerId = tellerId;

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: serving a customer");

                    // Tell the customer which teller is available
                    customer.tellerReady.release();

                    // Wait for the customer to introduce themselves
                    customer.customerIntroduced.acquire();

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: asks for transaction");

                    // Transaction processing will be added later
                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: finished serving customer");

                    // Tell the customer that service is finished
                    customer.transactionFinished.release();

                    // Wait for the customer to leave
                    customer.customerLeft.acquire();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            System.out.println("Teller " + tellerId + " []: finished for the day");
        }
    }

    // Represents a bank customer
    static class Customer extends Thread {

        private final int customerId;
        private int tellerId;

        // Semaphores for communication with the teller
        Semaphore tellerReady = new Semaphore(0);
        Semaphore customerIntroduced = new Semaphore(0);
        Semaphore transactionFinished = new Semaphore(0);
        Semaphore customerLeft = new Semaphore(0);

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

                // Enter through the door
                door.acquire();
                try {
                    System.out.println("Customer " + customerId + " []: entering bank");
                } finally {
                    door.release();
                }

                System.out.println("Customer " + customerId + " []: getting in line");

                // Join the customer line
                customerLine.put(this);

                // Wait for an available teller
                tellerReady.acquire();

                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: selects teller");

                // Introduce the customer to the teller
                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: introduces itself");

                customerIntroduced.release();

                // Wait for the teller to finish
                transactionFinished.acquire();

                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: leaves teller");

                // Leave through the door
                System.out.println("Customer " + customerId + " []: goes to door");

                door.acquire();
                try {
                    System.out.println("Customer " + customerId + " []: leaves the bank");
                } finally {
                    door.release();
                }

                // Tell the teller the customer has left
                customerLeft.release();

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
            // Wait for all three tellers to be ready
            bankOpen.acquire(3);

            System.out.println("Bank is now open.");

            // Create and start 50 customers
            Customer[] customers = new Customer[50];

            for (int i = 0; i < 50; i++) {
                customers[i] = new Customer(i);
                customers[i].start();
            }

            // Wait for all customers to finish
            for (int i = 0; i < 50; i++) {
                customers[i].join();
            }

            // Send stop signals to the tellers
            for (int i = 0; i < 3; i++) {
                customerLine.put(new Customer(-1));
            }

            // Wait for all tellers to finish
            for (int i = 0; i < 3; i++) {
                tellers[i].join();
            }

            System.out.println("Bank simulation finished.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

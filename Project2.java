
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Project2 {

    // Shared bank resources
    static Semaphore door = new Semaphore(2);
    static Semaphore bankOpen = new Semaphore(0);
    static Semaphore manager = new Semaphore(1);
    static Semaphore safe = new Semaphore(2);

    // Customers waiting for a teller
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
            bankOpen.release();

            try {
                while (true) {
                    System.out.println("Teller " + tellerId
                            + " []: waiting for a customer");

                    // Get the next customer
                    Customer customer = customerLine.take();

                    // Stop when the bank closes
                    if (customer.customerId == -1) {
                        break;
                    }

                    customer.tellerId = tellerId;

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: serving a customer");

                    // Call the customer
                    customer.tellerReady.release();

                    // Wait for the introduction
                    customer.customerIntroduced.acquire();

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: asks for transaction");

                    // Ask the customer for the transaction
                    customer.transactionRequested.release();

                    // Wait for the customer's answer
                    customer.transactionGiven.acquire();

                    String type = customer.withdrawal
                            ? "withdrawal" : "deposit";

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: handling " + type + " transaction");

                    // Withdrawals need manager permission
                    if (customer.withdrawal) {
                        System.out.println("Teller " + tellerId
                                + " [Customer " + customer.customerId
                                + "]: going to the manager");

                        manager.acquire();

                        try {
                            System.out.println("Teller " + tellerId
                                    + " [Customer " + customer.customerId
                                    + "]: getting manager's permission");

                            Thread.sleep(
                                    ThreadLocalRandom.current().nextInt(5, 31)
                            );

                            System.out.println("Teller " + tellerId
                                    + " [Customer " + customer.customerId
                                    + "]: got manager's permission");

                        } finally {
                            manager.release();
                        }
                    }

                    // All transactions use the safe
                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: going to safe");

                    safe.acquire();

                    try {
                        System.out.println("Teller " + tellerId
                                + " [Customer " + customer.customerId
                                + "]: enter safe");

                        Thread.sleep(
                                ThreadLocalRandom.current().nextInt(10, 51)
                        );

                        System.out.println("Teller " + tellerId
                                + " [Customer " + customer.customerId
                                + "]: leaving safe");

                    } finally {
                        safe.release();
                    }

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: finishes " + type + " transaction");

                    System.out.println("Teller " + tellerId
                            + " [Customer " + customer.customerId
                            + "]: wait for customer to leave");

                    // Let the customer leave
                    customer.transactionFinished.release();

                    // Wait for the customer to leave
                    customer.customerLeft.acquire();
                }

                System.out.println("Teller " + tellerId
                        + " []: finished for the day");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Represents a bank customer
    static class Customer extends Thread {

        private final int customerId;
        private int tellerId;
        private boolean withdrawal;

        // Customer and teller communication
        Semaphore tellerReady = new Semaphore(0);
        Semaphore customerIntroduced = new Semaphore(0);
        Semaphore transactionRequested = new Semaphore(0);
        Semaphore transactionGiven = new Semaphore(0);
        Semaphore transactionFinished = new Semaphore(0);
        Semaphore customerLeft = new Semaphore(0);

        public Customer(int id) {
            customerId = id;
        }

        @Override
        public void run() {
            try {
                // Choose a random transaction
                withdrawal = ThreadLocalRandom.current().nextBoolean();

                int delay = ThreadLocalRandom.current().nextInt(101);

                System.out.println("Customer " + customerId
                        + " []: going to bank");

                Thread.sleep(delay);

                // Enter the bank
                door.acquire();

                try {
                    System.out.println("Customer " + customerId
                            + " []: entering bank");
                } finally {
                    door.release();
                }

                System.out.println("Customer " + customerId
                        + " []: getting in line");

                // Join the line
                customerLine.put(this);

                // Wait for an available teller
                tellerReady.acquire();

                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: selects teller");

                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: introduces itself");

                customerIntroduced.release();

                // Wait for the teller to ask
                transactionRequested.acquire();

                String type = withdrawal ? "withdrawal" : "deposit";

                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: asks for " + type + " transaction");

                // Give the transaction to the teller
                transactionGiven.release();

                // Wait for the transaction to finish
                transactionFinished.acquire();

                System.out.println("Customer " + customerId
                        + " [Teller " + tellerId
                        + "]: leaves teller");

                System.out.println("Customer " + customerId
                        + " []: goes to door");

                // Leave through the door
                door.acquire();

                try {
                    System.out.println("Customer " + customerId
                            + " []: leaves the bank");
                } finally {
                    door.release();
                }

                // Tell the teller the customer left
                customerLeft.release();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("Bank simulation starting...");

        // Start three tellers
        Teller[] tellers = new Teller[3];

        for (int i = 0; i < 3; i++) {
            tellers[i] = new Teller(i);
            tellers[i].start();
        }

        try {
            // Wait until all tellers are ready
            bankOpen.acquire(3);

            System.out.println("Bank is now open.");

            // Start 50 customers
            Customer[] customers = new Customer[50];

            for (int i = 0; i < 50; i++) {
                customers[i] = new Customer(i);
                customers[i].start();
            }

            // Wait for all customers to leave
            for (int i = 0; i < 50; i++) {
                customers[i].join();
            }

            // Stop all three tellers
            for (int i = 0; i < 3; i++) {
                customerLine.put(new Customer(-1));
            }

            // Wait for tellers to finish
            for (int i = 0; i < 3; i++) {
                tellers[i].join();
            }

            System.out.println("Bank simulation finished.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

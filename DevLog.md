# CS 4348 Project 2 - Development Log

## Session 1 - November 15, 2026, 12:37PM

### Beginning of Session

I reviewed the project instructions and the sample output.
The project requires simulating a bank with three tellers
and 50 customers using Java threads and semaphores.

My goal for this session is to set up the Java project
and begin implementing the basic thread structure.


11-15-2026 12:46PM

I created the Teller class using Java threads. Each teller has its own ID so I can identify them in the output. I used a loop to start all three teller threads and added join() so the main program waits for them to finish.

I compiled Project2.java and ran the program to test the teller threads. All three tellers started and printed their ready messages.

What I learned: Each teller runs on its own thread, so the order of the messages can change each time the program runs. Using join() makes sure the main program waits for all the tellers to finish.

Next: create the 50 customer threads and start adding semaphores to control access to the bank.

11-15-2026 1:11PM

I added the Customer class to Project2.java. Each customer has its own ID, and I used a loop to create and start 50 customer threads. I also added join() so the main program waits for all the customers to finish.

I compiled the program using javac Project2.java and ran it with java Project2. The program compiled without errors and printed arrival messages for all 50 customers, along with the ready messages for the three tellers. The customer messages appeared in a random order, with Customer 46 printing first. The program ended with "Bank simulation finished."

What I learned: Java threads do not always execute in the order they are started. Each customer runs separately, and join() makes sure the main program waits for all the threads to finish.

Next: add semaphores to control how many customers can enter the bank and access shared resources.


11-15-2026 1:25PM

I added semaphores to control when the bank opens and how customers enter. I used a semaphore with two permits for the door so only two customers can use it at the same time. I also added another semaphore to make sure all three tellers are ready before the bank opens.

I ran the program using java Project2. All three tellers printed their ready messages before "Bank is now open." appeared. All 50 customers entered the bank and got in line. The customer messages appeared in different orders, and the program finished without errors.

What I learned: Semaphores can control access to shared resources. The acquire() method waits for a permit, and release() returns it so another thread can use the resource.

Next: connect customers to available tellers and add synchronization so customers wait for their transactions to finish before leaving.
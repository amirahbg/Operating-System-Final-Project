
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 * <p>
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */
public class Server {
    private List<ClientResourceManager> currentClients;
    private Semaphore clientsLock;
    private Semaphore[] resourceLock;
    AtomicInteger clientNumber;

    public Server() {
        clientNumber = new AtomicInteger(0);
        clientsLock = new Semaphore(ConstantValue.N_CLIENTS);
        currentClients = Collections.synchronizedList(new ArrayList<ClientResourceManager>());

        resourceLock = new Semaphore[ConstantValue.N_RESOURCES];
        for (int i = 0; i < ConstantValue.N_RESOURCES; i++) {
            resourceLock[i] = new Semaphore(ConstantValue.MUTEX_LOCK);
        }
    }


    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.begin();
    }

    private void begin() {
        System.out.println("The capitalization server is running...");
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(9898);
            while (true) {
                clientsLock.acquire();
                // TODO: notify client that the server is busy
                clientNumber.addAndGet(1);

                new Capitalizer(listener.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private class Capitalizer extends Thread {
        private Socket socket;
        private int clientNo;
        private ClientResourceManager client;

        public Capitalizer(Socket socket) {
            this.socket = socket;
            clientNo = clientNumber.get();
            client = new ClientResourceManager(clientNo, new ArrayList<>());
            log("New connection with client# " + clientNo + " at " + socket);
            currentClients.add(client);
            Log.logResourceInfo(clientNumber.get(), currentClients);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNo + ".");
                out.println("Enter a line with only a period to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    updateResource(input);
                    out.println(input.toUpperCase());
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNo + ": " + e);
            } finally {
                try {
                    socket.close();
                    clientsLock.release();
                    log("Connection with client# " + clientNo + " closed");
                    deallocateResource(client);
                    clientNumber.addAndGet(-1);
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
            }
        }

        private void deallocateResource(ClientResourceManager client) {
            for (int i: client.getAllocatedResources()) {
                resourceLock[i].release(1);
            }
        }

        private void updateResource(String input) {
            int i = input.indexOf('1');
            int j = input.indexOf('2');
            while (i >= 0 || j >= 0) {
                try {
                    if (j >= 0) {
                        resourceLock[j].release(ConstantValue.MUTEX_LOCK);
                        client.deallocateResource(j);
                    }
                    if (i >= 0) {
                        resourceLock[i].acquire();
                        client.addResource(i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                j = input.indexOf('2', j + 1);
                i = input.indexOf('1', i + 1);
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }

}
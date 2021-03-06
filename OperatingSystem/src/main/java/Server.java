
import ch.petikoch.libs.jtwfg.DeadlockAnalysisResult;
import ch.petikoch.libs.jtwfg.DeadlockCycle;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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
    private Semaphore mutexLock;
    private Semaphore[] resourceLock;
    AtomicInteger clientNumber;
    private DeadlockHandler deadlockHandler;

    public Server() {
        ArrayList<Integer> setting = (new SettingFile(ConstantValue.CONF_PATH)).getSetting();
        ConstantValue.init(setting);
        clientNumber = new AtomicInteger(0);
        clientsLock = new Semaphore(ConstantValue.N_CLIENTS);
        mutexLock = new Semaphore(ConstantValue.MUTEX_LOCK);
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
            listener = new ServerSocket(ConstantValue.PORT_NUMBER);

            while (true) {
                clientsLock.acquire();
                // TODO: notify client that the server is busy
                clientNumber.addAndGet(1);

                new RequestThread(listener.accept()).start();
                if (ConstantValue.DEADLOCK_MODE == 2) {
                    deadlockHandler = new DeadlockHandler(currentClients);
                    deadlockHandler.start();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                assert listener != null;
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
    private class RequestThread extends Thread {
        private Socket socket;
        private int clientNo;
        private ClientResourceManager client;
        private BufferedReader in;
        private PrintWriter out;

        RequestThread(Socket socket) {
            this.socket = socket;
            clientNo = clientNumber.get();
            client = new ClientResourceManager(clientNo);
            log("New connection with client# " + clientNo + " at " + socket);
            currentClients.add(client);
            Log.logResourceInfoAllOfActive(clientNumber.get(), currentClients);

        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        @Override
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNo + ".");
                out.println("Enter a line with only a period to quit\n");
                new LogThread(out, client).start();
                if (ConstantValue.DEADLOCK_MODE == 2) {
                    deadlockHandler.addWriter(out, clientNo);
                    deadlockHandler.addSocket(socket, clientNo);
                }
                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    updateResource(input);
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNo + ": " + e);
            } finally {
                try {
                    socket.close();
                    clientsLock.release();
                    log("Connection with client# " + clientNo + " closed");
                    deallocateResource(client);
                    currentClients.remove(client);
                    clientNumber.addAndGet(-1);
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
            }
        }

        private void deallocateResource(ClientResourceManager client) {
            for (int i : client.getAllocatedResources()) {
                resourceLock[i].release(1);
            }
        }

        private void updateResource(String input) {
            DeadlockChecker deadlockDetector = new DeadlockChecker();
//            deadlockDetector.getSystemStatus((List<ClientResourceManager>) currentClients);

            int i = input.indexOf('1');
            int j = input.indexOf('2');
            while (i >= 0 || j >= 0) {
                try {
                    if (j >= 0) {
                        resourceLock[j].release(ConstantValue.MUTEX_LOCK);
                        client.deallocateResource(j);
                    }
                    if (i >= 0) {
                        DeadlockAnalysisResult res = null;
                        if (resourceLock[i].availablePermits() == 0) {
                            client.addToWaitList(i);
                            if (ConstantValue.DEADLOCK_MODE == 1) {
                                res = deadlockDetector.getSystemStatusWithRequest(currentClients
                                        , i, clientNo);
                                if (res.hasDeadlock()) {
                                    out.println(Log.deadlockReport(res,
                                            ConstantValue.DEADLOCK_MODE));
                                }
                            } else if (ConstantValue.DEADLOCK_MODE == 2) {
                                // deadlock handler will take care of this
                            } else if (ConstantValue.DEADLOCK_MODE == 3) {
                                res = deadlockDetector.getSystemStatus(currentClients);
                                if (res.hasDeadlock()) {
                                    out.println(Log.deadlockReport(res,
                                            ConstantValue.DEADLOCK_MODE));

                                }
                            }
                        }
                        if (res == null) {
                            resourceLock[i].acquire();
                            client.removeFromWaitList(i);
                            client.addResource(i);
                            out.println("Accepted Request for resource: R" + String.valueOf(i));
                            // TODO: assure that the input has ConstantValue.N_RESOURCES digit
                        } else {
                            if (!res.hasDeadlock()) {
                                out.println("Wait For Resource: R" + String.valueOf(i));
                                resourceLock[i].acquire();
                                client.removeFromWaitList(i);
                                client.addResource(i);
                                out.println("Accepted Request for resource: R" + String.valueOf(i));
                            }
                            if (res.hasDeadlock() && ConstantValue.DEADLOCK_MODE != 1) {
                                resourceLock[i].acquire();
                                client.removeFromWaitList(i);
                                client.addResource(i);
                                out.println("Accepted Request for resource: R" + String.valueOf(i));
                            }
                            if (res.hasDeadlock() && ConstantValue.DEADLOCK_MODE == 1) {
                                out.println("Denied Request for resource: R" + String.valueOf(i));
                            }
                        }
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

    private class LogThread extends Thread {
        PrintWriter writer;
        ClientResourceManager client;

        private LogThread(PrintWriter writer, ClientResourceManager client) {
            this.writer = writer;
            this.client = client;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(ConstantValue.NOTIFY_TIME_ELAPSE);
                    if (writer != null)
                        writer.println(Log.logResourceInfoOfAClient(client));
//                    System.out.println(getName() + ": " + Log.logResourceInfoOfAClient(client));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * this class make a new thread to check if deadlock happened. this class will instantiate
     * when DEADLOCK_MODE == 2
     */
    private class DeadlockHandler extends Thread {
        List<ClientResourceManager> currentClients;
        HashMap<Integer, PrintWriter> writers;
        HashMap<Integer, Socket> sockets;

        private DeadlockHandler(List<ClientResourceManager> currentClients) {
            this.currentClients = currentClients;
            this.writers = new HashMap<>();
            this.sockets = new HashMap<>();
        }

        private void addWriter(PrintWriter writer, Integer clientNo) {
            writers.put(clientNo, writer);
        }

        private void addSocket(Socket socket, Integer clientNo) {
            sockets.put(clientNo, socket);
        }

        @Override
        public void run() {
            DeadlockAnalysisResult res;
            DeadlockChecker deadlockChecker = new DeadlockChecker();
            res = deadlockChecker.getSystemStatus(currentClients);

            while (true) {
                if (writers.isEmpty() || currentClients.isEmpty())
                    continue;
                res = deadlockChecker.getSystemStatus(currentClients);
                if (res.hasDeadlock()) {
                    Set<DeadlockCycle> cycles = res.getDeadlockCycles();
                    try {
                        mutexLock.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (DeadlockCycle cycle : cycles) {
                        for (ClientResourceManager client : currentClients) {
                            if (cycle.isDeadlocked("P" + client.getClientNumber())) {
                                Socket socket = sockets.get(client.getClientNumber());
                                PrintWriter writer = writers.get(client.getClientNumber());
                                try {
                                    writer.println("Your Connection with server has been closed because of a Deadlock");
                                    clientsLock.release();
                                    for (int i : client.getAllocatedResources()) {
                                        resourceLock[i].release(1);
                                    }
                                    currentClients.remove(client);
                                    clientNumber.addAndGet(-1);
                                    socket.close();
                                    System.out.println("Connection with client# " + client.getClientNumber() + " closed");

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    mutexLock.release();
                }
                try {
                    Thread.sleep(ConstantValue.DEADLOCK_HANDLER_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
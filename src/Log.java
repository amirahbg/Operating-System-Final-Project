import java.util.List;

public class Log {
    private Log() {
    }

    public static void logResourceInfoAllOfActive(
            int clientNumber, List<ClientResourceManager> clients) {
        System.out.println(new StringBuilder("Number Of Clients: ")
                .append(clientNumber));
        System.out.println(new StringBuilder("Client's Resources: ")
                .append(clients.toString()));
    }

    public static String logResourceInfoOfAClient(ClientResourceManager client) {
        return client.toString();
    }
}

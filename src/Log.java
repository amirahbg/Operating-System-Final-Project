import java.util.List;

class Log {
    private Log() {
    }

    static void logResourceInfoAllOfActive(
            int clientNumber, List<ClientResourceManager> clients) {
        System.out.println(new StringBuilder("Number Of Clients: ")
                .append(clientNumber));
        System.out.println(new StringBuilder("Client's Resources: ")
                .append(clients.toString()));
    }

    static String logResourceInfoOfAClient(ClientResourceManager client) {
        return "Allocated Resources: " + client.toString();
    }
}

import java.util.ArrayList;
import java.util.List;

public class Log {
    private Log() {
    }

    public static void logResourceInfo(
            int clientNumber, List<ClientResourceManager> clients) {
        System.out.println(new StringBuilder("ClientNumber: ")
                .append(clientNumber));
        System.out.println(new StringBuilder("Client's Resources: ")
                .append(clients.toString()));
    }
}

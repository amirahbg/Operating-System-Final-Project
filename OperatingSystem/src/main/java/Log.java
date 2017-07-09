import ch.petikoch.libs.jtwfg.DeadlockAnalysisResult;

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

    static String deadlockReport(DeadlockAnalysisResult deadlockReport, int deadlockMode) {
        if (deadlockMode == 1) {
            return "Your request has been discarded, because it will end up with Deadlock";

        } else if (deadlockMode == 2) {
            return "";
        }
        else if (deadlockMode == 3) {
            return "Encountered with Deadlock: " +
                    deadlockReport.getDeadlockCycles();
        }
        return null;
    }
}

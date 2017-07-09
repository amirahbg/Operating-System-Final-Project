import ch.petikoch.libs.jtrag.GraphBuilder;
import ch.petikoch.libs.jtwfg.DeadlockAnalysisResult;
import ch.petikoch.libs.jtwfg.DeadlockDetector;
import ch.petikoch.libs.jtwfg.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by amiiir on 7/9/17.
 */
public class DeadlockHandler {
    int deadlockMode;

    DeadlockHandler(int deadlockMode) {
        this.deadlockMode = deadlockMode;
    }

    /**
     * @return true denotes that the system is in the safe state
     * and false denotes that the system is in the unsafe state
     * @param currentClients
     */
    boolean getSystemStatus(List<ClientResourceManager> currentClients) {
        GraphBuilder builder = new GraphBuilder();
        for (ClientResourceManager client : currentClients) {
            ArrayList<Integer> clientResources = client.getAllocatedResources();
            for (Integer i : clientResources) {
                builder.addResource2TaskAssignment(
                        "r" + i.toString(),
                        "c" + String.valueOf(client.getClientNumber()));
            }
            ArrayList<Integer> waitList = client.getWaitForResources();
            for (Integer i : waitList) {
                builder.addTask2ResourceDependency(
                        "c" + String.valueOf(client.getClientNumber()),
                        "r" + i.toString());
            }
        }
        Graph graph = builder.build();

        DeadlockDetector deadlockDetector = new DeadlockDetector();
        DeadlockAnalysisResult analyzisResult = deadlockDetector.analyze(graph);
        if (analyzisResult.hasDeadlock()) {
            System.out.println(analyzisResult);
        }

        return analyzisResult.hasDeadlock();
    }
}

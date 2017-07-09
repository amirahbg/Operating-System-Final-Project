import ch.petikoch.libs.jtrag.GraphBuilder;
import ch.petikoch.libs.jtwfg.DeadlockAnalysisResult;
import ch.petikoch.libs.jtwfg.DeadlockDetector;
import ch.petikoch.libs.jtwfg.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amiiir on 7/9/17.
 */
public class DeadlockHandler {
    int deadlockMode;

    DeadlockHandler(int deadlockMode) {
        this.deadlockMode = deadlockMode;
    }


    DeadlockAnalysisResult getSystemStatus(List<ClientResourceManager> currentClients) {
        GraphBuilder builder = new GraphBuilder();
        for (ClientResourceManager client : currentClients) {
            ArrayList<Integer> clientResources = client.getAllocatedResources();
            for (Integer i : clientResources) {
                builder.addResource2TaskAssignment(
                        "R" + i.toString(),
                        "P" + String.valueOf(client.getClientNumber()));
            }
            ArrayList<Integer> waitList = client.getWaitList();
            for (Integer i : waitList) {
                builder.addTask2ResourceDependency(
                        "P" + String.valueOf(client.getClientNumber()),
                        "R" + i.toString());
            }
        }
        Graph graph = builder.build();

        DeadlockDetector deadlockDetector = new DeadlockDetector();
        DeadlockAnalysisResult analyzisResult = deadlockDetector.analyze(graph);

        return analyzisResult;
    }
}

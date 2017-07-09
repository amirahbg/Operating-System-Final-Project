import ch.petikoch.libs.jtrag.GraphBuilder;
import ch.petikoch.libs.jtwfg.DeadlockAnalysisResult;
import ch.petikoch.libs.jtwfg.DeadlockDetector;
import ch.petikoch.libs.jtwfg.Graph;

import java.util.ArrayList;

/**
 * Created by amiiir on 7/7/17.
 */
public class Util {
    public static void main(String[] args) {
        Util util = new Util();
        util.begin();
    }

    private void begin() {

        GraphBuilder builder = new GraphBuilder();
        builder.addTask2ResourceDependency(1, 2);
        builder.addResource2TaskAssignment(2, 3);
        builder.addTask2ResourceDependency(3, 1);
        builder.addResource2TaskAssignment("resource 2", "task 1");
        builder.addTask("task 3");
        builder.addTask("task 4");
        builder.addResource("resource 3");
        builder.addResource("resource 4");
        Graph graph = builder.build();

        DeadlockDetector deadlockDetector = new DeadlockDetector();
        DeadlockAnalysisResult analyzisResult = deadlockDetector.analyze(graph);
        if(analyzisResult.hasDeadlock()){
             System.out.println(analyzisResult);
        }
    }
}

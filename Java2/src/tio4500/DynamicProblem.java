package tio4500;

import constants.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DynamicProblem {

    private ProblemInstance problemInstance;

    public DynamicProblem() {

    }

    public void solve() {

        int subproblemNo = 0;
        for (int time = Constants.START_TIME; time <= Constants.START_TIME + Constants.TOTAL_TIME_DURING_DAY; time += Constants.TIME_INCREMENTS) {
            problemInstance.writeProblemInstanceToFile();
            StaticProblem staticProblem = new StaticProblem(problemInstance.getExampleNumber(),subproblemNo);
            staticProblem.compile();
            staticProblem.solve();
            //generateNextSubproblem();
            subproblemNo++;
        }

    }

    public void generateNextSubproblem() {
        throw new NotImplementedException();
    }
}

package tio4500;

import constants.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DynamicProblem {

    static int noOfSubproblems;

    public DynamicProblem(int noOfSubproblems) {
        this.noOfSubproblems = noOfSubproblems;

    }

    public void solve() {


        for (int time = Constants.START_TIME; time <= Constants.START_TIME + Constants.TOTAL_TIME_DURING_DAY; time += Constants.TIME_INCREMENTS) {

        }



        for(int subproblemNo = 0; subproblemNo < noOfSubproblems; subproblemNo++) {
            StaticProblem staticProblem = new StaticProblem(this.noOfSubproblems, subproblemNo);
            staticProblem.compile();
            staticProblem.solve();
            //generateNextSubproblem();
        }
    }

    public void generateNextSubproblem() {
        throw new NotImplementedException();
    }
}

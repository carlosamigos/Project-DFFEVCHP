package tio4500;

import constants.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tio4500.simulations.Travels.OperatorTravel;

public class DynamicProblem {

    private ProblemInstance problemInstance;

    public DynamicProblem(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }

    public void solve() {

        int subproblemNo = 0;
        for (int time = Constants.START_TIME; time <= Constants.START_TIME + Constants.TOTAL_TIME_DURING_DAY; time += Constants.TIME_INCREMENTS) {
            problemInstance.writeProblemInstanceToFile();
            StaticProblem staticProblem = new StaticProblem();
            staticProblem.compile();
            staticProblem.solve();
            //generateNextSubproblem();
            System.out.println("Objective value: "+staticProblem.getModel().getObjectiveValue());
            subproblemNo++;

            //TODO: update all states until next iteration

        }

    }

    public void generateNextSubproblem() {
        throw new NotImplementedException();
    }

    //private ArrayList<OperatorTravel> readOperatorTravels(){}
}

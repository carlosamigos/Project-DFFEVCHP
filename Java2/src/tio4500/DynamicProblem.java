package tio4500;

import constants.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Travels.OperatorTravel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
            //System.out.println("Objective value: "+staticProblem.getModel().getObjectiveValue());
            subproblemNo++;

            //TODO: update all states until next iteration

        }

    }

    public void generateNextSubproblem() {
        throw new NotImplementedException();
    }

    private ArrayList<OperatorTravel> readOperatorTravels(){
        ArrayList<OperatorTravel> travels = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(Constants.MOSEL_OUTPUT + Constants.OUTPUT_REAL_SERVICE_PATHS);
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {

                line.trim();
                String[] stringList = line.split(":");
                int operatorId = Integer.parseInt(stringList[0]);
                stringList = line.trim().split(",");
                for (String tuple : stringList) {
                    String[] tupleList = tuple.substring(1,tuple.length()-1).split(",");

                }

                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            br.close();
        }catch (IOException e){
            System.out.println(e.getLocalizedMessage());
        }
        return new ArrayList<OperatorTravel>();
    }

}

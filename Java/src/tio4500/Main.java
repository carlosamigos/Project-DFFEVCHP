package tio4500;


import constants.Constants;
import tests.StaticTestSuite;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, rolling horizon!");
        /*ProblemInstance instance = new ProblemInstance(Constants.EXAMPLE_NUMBER);
        SimulationModel simulationModel = new SimulationModel(Constants.DAY_NUMBER,instance);

        if(Constants.CREATE_NEW_SIMULATION_MODEL){
            simulationModel.createNewDaySimulationModel();
            simulationModel.saveDaySimulationModel();
        } else{
            simulationModel.readSimulationModelFromFile();
        }
        instance.writeProblemInstanceToFile();


        DynamicProblem dynProb = new DynamicProblem(instance, simulationModel);
        dynProb.solve();*/
        
        StaticTestSuite testSuite = new StaticTestSuite(Constants.SolverType.MOSEL);
        testSuite.runTestSuite();

    }

}

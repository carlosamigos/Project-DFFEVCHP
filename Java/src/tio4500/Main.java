package tio4500;


import constants.Constants;
import tests.DynamicTestSuite;
import tests.StaticTestSuite;
import utils.FileHandler;

public class Main {

    public static void main(String[] args) {
    	/*
        DynamicTestSuite dynTest = new DynamicTestSuite(Constants.SolverType.MOSEL, Constants.NUMBER_OF_DAYS_TO_TEST);
        dynTest.runTestSuite();
        ProblemInstance instance = new ProblemInstance(Constants.EXAMPLE_NUMBER);
        SimulationModel simulationModel = new SimulationModel(Constants.DAY_NUMBER,instance);


		/* Flyttes inn i SimulationModel-klassen?
        if(Constants.CREATE_NEW_SIMULATION_MODEL){
            simulationModel.createNewDaySimulationModel();
            simulationModel.saveDaySimulationModel();
        } else{
            simulationModel.readSimulationModelFromFile();
        }
        instance.writeProblemInstanceToFile();

        DynamicProblem dynProb = new DynamicProblem(instance, simulationModel, Constants.SolverType.MOSEL);
        dynProb.solve();
		
        */
        StaticTestSuite testSuite = new StaticTestSuite(Constants.SolverType.MOSEL);
        testSuite.runTestSuite();
    }

}

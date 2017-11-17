package tio4500;


import constants.Constants;
import tests.StaticTestSuite;
import utils.FileHandler;

public class Main {

    public static void main(String[] args) {
        /*ProblemInstance instance = new ProblemInstance(Constants.EXAMPLE_NUMBER);
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
        
    	/*
    	FileHandler fh = new FileHandler("../foo", true, true);
    	fh.writeFile("FOO\n");
    	fh.writeFile("BAR\n");
    	fh.writeFile("BAZ");
		*/
    }

}

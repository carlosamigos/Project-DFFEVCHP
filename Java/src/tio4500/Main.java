package tio4500;


import java.util.HashMap;

import constants.Constants;
import tests.DynamicTestSuite;
import tests.StaticTestSuite;
import tests.TestSuite;
import utils.FileHandler;

public class Main {

    public static void main(String[] args) {

    	setConstants(args);
    	TestSuite testSuite;
    	
    	if(Constants.TEST_TYPE == Constants.TestType.STATIC) {
    		testSuite = new StaticTestSuite(Constants.SOLVER_TYPE);
    	} else {
    		testSuite = new DynamicTestSuite(Constants.SOLVER_TYPE, Constants.NUMBER_OF_DAYS_TO_TEST);
    	}
    	//testSuite.runTestSuite();

	/*
        ProblemInstance instance = new ProblemInstance(Constants.EXAMPLE_NUMBER);
        SimulationModel simulationModel = new SimulationModel(Constants.DAY_NUMBER,instance);


        if(Constants.CREATE_NEW_SIMULATION_MODEL){
            simulationModel.createNewDaySimulationModel();
            simulationModel.saveDaySimulationModel();
        } else{
            simulationModel.readSimulationModelFromFile();
        }
        instance.writeProblemInstanceToFile();

        DynamicProblem dynProb = new DynamicProblem(instance, simulationModel, Constants.solverType);
        dynProb.solve();
		
        StaticTestSuite testSuite = new StaticTestSuite(Constants.solverType);
        testSuite.runTestSuite();
        */
    	
    	
    }
    
    // Sets file paths and solver type based on command line arguments.
    public static void setConstants(String[] args) {
    	if(args.length == 0) {
    		return;
    	}
    	
    	HashMap<String, String> input = new HashMap<String, String>();
    	
    	for(String s : args) {
    		if(s.contains(":")) {
    			String[] splitted = s.split(":");
    			input.put(splitted[0], splitted[1]);
    		}
    	}
    	
    	for(String key : input.keySet()) {
    		switch(key) {
    		case "static":
    			Constants.TEST_STATIC_FOLDER = Constants.TEST_INPUT_FOLDER + "Static/" + input.get(key) + "/";
    			Constants.STATIC_TEST_SUITE_RESULTS_FILE = Constants.TEST_OUTPUT_FOLDER + "Static/" + input.get(key) + "/";
    			break;
    		case "dynamic":
    			Constants.TEST_TYPE = Constants.TestType.DYNAMIC;
    			Constants.TEST_DYNAMIC_FOLDER = Constants.TEST_INPUT_FOLDER + "Dynamic/" + input.get(key) + "/";
    			Constants.TEST_DYNAMIC_INITIAL_FOLDER = Constants.TEST_DYNAMIC_FOLDER + "Initial/";
    			break;
    		case "solver":
    			String solver = input.get(key);
    			if (solver == "mosel") {
    				Constants.SOLVER_TYPE = Constants.SolverType.MOSEL;
    			} else if (solver == "ga") {
    				Constants.SOLVER_TYPE = Constants.SolverType.GA;
    			} else {
    				Constants.SOLVER_TYPE = Constants.SolverType.MOSEL;
    			}
    			break;
    		default:
    			break;
    		}
    	}
    	
    	System.out.println(Constants.TEST_DYNAMIC_FOLDER);
    	System.out.println(Constants.TEST_STATIC_FOLDER);
    	System.out.println(Constants.SOLVER_TYPE);
    	System.out.println(Constants.TEST_DYNAMIC_INITIAL_FOLDER);
    }

}

package code;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import code.solver.TSSolver;
import constants.Constants;
import constants.FileConstants;
import constants.HeuristicsConstants;
import tests.DynamicTestSuite;
import tests.StaticTestSuite;
import tests.TestSuite;

public class Main {

    public static void main(String[] args) {

    	boolean testing = true;
    	setConstants(args);
    	createTestingFolders();

    	/*
    	TSSolver solver = new TSSolver(Constants.TABU_ITERATIONS, Constants.TABU_NEIGHBORHOOD_SIZE, Constants.TABU_SIZE);
    	solver.solve(null);
    	System.out.println(solver.getBest());
    	*/
    	
    	if(!testing) {
	    	TSSolver solver = new TSSolver();
	    	solver.solve(null);
	    	System.out.println(solver.getBest());
	    	
	    	TestSuite testSuite;
	    	
	    	if(Constants.TEST_TYPE == Constants.TestType.STATIC) {
	    		testSuite = new StaticTestSuite(Constants.SOLVER_TYPE);
	    	} else {
	    		testSuite = new DynamicTestSuite(Constants.SOLVER_TYPE, Constants.NUMBER_OF_DAYS_TO_TEST);
	    	}
	    	testSuite.runTestSuite();
    	}
    }
    
    // Sets file paths and solver type based on command line arguments.
    private static void setConstants(String[] args) {
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
    			Constants.TEST_TYPE = Constants.TestType.STATIC;
    			FileConstants.TEST_STATIC_FOLDER = FileConstants.TEST_INPUT_FOLDER + "Static/" + input.get(key) + "/";
    			FileConstants.TEST_STATIC_OUTPUT_FOLDER = FileConstants.TEST_OUTPUT_FOLDER + "Static/" + input.get(key) + "/";
    			FileConstants.STATIC_TEST_SUITE_RESULTS_FILE = FileConstants.TEST_STATIC_OUTPUT_FOLDER + "results_";
    			break;
    		case "dynamic":
    			Constants.TEST_TYPE = Constants.TestType.DYNAMIC;
    			FileConstants.TEST_DYNAMIC_FOLDER = FileConstants.TEST_INPUT_FOLDER + "Dynamic/" + input.get(key) + "/";
    			FileConstants.TEST_DYNAMIC_INITIAL_FOLDER = FileConstants.TEST_DYNAMIC_FOLDER + "Initial/";
    			FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER = FileConstants.TEST_OUTPUT_FOLDER + "Dynamic/" + input.get(key) + "/";
    			FileConstants.DYNAMIC_TEST_SUITE_RESULTS_FILE = FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER + "dynamic_test_results_";
    		    FileConstants.DYNAMIC_SINGLE_TEST_RESULTS_FILE = FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER + "static_results_";
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
    }
    
    private static void createTestingFolders() {
    	try {
			Files.createDirectories(Paths.get(FileConstants.TEST_DYNAMIC_INITIAL_FOLDER));
			Files.createDirectories(Paths.get(FileConstants.TEST_STATIC_FOLDER));
			Files.createDirectories(Paths.get(FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}

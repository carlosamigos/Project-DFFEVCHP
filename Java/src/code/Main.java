package code;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import code.problem.ProblemInstance;
import code.solver.ALNSSolver;
import code.solver.heuristics.alns.BestIndividual;
import constants.Constants;
import constants.FileConstants;
import tests.DynamicTestSuite;
import tests.StaticTestSuite;
import tests.TestSuite;
import utils.SolutionFileMaker;

public class Main {

    public static void main(String[] args) {
	boolean testing = false;
    	setConstants(args);
    	createTestingFolders();
    	
    	if(testing) {
    		String fileName = "test_25nodes_5so_3c_15mov_5charging_0finishes_165CM_a";
        	ProblemInstance problemInstance = new ProblemInstance(FileConstants.TEST_STATIC_FOLDER + fileName);
        	for(int i = 0; i < 1; i++) {
        		ALNSSolver solver = new ALNSSolver(problemInstance);
    			BestIndividual best = (BestIndividual) solver.solve(problemInstance);
    		SolutionFileMaker.writeSolutionToFile(best, problemInstance, fileName + ".txt");
            System.out.println(best);
        	}
    	}
	


    	if(!testing) {
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
    			FileConstants.OPERATOR_PATH_OUTPUT_FOLDER = FileConstants.TEST_DYNAMIC_FOLDER + "Paths/";
    			FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER = FileConstants.TEST_OUTPUT_FOLDER + "Dynamic/" + input.get(key) + "/";
    			FileConstants.DYNAMIC_TEST_SUITE_RESULTS_FILE = FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER + "dynamic_test_results_";
    		    FileConstants.DYNAMIC_SINGLE_TEST_RESULTS_FILE = FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER + "static/" + "static_results_";
    			break;
    		case "solver":
    			String solver = input.get(key);
    			if (solver.equals("mosel")) {
    				Constants.SOLVER_TYPE = Constants.SolverType.MOSEL;
    			} else if (solver.equals("alns")) {
    				Constants.SOLVER_TYPE = Constants.SolverType.ALNS;
    			} else {
    				Constants.SOLVER_TYPE = Constants.SolverType.MOSEL;
    			}
    			break;
    		case "model_folder":
    			FileConstants.MOSEL_TEST_FILES_FOLDER =  "../Mosel/Models/" + input.get(key) + "/";
    		default:
    			break;
    		}
    	}
    }
    
    private static void createTestingFolders() {
    	try {
			Files.createDirectories(Paths.get(FileConstants.TEST_DYNAMIC_INITIAL_FOLDER));
			Files.createDirectories(Paths.get(FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER));
			Files.createDirectories(Paths.get(FileConstants.TEST_DYNAMIC_OUTPUT_FOLDER + "static/"));
			Files.createDirectories(Paths.get(FileConstants.TEST_STATIC_FOLDER));
			Files.createDirectories(Paths.get(FileConstants.TEST_STATIC_OUTPUT_FOLDER));
			Files.createDirectories(Paths.get(FileConstants.OPERATOR_PATH_OUTPUT_FOLDER));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}

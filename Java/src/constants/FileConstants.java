package constants;

public class FileConstants {

	 // Mosel specific files and folders
    public final static String PROBLEM_FOLDER = "../Mosel/";
    public final static String MOSEL_OUTPUT_REAL= "../Mosel/output/realOperators/";
    public final static String MOSEL_OUTPUT_ARTIFICIAL= "../Mosel/output/artificialOperators/";
    public final static String MOSEL_TEST_FILES_FOLDER = "../Mosel/Models/main models/";
    public static String STATIC_RUN_STATS = "../Mosel/runStats";
    
    
    // Testing files and folders
    public final static String TEST_FOLDER = "../Testing/";
    public final static String DEFAULT_STATIC_TEST_FOLDER = "Default/";
    public final static String DEFAULT_DYNAMIC_TEST_FOLDER = "Default/";
    public final static String OPERATOR_PATH_FOLDER = "RealOperators/";
    
    public final static String TEST_INPUT_FOLDER = TEST_FOLDER + "Input/";
    public static String TEST_STATIC_FOLDER = TEST_INPUT_FOLDER + "Static/" + DEFAULT_STATIC_TEST_FOLDER;
    public static String TEST_DYNAMIC_FOLDER = TEST_INPUT_FOLDER + "Dynamic/" + DEFAULT_DYNAMIC_TEST_FOLDER;
    public static String TEST_DYNAMIC_INITIAL_FOLDER = TEST_DYNAMIC_FOLDER + "Initial/";
    
    public final static String TEST_OUTPUT_FOLDER = TEST_FOLDER + "Output/";
    public static String TEST_STATIC_OUTPUT_FOLDER = TEST_OUTPUT_FOLDER + "Static/" + DEFAULT_STATIC_TEST_FOLDER;
    public static String STATIC_TEST_SUITE_RESULTS_FILE = TEST_STATIC_OUTPUT_FOLDER + "results_";
    public static String TEST_DYNAMIC_OUTPUT_FOLDER = TEST_OUTPUT_FOLDER + "Dynamic/" + DEFAULT_DYNAMIC_TEST_FOLDER;
	public static String DYNAMIC_TEST_SUITE_RESULTS_FILE = TEST_DYNAMIC_OUTPUT_FOLDER + "dynamic_test_results_";
    public static String DYNAMIC_SINGLE_TEST_RESULTS_FILE = TEST_DYNAMIC_OUTPUT_FOLDER + "static_results_";

    public static String OPERATOR_PATH_OUTPUT_FOLDER = TEST_OUTPUT_FOLDER + "Dynamic/" + OPERATOR_PATH_FOLDER;

    
    // Simulation specific files and folders
    public final static String DEMAND_REQUESTS = "demand_request";
    public final static String SIMULATIONS_FOLDER = "../Simulations/";
}

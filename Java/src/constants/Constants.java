package constants;

public class Constants {

    public final static int NUMBER_OF_DAYS_TO_TEST = 4;

    // General parameters
    public static SolverType SOLVER_TYPE = SolverType.MOSEL;
    public static TestType TEST_TYPE = TestType.DYNAMIC;
    
    // Folders
    public final static String PROBLEM_FOLDER = "../Mosel/";
    public final static String MOSEL_OUTPUT_REAL= "../Mosel/output/realOperators/";
    public final static String MOSEL_OUTPUT_ARTIFICIAL= "../Mosel/output/artificialOperators/";
    public final static String SIMULATIONS_FOLDER = "../Simulations/";
    public final static String TEST_FOLDER = "../Testing/";
    public final static String TEST_INPUT_FOLDER = TEST_FOLDER + "Input/";
    public final static String TEST_OUTPUT_FOLDER = TEST_FOLDER + "Output/";
    public final static String MOSEL_TEST_FILES_FOLDER = "../Mosel/Models/main models/";
    public final static String DEFAULT_STATIC_TEST_FOLDER = "Default/";
    public final static String DEFAULT_DYNAMIC_TEST_FOLDER = "Default/";
    public static String TEST_STATIC_FOLDER = TEST_INPUT_FOLDER + "Static/" + DEFAULT_STATIC_TEST_FOLDER;
    public static String TEST_DYNAMIC_FOLDER = TEST_INPUT_FOLDER + "Dynamic/" + DEFAULT_DYNAMIC_TEST_FOLDER;
    public static String TEST_DYNAMIC_INITIAL_FOLDER = TEST_DYNAMIC_FOLDER + "Initial/";
   
    
    // Indexing
    public final static int START_INDEX = 1; //1 means 1 indexed mosel

    // Files
    public final static String OUTPUT_REAL_SERVICE_PATHS = "outputServiceOperatorsPath";
    public final static String OUTPUT_ARTIFICIAL_SERVICE_PATHS = "outputArtificialServiceOperators";
    public final static String DEMAND_REQUESTS = "demand_request";
    public static String STATIC_TEST_SUITE_RESULTS_FILE = TEST_OUTPUT_FOLDER + "Static/" + DEFAULT_STATIC_TEST_FOLDER + "results";
    public static String DYNAMIC_TEST_SUITE_RESULTS_FILE = PROBLEM_FOLDER + "dynamic_test_results";
    public static String DYNAMIC_SINGLE_TEST_RESULTS_FILE = TEST_OUTPUT_FOLDER + "Dynamic/" + DEFAULT_DYNAMIC_TEST_FOLDER
            + "/static_results_";
    public static String STATIC_RUN_STATS = "../Mosel/runStats";
    
    // Solver
    

    //Cost parameters
    public final static double COST_POSTPONED = 30;
    public final static double COST_DEVIATION = 10;

    // Time specific parameters : Minutes
    public final static int START_TIME = 60*8; // Minutes from midnight
    public final static int END_TIME = 60*16;
    public final static int TIME_INCREMENTS = 15;

    // Parameters for Mosel
    public final static int MAX_SOLVE_TIME_MOSEL_SECONDS = 60; // In seconds
    public final static int OBJECTIVE_MODE = 4;
    public final static boolean PRINT_MOSEL_RESULTS = false;
    public final static boolean PRINT_MOSEL_PARAMETERS = false;
    public final static int TIME_LIMIT_LAST_VISIT = 10;
    public final static int TIME_LIMIT_STATIC_PROBLEM = 60;

    // Variables for simulation environment
    public final static double CUSTOMER_CONSTANT_TIME_USED = 10;
    public final static double HIGH_ARRIVAL_RATE = 10.0; // Average waiting time before next arrival
    public final static double HIGH_RATE_LAMBDA = 1.0/HIGH_ARRIVAL_RATE; // How many Arrivals per time unit
    public final static double MEDIUM_ARRIVAL_RATE = 40.0;
    public final static double MEDIUM_RATE_LAMBDA = 1.0/MEDIUM_ARRIVAL_RATE;
    public final static double LOW_ARRIVAL_RATE = 100.0;
    public final static double LOW_RATE_LAMBDA = 1.0/LOW_ARRIVAL_RATE;
    public final static double PERCENTAGE_AFFECTED_BY_RUSH_HOUR = 2.0/3.0;
    public final static double PERCENTAGE_RUSH_HOUR_SPLIT = 0.5;
    public final static double CUSTOMER_TIME_MULTIPLICATOR = 1.5; // 1-1.5
    public final static double PROBABILITY_CUSTOMERS_CHARGE = 0.7;
    public final static boolean PRINT_OUT_ACTIONS = false;

    public final static double CHARGING_TIME_FULL = 3.5*60; //3.5 hours, in minutes
    public final static double BATTERY_CHARGED_PER_TIME_UNIT = 1.0/CHARGING_TIME_FULL;
    public final static double HARD_CHARGING_THRESHOLD = 0.30;
    public final static double SOFT_CHARGING_THRESHOLD = 0.40;
    public final static double BATTERY_RANGE = 2*60; //2 hours, in minutes
    public final static double BATTERY_USED_PER_TIME_UNIT = 1.0/BATTERY_RANGE;


    public final static int LOCK_TIME_CAR_FOR_OPERATOR = 15;

   
    // TabuSearch Specifics
    public final static int TABU_NEIGHBORHOOD_SIZE = 10;
    public final static int TABU_SIZE = 10;
    public final static int TABU_ITERATIONS = 5;
    
    public enum nodeDemandGroup{
        MORNING_RUSH,
        NEUTRAL,
        MIDDAY_RUSH;
    }

    public enum SolverType {
    	MOSEL,
    	GA;
    }
    
    public enum TestType {
    	STATIC,
    	DYNAMIC;
    }

}

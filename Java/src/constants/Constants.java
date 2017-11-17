package constants;

public class Constants {

    // Example specification
    public final static int EXAMPLE_NUMBER = 3;
    public final static int DAY_NUMBER = 1;
    public final static boolean CREATE_NEW_SIMULATION_MODEL = true;

    // Folders
    public final static String PROBLEM_FOLDER = "../Mosel/";
    public final static String MOSEL_OUTPUT= "../Mosel/output/";
    public final static String STATE_FOLDER = "../Mosel/states/";
    public final static String SIMULATIONS_FOLDER = "../Simulations/";
    public final static String TEST_FOLDER = "../Mosel/tests/";
    public final static String MOSEL_TEST_FILES_FOLDER = "models/";
    
    // Indexing
    public final static int START_INDEX = 1; //1 means 1 indexed mosel

    // Files
    public final static String MOSEL_FILE = "main.mos";
    public final static String MOSEL_BIM_FILE = "main.bim";
    public final static String OUTPUT_REAL_SERVICE_PATHS = "outputServiceOperatorsPath";
    public final static String OUTPUT_ARTIFICIAL_SERVICE_PATHS = "outputArtificialServiceOperators";
    public final static String DEMAND_REQUESTS = "demand_request";
    public final static String INITIAL_STATE_FOLDER = "../Mosel/states/";
    public final static String STATE_FOLDER_FILE = "../Mosel/states/exampleState";
    public final static String STATIC_TEST_SUITE_RESULTS_FILE = PROBLEM_FOLDER + "static_test_results";
    public final static String DYNAMIC_TEST_SUITE_RESULTS_FILE = PROBLEM_FOLDER + "dynamic_test_results";
    public final static String STATIC_RUN_STATS = "../Mosel/runStats";
    
    // Time specific parameters : Minutes
    public final static int START_TIME = 60*6; // Minutes from midnight
    public final static int END_TIME = 60*18;
    public final static int TIME_INCREMENTS = 15;

    // Parameters for Mosel
    public final static int MAX_SOLVE_TIME_MOSEL_SECONDS = 1; // In seconds
    public final static int OBJECTIVE_MODE = 2;
    public final static boolean PRINT_MOSEL_RESULTS = false;
    public final static boolean PRINT_MOSEL_PARAMETERS = false;
    public final static int TIME_LIMIT_LAST_VISIT = 10;
    public final static int TIME_LIMIT_STATIC_PROBLEM = 30;

    // Variables for simulation environment
    public final static double HIGH_ARRIVAL_RATE = 10.0; // Average waiting time before next arrival
    public final static double HIGH_RATE_LAMBDA = 1.0/HIGH_ARRIVAL_RATE; // How many Arrivals per time unit
    public final static double MEDIUM_ARRIVAL_RATE = 50.0;
    public final static double MEDIUM_RATE_LAMBDA = 1.0/MEDIUM_ARRIVAL_RATE;
    public final static double LOW_ARRIVAL_RATE = 120.0;
    public final static double LOW_RATE_LAMBDA = 1.0/LOW_ARRIVAL_RATE;
    public final static double PERCENTAGE_AFFECTED_BY_RUSH_HOUR = 2.0/3.0;
    public final static double PERCENTAGE_RUSH_HOUR_SPLIT = 0.5;
    public final static double CUSTOMER_TIME_MULTIPLICATOR = 1; // 1-1
    public final static double PROBABILITY_CUSTOMERS_CHARGE = 0.7;
    public final static boolean PRINT_OUT_ACTIONS = false;


    public final static double CHARGING_TIME_FULL = 3.5*60; //3.5 hours, in minutes
    public final static double BATTERY_CHARGED_PER_TIME_UNIT = 1.0/CHARGING_TIME_FULL;
    public final static double HARD_CHARGING_THRESHOLD = 0.30;
    public final static double SOFT_CHARGING_THRESHOLD = 0.40;
    public final static double BATTERY_RANGE = 2*60; //2 hours, in minutes
    public final static double BATTERY_USED_PER_TIME_UNIT = 1.0/BATTERY_RANGE;



    public final static int LOCK_TIME_CAR_FOR_OPERATOR = 15;

    public enum nodeDemandGroup{
        MORNING_RUSH,
        NEUTRAL,
        MIDDAY_RUSH;
    }
    
    public enum SolverType {
    	MOSEL;
    }


}

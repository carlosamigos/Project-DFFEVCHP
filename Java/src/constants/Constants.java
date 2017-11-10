package constants;

public class Constants {

    // Example specification
    public final static int EXAMPLE_NUMBER = 2;
    public final static int DAY_NUMBER = 1;
    public final static boolean CREATE_NEW_SIMULATION_MODEL = true;

    // Folders
    public final static String PROBLEM_FOLDER = "../Mosel/";
    public final static String MOSEL_OUTPUT= "../Mosel/output/";
    public final static String STATE_FOLDER = "../Mosel/states/";
    public final static String SIMULATIONS_FOLDER = "../Simulations/";

    // Indexing
    public final static int START_INDEX = 1; //1 means 1 indexed mosel

    // Files
    public final static String MOSEL_FILE = "main.mos";
    public final static String MOSEL_BIM_FILE = "main.bim";
    public final static String OUTPUT_REAL_SERVICE_PATHS = "outputServiceOperatorsPath";
    public final static String OUTPUT_ARTIFICIAL_SERVICE_PATHS = "outputArtificialServiceOperators";
    public final static String DEMAND_REQUESTS = "demand_request";
    public final static String INITIAL_STATE_FOLDER_FILE = "../Mosel/states/initialExample";
    public final static String STATE_FOLDER_FILE = "../Mosel/states/exampleState";

    // Time specific parameters : Minutes
    public final static int START_TIME = 60*6; // Minutes from midnight
    public final static int END_TIME = 60*10;
    public final static int TIME_INCREMENTS = 15;

    // Parameters for Mosel
    public final static int MAX_SOLVE_TIME_MOSEL_SECONDS = 225; // In seconds
    public final static int OBJECTIVE_MODE = 1;
    public final static boolean PRINT_MOSEL_RESULTS = true;
    public final static int TIME_LIMIT_LAST_VISIT = 10;
    public final static int TIME_LIMIT_STATIC_PROBLEM = 60;

    // Variables for simulation environment
    public final static double HIGH_ARRIVAL_RATE = 15.0; // Average waiting time before next arrival
    public final static double HIGH_RATE_LAMBDA = 1.0/HIGH_ARRIVAL_RATE; // How many Arrivals per time unit
    public final static double MEDIUM_ARRIVAL_RATE = 50.0;
    public final static double MEDIUM_RATE_LAMBDA = 1.0/MEDIUM_ARRIVAL_RATE;
    public final static double LOW_ARRIVAL_RATE = 150.0;
    public final static double LOW_RATE_LAMBDA = 1.0/LOW_ARRIVAL_RATE;
    public final static double PERCENTAGE_AFFECTED_BY_RUSH_HOUR = 2.0/3.0;
    public final static double PERCENTAGE_RUSH_HOUR_SPLIT = 0.5;


    public final static double CHARGING_TIME_FULL = 3*60; //3 hours, in minutes
    public final static double BATTERY_CHARGED_PER_TIME_UNIT = 1.0/CHARGING_TIME_FULL;
    public final static double HARD_CHARGING_THRESHOLD = 0.20;
    public final static double SOFT_CHARGING_THRESHOLD = 0.40;
    public final static double BATTERY_RANGE = 3*60; //3 hours, in minutes
    public final static double BATTERY_USED_PER_TIME_UNIT = 1.0/BATTERY_RANGE;



    public final static int LOCK_TIME_CAR_FOR_OPERATOR = 15;

    public enum nodeDemandGroup{
        MORNING_RUSH,
        NEUTRAL,
        MIDDAY_RUSH;
    }


}

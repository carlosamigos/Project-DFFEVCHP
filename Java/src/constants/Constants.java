package constants;

public class Constants {

    // Example specification
    public final static int EXAMPLE_NUMBER = 1;

    // Folders
    public final static String MOSEL_FOLDER= "../Mosel/";
    public final static String PROBLEM_FOLDER = "../Mosel/";
    public final static String MOSEL_OUTPUT= "../Mosel/output/";
    public final static String STATE_FOLDER = "../Mosel/states/";
    public final static String INITIAL_STATE_FOLDER_FILE = "../Mosel/states/initialExample"; //without numbering
    public final static String SIMULATIONS_FOLDER = "../Simulations/";

    // Indexing
    public final static int START_INDEX = 1; //1 means 1 indexed mosel

    // Files
    public final static String MOSEL_FILE = "program_v4_2.mos";
    public final static String MOSEL_BIM_FILE = "program_v4_2.bim";
    public final static String GENERAL_INFO_FILE = "general_info.txt";
    public final static String OUTPUT_REAL_SERVICE_PATHS = "outputServiceOperatorsPath.txt";
    public final static String OUTPUT_ARTIFICIAL_SERVICE_PATHS = "outputArtificialServiceOperators.txt";
    public final static String DEMAND_REQUESTS = "demand_request";

    // Time specific parameters : Minutes
    public final static int TOTAL_TIME_DURING_DAY = 60*12;
    public final static int START_TIME = 6*60; // Minutes from midnight
    public final static int TIME_INCREMENTS = 60*6;

    // Variables for Mosel
    public final static int MAX_DURATION = 200; // In seconds, not used at the moment

    // Variables for simulation environment
    public final static double HIGH_RATE_LAMBDA = 3.0; // Average waiting time before next arrival
    public final static double MEDIUM_RATE_LAMBDA = 15.0;
    public final static double LOW_RATE_LAMBDA = 30.0;
    public final static double PERCENTAGE_AFFECTED_BY_RUSH_HOUR = 2.0/3.0;
    public final static double PERCENTAGE_RUSH_HOUR_SPLIT = 0.5;

    public final static double CHARGING_TIME_FULL = 3*60; //3 hours, in minutes
    public final static double CHARGING_THRESHOLD = 0.40;
    public final static double BATTERY_RANGE = 3*60; //3 hours, in minutes
    public final static double BATTERY_USED_PER_TIME_UNIT = 1.0/BATTERY_RANGE;

    public final static int NUMBER_OF_CARS_LOCKED_IN_OPERATOR_PATH = 1;

    public enum nodeDemandGroup{
        MORNING_RUSH,
        NEUTRAL,
        MIDDAY_RUSH;
    }


}

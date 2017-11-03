package constants;

public class Constants {

	// Example specification
	public final static int EXAMPLE_NUMBER = 1;

	// Folders
	public final static String MOSEL_FOLDER= "../Mosel/";
	public final static String PROBLEM_FOLDER = "../Mosel/problems/";
	public final static String MOSEL_OUTPUT= "../Mosel/output";
	public final static String STATE_FOLDER = "../Mosel/states/";
	public final static String INITIAL_STATE_FOLDER_FILE = "../Mosel/initialStates/initialExample"; //without numbering
	public final static String SIMULATIONS_FOLDER = "../Simulations/";

	// Indexing
	public final static int START_INDEX = 1; //1 means 1 indexed mosel

	// Files
	public final static String MOSEL_FILE = "test.mos";
	public final static String MOSEL_BIM_FILE = "test.bim";
	public final static String GENERAL_INFO_FILE = "general_info.txt";
	
	// Time specific parameters
	public final static double TOTAL_TIME_DURING_DAY = 60*24; // Minutes

	// Variables for Mosel
	public final static int MAX_DURATION = 200; // In minutes

	// Variables for simulation environment
	public final static double HIGH_RATE_LAMBDA = 1.0; // Average waiting time before next arrival
	public final static double MEDIUM_RATE_LAMBDA = 5.0;
	public final static double LOW_RATE_LAMBDA = 10.0;
	public final static double PERCENTAGE_AFFECTED_BY_RUSH_HOUR = 2.0/3.0;
	public final static double PERCENTAGE_RUSH_HOUR_SPLIT = 0.5;

	public final static double CHARGING_TIME_FULL = 3*60; //3 hours, in minutes
	public final static double CHARGING_THRESHOLD = 0.40;
	public final static double BATTERY_RANGE = 3*60; //3 hours, in minutes
	public final static double PERCENTAGE_BATTERY_USED_PER_TIME_UNIT = 1.0/BATTERY_RANGE;

	public final static int NUMBER_OF_CARS_LOCKED_IN_OPERATOR_PATH = 1;




}

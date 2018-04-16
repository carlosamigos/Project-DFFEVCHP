package constants;

public class Constants {


	// General Parameters
	public final static boolean PRINT_OUT_ACTIONS = true;
	public final static boolean DETAILED_PRINTOUTS = false;
	
	// Cost Parameters
    public final static double COST_POSTPONED = 30;
    public final static double COST_DEVIATION = 10;

    // Test Parameters
    public static SolverType SOLVER_TYPE = SolverType.MOSEL;
    public static TestType TEST_TYPE = TestType.STATIC;
    public final static int NUMBER_OF_DAYS_TO_TEST = 1;
    public final static int START_TIME = 60*8; // Minutes from midnight
    public final static int END_TIME = 60*16;
    public final static int TIME_INCREMENTS = 15;

    // Parameters for Mosel
    public final static int START_INDEX = 1; //1 means 1 indexed mosel
    public final static int MAX_SOLVE_TIME_MOSEL_SECONDS = 7200; // In seconds
    public final static int OBJECTIVE_MODE = 4;
    public final static boolean PRINT_MOSEL_RESULTS = false;
    public final static boolean PRINT_MOSEL_PARAMETERS = false;
    public final static int TIME_LIMIT_LAST_VISIT = 10;
    public final static int TIME_LIMIT_STATIC_PROBLEM = 60;

    public enum SolverType {
    	MOSEL,
    	ALNS;
    }
    
    public enum TestType {
    	STATIC,
    	DYNAMIC;
    }

}

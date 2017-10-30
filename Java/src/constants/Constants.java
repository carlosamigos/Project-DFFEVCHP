package constants;

public class Constants {

	// Folders
	public final static String INPUT_FOLDER = "../Mosel/examples/";
	public final static String PROBLEM_FOLDER = "../Mosel/problems/";
	public final static String MOSEL_FOLDER= "../Mosel/";
	
	// Files
	public final static String MOSEL_FILE = "test.mos";
	public final static String MOSEL_BIM_FILE = "test.bim";
	public final static String GENERAL_INFO_FILE = "general_info.txt";
	
	// Time specific parameters
	public final static double TOTAL_TIME = 100;
	public final static double TIME_LIMIT_STEP = 3.5;
	public final static double TIME_LIMIT_LAST_VISIT = 5.0;
	public final static int VISITS = 3;
	
	// Node specific parameters
	public final static int N_NODES = 8;
	public final static int P_NODES = 6;
	public final static int C_NODES = 2;
	
	// Operator specific parameters
	public final static int N_OPERATORS = 3;

	// Settings for drawing code
	public final static String C_TO_P = "[3 5]";
	public final static int VERTICAL_NODES = 3;
	public final static int HORIZONTAL_NODES = 2;
	
	// Variables for Mosel
	public final static int MAX_DURATION = 200; // In seconds
}

package constants;

public class HeuristicsConstants {

    public final static boolean PRINT_OUT_PROGRESS = true;
    
    // Chromosome Generator
    public final static double MAX_THRESHOLD_CARMOVE_DISTANCE = 1.0;
	 
	// Tabu Search
    public final static int TABU_NEIGHBORHOOD_SIZE = 100;
    public final static int TABU_ITERATIONS = 200000;
    public final static int ALNS_MAX_TIME_SECONDS = 60;
    public final static int TABU_MAX_NON_IMPROVING_ITERATIONS = 250000;

    //Fitness, that does not exist in Mosel
    public final static double TABU_CHARGING_UNIT_REWARD = 0;
    public final static double TABU_BREAK_CHARGING_CAPACITY = 100;
    public final static double TABU_SIZE_OF_OPERATOR_LIST = 0.1;
    
    // Best first
    public final static boolean BEST_FIRST = true;
    
    // ALNS Parameters used when comparring with Mosel
    public final static double ALNS_CHARGING_REWARD = 30;
    public final static double ALNS_TRAVEL_TIME_CAR_MOVE_PENALTY = 0.2;
    public final static double ALNS_IDEAL_STATE_UNIT_REWARD = 10; 
    public final static double ALNS_TRAVEL_COST = 0.01;

    // Tabu list specifics
    public final static int TABU_MAX_NON_IMPROVING_LOCAL_ITERATIONS = 10;
    public final static int TABU_MIN_IMPROVING_LOCAL_ITERATIONS = 2;
    public final static int TABU_MAX_SIZE = 256;
    public final static int TABU_SIZE = 2;
   
    // Stopping criteria: Max iterations without improvement
    public final static int ALNS_MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 100000;

    //LSO Weight
    public final static int ALNS_SEGMENT_LENGTH = 100;
    public final static double ALNS_FOUND_NEW_SOLUTION = 1.0;
    public final static double ALNS_FOUND_NEW_BEST_REWARD = 2.0;
    public final static double ALNS_FOUND_NEW_GLOBAL_BEST_REWARD = 10.0;
    public final static double ALNS_UPDATE_FACTOR = 0.5;
    public final static double ALNS_MINIMAL_WEIGHT_FACTOR = 0.4;

    //Initial solution
    public final static double TABU_TRAVEL_COST_INITIAL_CONSTRUCTION = 0.01;
    public final static double TABU_CHARGING_UNIT_INITIAL_REWARD = 30;
    public final static double TABU_IDEAL_STATE_INITIAL_REWARD = 10;
    public final static double TABU_INITIAL_BREAK_CHARGING_CAPACITY = 100;
    public final static double TABU_INITIAL_SURPLUS_IDEAL_STATE_COST = 0;
    public final static boolean ALNS_INITIAL_GREEDY_BUILD = true;

    // Relatedness measure
    public final static double FROM_NODE_WEIGHT = 0.315;
    public final static double TO_NODE_WEIGHT = 0.315;
    public final static double IS_CHARGING_WEIGHT = 0.315;
    public final static double TRAVEL_DISTANCE_WEIGHT = 0.005;
    public final static double EARLIEST_DEPARTURE_WEIGHT = 0.05;

    //Large Neighborhood
    public final static double ALNS_FOUND_NEW_SOLUTION_LNS = 1.0;
    public final static double ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS = 10.0;
    public final static double ALNS_DESTROY_FACTOR = 0.5;
    public final static double ALNS_UPDATE_FACTOR_LNS = 0.2;
    public final static double ALNS_MINIMAL_WEIGHT_FACTOR_LNS = 0.4;
    public final static int TABU_MAX_NON_IMPROVING_ITERATIONS_DESTROY = 3000;

    //Enumeration of neighborhood
    public final static boolean ALNS_FULL_NEIGHBORHOOD = true;
    public final static boolean ALNS_FULL_ALL_NEIGHBORHOOD = false;

}

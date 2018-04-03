package constants;

public class HeuristicsConstants {
    
    // Chromosome Generator
    public final static double MAX_THRESHOLD_CARMOVE_DISTANCE = 1.0;
	 
	// Tabu Search
    public final static int TABU_NEIGHBORHOOD_SIZE = 200;
    public final static int TABU_SIZE = 2;
    public final static int TABU_ITERATIONS = 10001;
    public final static int TABU_MAX_NON_IMPROVING_ITERATIONS = 25000;
    public final static double TABU_CHARGING_UNIT_REWARD = 1;
    public final static double TABU_BREAK_CHARGING_CAPACITY = 100;
    public final static double TABU_SURPLUS_IDEAL_STATE_COST = 100;
    public final static double TABU_IDEAL_STATE_UNIT_REWARD = 30;
    public final static double TABU_TRAVEL_COST = 0;
    
    // This variable decides when the tabu list is increased in size (doubled)
    public final static int TABU_MAX_NON_IMPROVING_LOCAL_ITERATIONS = 10;

    // This variable decides when to destroy the current solution
    public final static int TABU_MAX_NON_IMPROVING_ITERATIONS_DESTROY = 5000;
   
    
    
    public final static int ALNS_SEGMENT_LENGTH = 100;
    public final static double ALNS_FOUND_NEW_SOLUTION = 1.0;
    public final static double ALNS_FOUND_NEW_BEST_REWARD = 2.0;
    public final static double ALNS_FOUND_NEW_GLOBAL_BEST_REWARD = 10.0;
    public final static double ALNS_UPDATE_FACTOR = 0.5;
    public final static double ALNS_DESTROY_FACTOR = 0.5;
    

    //Punishes the size of operator list, relative to the travel time
    public final static double TABU_SIZE_OF_OPERATOR_LIST = 1;

    public final static double TABU_INTRA_MOVE_SIZE = 0.4*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_INTER_MOVE_SIZE = 0.2*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_INTER_2_SWAP_SIZE = 0.3*TABU_NEIGHBORHOOD_SIZE;

    public final static double TABU_EJECTION_REPLACE_SIZE = 0.3*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_REMOVE_SIZE = 0.05*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_INSERT_SIZE = 0.05*TABU_NEIGHBORHOOD_SIZE;
    
    //Initial solution
    public final static double TABU_TRAVEL_COST_INITIAL_CONSTRUCTION = 0;
    public final static double TABU_CHARGING_UNIT_INITIAL_REWARD = 1;
    public final static double TABU_IDEAL_STATE_INITIAL_REWARD = 5;
    public final static double TABU_INITIAL_BREAK_CHARGING_CAPACITY = 100;
    public final static double TABU_INITIAL_SURPLUS_IDEAL_STATE_COST = 0;

    
    // Adaptive Large Neighborhood Search


}

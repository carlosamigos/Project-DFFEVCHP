package constants;

public class HeuristicsConstants {
    
    // Chromosome Generator
    public final static double MAX_THRESHOLD_CARMOVE_DISTANCE = 1.0;
	 
	// Tabu Search
    public final static int TABU_NEIGHBORHOOD_SIZE = 500;
    public final static int TABU_SIZE = 2;
    public final static int TABU_ITERATIONS = 5000;
    public final static double TABU_CHARGING_UNIT_REWARD = 2;
    public final static double TABU_BREAK_CHARGING_CAPACITY = 100;
    public final static double TABU_SURPLUS_IDEAL_STATE_COST = 100;
    public final static double TABU_IDEAL_STATE_UNIT_REWARD = 5;
    public final static double TABU_TRAVEL_COST = 0;
    
    //Punishes the size of operator list, relative to the travel time
    public final static double TABU_SIZE_OF_OPERATOR_LIST = 0.1;

    public final static double TABU_INTRA_MOVE_SIZE = 1*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_INTER_MOVE_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_INTER_2_SWAP_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_REPLACE_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_REMOVE_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_INSERT_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    
    //Initial solution
    public final static double TABU_TRAVEL_COST_INITIAL_CONSTRUCTION = 0.2;
    public final static double TABU_CHARGING_UNIT_INITIAL_REWARD = 10;
    public final static double TABU_IDEAL_STATE_INITIAL_REWARD = 10;
    public final static double TABU_INITIAL_BREAK_CHARGING_CAPACITY = 100;
    public final static double TABU_INITIAL_SURPLUS_IDEAL_STATE_COST = 100;
    
    // Genetic Algorithm

    // Adaptive Large Neighborhood Search
}

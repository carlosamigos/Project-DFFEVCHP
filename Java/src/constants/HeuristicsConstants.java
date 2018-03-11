package constants;

public class HeuristicsConstants {
    
    // Chromosome Generator
    public final static double MAX_THRESHOLD_CARMOVE_DISTANCE = 1.0;
	 
	// Tabu Search
    public final static int TABU_NEIGHBORHOOD_SIZE = 50;
    public final static int TABU_SIZE = 10;
    public final static int TABU_ITERATIONS = 10;
    
    public final static double TABU_SIZE_OF_OPERATOR_LIST = 0;
    public final static double TABU_CHARGING_UNIT_REWARD = 0;
    public final static double TABU_BREAK_CHARGING_CAPACITY = 0;
    public final static double TABU_IDEAL_STATE_UNIT_REWARD = 100;
    public final static double TABU_TRAVEL_COST = 0;

    
    //Initial solution
    public final static double TABU_TRAVEL_COST_INITIAL_CONSTRUCTION = 0.2;
    public final static double TABU_CHARGING_UNIT_INITIAL_REWARD = 5;
    public final static double TABU_IDEAL_STATE_INITIAL_REWARD = 0;
    public final static double TABU_IDEAL_STATE_REWARD = 0.5;
    public final static double TABU_SURPLUS_IDEAL_STATE_COST = 100;

    public final static double TABU_INTRA_MOVE_SIZE = 0.1*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_INTER_MOVE_SIZE = 0.2*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_INTER_2_SWAP_SIZE = 0.5*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_REPLACE_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_REMOVE_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    public final static double TABU_EJECTION_INSERT_SIZE = 0*TABU_NEIGHBORHOOD_SIZE;
    
    // Genetic Algorithm

    // Adaptive Large Neighborhood Search
}

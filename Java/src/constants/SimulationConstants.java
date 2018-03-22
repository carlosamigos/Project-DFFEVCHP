package constants;


public class SimulationConstants {
	
	public final static double CUSTOMER_CONSTANT_TIME_USED = 10;
    public final static double HIGH_ARRIVAL_RATE = 10.0; // Average waiting time before next arrival
    public final static double HIGH_RATE_LAMBDA = 1.0/HIGH_ARRIVAL_RATE; // How many Arrivals per time unit
    public final static double MEDIUM_ARRIVAL_RATE = 40.0;
    public final static double MEDIUM_RATE_LAMBDA = 1.0/MEDIUM_ARRIVAL_RATE;
    public final static double LOW_ARRIVAL_RATE = 100.0;
    public final static double LOW_RATE_LAMBDA = 1.0/LOW_ARRIVAL_RATE;
    public final static double PERCENTAGE_AFFECTED_BY_RUSH_HOUR = 2.0/3.0;
    public final static double PERCENTAGE_RUSH_HOUR_SPLIT = 0.5;
    public final static double CUSTOMER_TIME_MULTIPLICATOR = 1.5; // 1-1.5
    public final static double PROBABILITY_CUSTOMERS_CHARGE = 0.7;
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


}

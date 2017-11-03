package tio4500;



public class Main {

    public static void main(String[] args) {
	    System.out.println("Hello, rolling horizon!");
	    
	    DynamicProblem dynamicProblem = new DynamicProblem(3);
	    dynamicProblem.solve();
    }
    
    public static void generateGeneralInfo() {
    	InputGenerator input = new InputGenerator();
	    input.generateGeneralInfo();
    }

    public static void generateSimulationEnvironment(){

		// 0. Generate travel times
		// 1. get initial state
    	// 2. generate arrivals
		// 3. generate pickups

	}
}

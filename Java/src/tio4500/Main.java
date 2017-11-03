package tio4500;



public class Main {

    public static void main(String[] args) {
	    System.out.println("Hello, rolling horizon!");
	    
	    //DynamicProblem dynamicProblem = new DynamicProblem(3);
	    //dynamicProblem.solve();

		createProblemInstance();
    }
    

    public static void createProblemInstance(){
    	int exampleNumber = 1;
    	ProblemInstance instance = new ProblemInstance(exampleNumber);
    	System.out.println(instance);
	}

    public static void runSimulation(){

		SimulationModel simModel = new SimulationModel(1);


	}
}

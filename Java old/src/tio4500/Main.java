package tio4500;



public class Main {

    public static void main(String[] args) {
	    System.out.println("Hello, rolling horizon!");

	    
	    //DynamicProblem dynamicProblem = new DynamicProblem(3);
	    //dynamicProblem.solve();

		int exampleNumber = 1;
		int dayNumber = 1;
		ProblemInstance instance = new ProblemInstance(exampleNumber);
		System.out.println(instance);
		SimulationModel simulationModel = new SimulationModel(dayNumber,instance);
		simulationModel.createNewDaySimulationModel();
		simulationModel.saveDaySimulationModel();
    }




}

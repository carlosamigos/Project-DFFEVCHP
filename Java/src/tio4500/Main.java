package tio4500;



public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, rolling horizon!");

        int exampleNumber = 1;
        int dayNumber = 1;

        ProblemInstance instance = new ProblemInstance(exampleNumber);
        System.out.println(instance);
        SimulationModel simulationModel = new SimulationModel(dayNumber,instance);
        simulationModel.createNewDaySimulationModel();
        //simulationModel.saveDaySimulationModel();
        //simulationModel.readSimulationModelFromFile();
        instance.writeProblemInstanceToFile();

        DynamicProblem dynProb = new DynamicProblem(instance, simulationModel);
        dynProb.solve();

    }

}

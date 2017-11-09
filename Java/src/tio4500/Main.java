package tio4500;


import constants.Constants;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, rolling horizon!");

        ProblemInstance instance = new ProblemInstance(Constants.EXAMPLE_NUMBER);
        System.out.println(instance);
        SimulationModel simulationModel = new SimulationModel(Constants.DAY_NUMBER,instance);
        simulationModel.createNewDaySimulationModel();
        simulationModel.saveDaySimulationModel();
        //simulationModel.readSimulationModelFromFile();
        instance.writeProblemInstanceToFile();

        DynamicProblem dynProb = new DynamicProblem(instance, simulationModel);
        dynProb.solve();

    }

}

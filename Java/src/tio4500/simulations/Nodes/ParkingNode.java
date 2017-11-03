package tio4500.simulations.Nodes;

public class ParkingNode {

    private final int nodeId;
    private int numberOfFullyChargedCars;
    private int numberOfCarsInNeed;

    public ParkingNode(int nodeId, int numberOfFullyChargedCars, int numberOfCarsInNeed) {
        this.nodeId = nodeId;
        this.numberOfFullyChargedCars = numberOfFullyChargedCars;
        this.numberOfCarsInNeed = numberOfCarsInNeed;
    }


    // Getters and Setters

    public int getNodeId() {
        return nodeId;
    }

    public int getNumberOfFullyChargedCars() {
        return numberOfFullyChargedCars;
    }

    public int getNumberOfCarsInNeed() {
        return numberOfCarsInNeed;
    }

    public void setNumberOfFullyChargedCars(int numberOfFullyChargedCars) {
        this.numberOfFullyChargedCars = numberOfFullyChargedCars;
    }

    public void setNumberOfCarsInNeed(int numberOfCarsInNeed) {
        this.numberOfCarsInNeed = numberOfCarsInNeed;
    }
}

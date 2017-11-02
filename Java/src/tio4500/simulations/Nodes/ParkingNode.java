package tio4500.simulations.Nodes;

public class ParkingNode extends Node{


    private int numberOfFullyChargedCars = 0;
    private int numberOfCarsInNeed = 0;

    public ParkingNode(int nodeId) {
        super(nodeId);
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

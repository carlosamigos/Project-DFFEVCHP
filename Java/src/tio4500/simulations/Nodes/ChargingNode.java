package tio4500.simulations.Nodes;

public class ChargingNode extends Node{

    private int numberOfCarsCharging = 0;
    private int numberOfTotalChargingSpots = 0;

    public ChargingNode(int nodeId) {
        super(nodeId);
    }

    public int getNumberOfCarsCharging() {
        return numberOfCarsCharging;
    }

    public void setNumberOfCarsCharging(int numberOfCarsCharging) {
        this.numberOfCarsCharging = numberOfCarsCharging;
    }

    public int getNumberOfTotalChargingSpots() {
        return numberOfTotalChargingSpots;
    }

    public void setNumberOfTotalChargingSpots(int numberOfTotalChargingSpots) {
        this.numberOfTotalChargingSpots = numberOfTotalChargingSpots;
    }
}


package tio4500.simulations.Nodes;

public class ChargingNode {

    public final int nodeId;
    private int numberOfCarsCharging;
    private int numberOfTotalChargingSpots;

    public ChargingNode(int nodeId, int numberOfCarsCharging, int numberOfTotalChargingSpots) {
        this.nodeId = nodeId;
        this.numberOfCarsCharging = numberOfCarsCharging;
        this.numberOfTotalChargingSpots = numberOfTotalChargingSpots;
    }

    public int getNodeId() {
        return nodeId;
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


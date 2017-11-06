package tio4500.simulations.Nodes;

public class ChargingNode extends Node{

    private int numberOfCarsCharging = 0;
    private int numberOfTotalChargingSlots = 0;

    public ChargingNode(int nodeId) {
        super(nodeId);
    }

    public int getNumberOfCarsCharging() {
        return numberOfCarsCharging;
    }

    public void setNumberOfCarsCharging(int numberOfCarsCharging) {
        this.numberOfCarsCharging = numberOfCarsCharging;
    }

    public int getNumberOfTotalChargingSlots() {
        return numberOfTotalChargingSlots;
    }

    public void setNumberOfTotalChargingSpots(int numberOfTotalChargingSlots) {
        this.numberOfTotalChargingSlots = numberOfTotalChargingSlots;
    }

    @Override
    public String toString() {
        return "cNode{" +
                    super.getNodeId()+
                '}';
    }
}


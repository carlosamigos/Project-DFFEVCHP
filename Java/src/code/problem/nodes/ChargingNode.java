package code.problem.nodes;

import constants.Constants;

import java.util.HashSet;

import code.problem.entities.Car;

public class ChargingNode extends Node{

    private int numberOfTotalChargingSlots = 0;
    private HashSet<Car> carsCurrentlyCharging;

    public ChargingNode(int nodeId) {
        super(nodeId);
        carsCurrentlyCharging = new HashSet<>();
    }

    public HashSet<Car> getCarsCurrentlyCharging() {
        return carsCurrentlyCharging;
    }

    public int findNumberOfCarsFinishingChargingDuringNextPeriod(){
        int numberOfCarsFinishingCharging = 0;
        for (Car car : carsCurrentlyCharging) {
            if (car.getRemainingChargingTime() < Constants.TIME_LIMIT_STATIC_PROBLEM){
                numberOfCarsFinishingCharging +=1;
            }
        }
        return numberOfCarsFinishingCharging;
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


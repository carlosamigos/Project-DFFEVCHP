package tio4500.simulations.Nodes;

import constants.Constants;
import tio4500.simulations.Entities.Car;

import java.util.HashSet;

public class ChargingNode extends Node{

    private int numberOfCarsCharging = 0;
    private int numberOfTotalChargingSlots = 0;
    private HashSet<Car> carsCurrentlyCharging;

    public ChargingNode(int nodeId) {
        super(nodeId);
        carsCurrentlyCharging = new HashSet<>();
    }

    public HashSet<Car> getCarsCurrentlyCharging() {
        return carsCurrentlyCharging;
    }

    public int findNumberOfChargingSpotsAvailableDuringNextPeriod(){
        int numberOfCarsFinishingCharging = 0;
        for (Car car : getCarsCurrentlyCharging()) {
            if (car.getRemainingChargingTime() < Constants.TIME_INCREMENTS){
                numberOfCarsFinishingCharging +=1;
            }
        }
        int numberOfAvailableChargingSpotsDuringNextPeriod = getNumberOfTotalChargingSlots() - getNumberOfCarsCharging() + numberOfCarsFinishingCharging;
        return numberOfAvailableChargingSpotsDuringNextPeriod;
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


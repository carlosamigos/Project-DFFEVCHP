package tio4500.simulations.Travels;

import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.ParkingNode;

public class OperatorTravel {

    private Operator operator;
    private Car car;
    private double pickupTime;
    private ParkingNode pickupNode;
    private double arrivalTime;
    private ParkingNode arrivalNode;
    private double pickupBatteryLevel;
    private double arrivalBatteryLevel;


    public OperatorTravel(Operator operator, Car car, double pickupTime, ParkingNode pickupNode, double arrivalTime, ParkingNode arrivalNode, double pickupBatteryLevel, double arrivalBatteryLevel) {
        this.operator = operator;
        this.car = car;
        this.pickupTime = pickupTime;
        this.pickupNode = pickupNode;
        this.arrivalTime = arrivalTime;
        this.arrivalNode = arrivalNode;
        this.pickupBatteryLevel = pickupBatteryLevel;
        this.arrivalBatteryLevel = arrivalBatteryLevel;
    }

    // Getters and Setters

    public double getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(double pickupTime) {
        this.pickupTime = pickupTime;
    }

    public ParkingNode getPickupNode() {
        return pickupNode;
    }

    public void setPickupNode(ParkingNode pickupNode) {
        this.pickupNode = pickupNode;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public ParkingNode getArrivalNode() {
        return arrivalNode;
    }

    public void setArrivalNode(ParkingNode arrivalNode) {
        this.arrivalNode = arrivalNode;
    }

    public double getPickupBatteryLevel() {
        return pickupBatteryLevel;
    }

    public void setPickupBatteryLevel(double pickupBatteryLevel) {
        this.pickupBatteryLevel = pickupBatteryLevel;
    }

    public double getArrivalBatteryLevel() {
        return arrivalBatteryLevel;
    }

    public void setArrivalBatteryLevel(double arrivalBatteryLevel) {
        this.arrivalBatteryLevel = arrivalBatteryLevel;
    }
}

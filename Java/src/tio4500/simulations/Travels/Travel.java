package tio4500.simulations.Travels;

import tio4500.simulations.Entities.Car;
import tio4500.simulations.Nodes.Node;
import tio4500.simulations.Nodes.ParkingNode;

public class Travel {

    private Car car;
    private double departureTime;
    private Node pickupNode;
    private double arrivalTime;
    private Node arrivalNode;
    private double previousTimeStep;


    public Travel(double departureTime, Node pickupNode, double arrivalTime, Node arrivalNode) {
        this.departureTime = departureTime;
        this.pickupNode = pickupNode;
        this.arrivalTime = arrivalTime;
        this.arrivalNode = arrivalNode;
        this.previousTimeStep = departureTime;

    }

    public double getPreviousTimeStep() {
        return previousTimeStep;
    }

    public void setPreviousTimeStep(double previousTimeStep) {
        this.previousTimeStep = previousTimeStep;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public Node getPickupNode() {
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

    public Node getArrivalNode() {
        return arrivalNode;
    }

    public void setArrivalNode(ParkingNode arrivalNode) {
        this.arrivalNode = arrivalNode;
    }


    @Override
    public String toString() {
        return "Travel{" +
                "car=" + car +
                ", from=" + pickupNode +
                ", to=" + arrivalNode +
                '}';
    }
}

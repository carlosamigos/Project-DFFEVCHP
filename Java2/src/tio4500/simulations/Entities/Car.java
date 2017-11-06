package tio4500.simulations.Entities;

import tio4500.simulations.Nodes.Node;

public class Car {

    private final int carId;
    private double batteryLevel;
    private Node currentNode;

    public Car(int carId, double batteryLevel, Node currentNode) {
        this.carId = carId;
        this.batteryLevel = batteryLevel;
        this.currentNode = currentNode;
    }

    public int getCarId() {
        return carId;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + carId +
                ", lvl=" + batteryLevel +
                ", node=" + currentNode.getNodeId()+
                '}';
    }
}

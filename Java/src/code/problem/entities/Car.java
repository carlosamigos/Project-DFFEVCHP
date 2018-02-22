package code.problem.entities;

import code.problem.nodes.Node;

public class Car implements Comparable {

    private final int carId;
    private double batteryLevel;
    private double previousBatteryLevel;
    private Node currentNextNode = null;
    private Node previousNode = null;
    private double remainingChargingTime = 0;
    private double timeInInNeedState = 0.0;



    public Car(int carId, double batteryLevel) {
        this.carId = carId;
        this.batteryLevel = batteryLevel;
        this.previousBatteryLevel = batteryLevel;
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

    public Node getCurrentNextNode() {
        return currentNextNode;
    }

    public void setCurrentNextNode(Node currentNextNode) {
        this.currentNextNode = currentNextNode;
    }

    public double getRemainingChargingTime() {
        return remainingChargingTime;
    }

    public void setRemainingChargingTime(double remainingChargingTime) {
        this.remainingChargingTime = remainingChargingTime;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public double getPreviousBatteryLevel() {
        return previousBatteryLevel;
    }

    public void setPreviousBatteryLevel(double previousBatteryLevel) {
        this.previousBatteryLevel = previousBatteryLevel;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

    public double getTimeInInNeedState() {
        return timeInInNeedState;
    }

    public void setTimeInInNeedState(double timeInInNeedState) {
        this.timeInInNeedState = timeInInNeedState;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + carId +
                ", lvl=" + batteryLevel +
                ", node=" + currentNextNode.getNodeId()+
                ", rem.C.time="+remainingChargingTime +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        if(this.batteryLevel - ((Car)o).getBatteryLevel() > 0 ){
            return 1;
        } else if (this.batteryLevel - ((Car)o).getBatteryLevel() ==0 ){
            return 0;
        } else {
            return -1;
        }
    }
}

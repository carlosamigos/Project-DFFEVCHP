package code.problem.entities;

import java.io.Serializable;

import code.problem.nodes.Node;
import constants.Constants;

@SuppressWarnings("serial")
public class Operator implements Serializable {

    private final int id;
    private Node nextOrCurrentNode = null;
    private Node previousNode = null;
    private double timeRemainingToCurrentNextNode = 0;
    private boolean isHandling = false;
    private double totalIdleTime = 0.0;
    private double arrivalTimeToNextOrCurrentNode = Constants.START_TIME;
    private boolean wasHandlingToNextCurrentNode = false;
    private Car car = null;


    public Operator(int id) {
        this.id = id;
    }

    public void setNextOrCurrentNode(Node nextOrCurrentNode) {
        this.nextOrCurrentNode = nextOrCurrentNode;
        if(previousNode == null){
            previousNode = nextOrCurrentNode;
        }
    }

    public int getId() {
        return id;
    }

    public Node getNextOrCurrentNode() {
        return nextOrCurrentNode;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

    public double getTimeRemainingToCurrentNextNode() {
        return timeRemainingToCurrentNextNode;
    }

    public void setTimeRemainingToCurrentNextNode(double timeRemainingToCurrentNextNode) {
        this.timeRemainingToCurrentNextNode = timeRemainingToCurrentNextNode;
    }

    public boolean isHandling() {
        return isHandling;
    }

    public void setHandling(boolean handling) {
        isHandling = handling;
    }

    public double getTotalIdleTime() {
        return totalIdleTime;
    }

    public void setTotalIdleTime(double totalIdleTime) {
        this.totalIdleTime = totalIdleTime;
    }

    public double getArrivalTimeToNextOrCurrentNode() {
        return arrivalTimeToNextOrCurrentNode;
    }

    public void setArrivalTimeToNextOrCurrentNode(double arrivalTimeToNextOrCurrentNode) {
        this.arrivalTimeToNextOrCurrentNode = arrivalTimeToNextOrCurrentNode;
    }

    public boolean wasHandlingToNextCurrentNode() {
        return wasHandlingToNextCurrentNode;
    }

    public void setWasHandlingToNextCurrentNode(boolean wasHandlingToNextCurrentNode) {
        this.wasHandlingToNextCurrentNode = wasHandlingToNextCurrentNode;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public String toString() {
        return "Operator{" +
                "id=" + id +
                '}';
    }
}

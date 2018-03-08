package code.solver.heuristics.entities;

import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;

public class CarMove {

    private ParkingNode fromNode;
    private Node toNode; //either ParkingNode or ChargingNode
    private Car car;
    private final double travelTime;
    private final double earliestDepartureTime;
    private final boolean isToCharging;

    public CarMove(ParkingNode fromNode, Node toNode, Car car, double travelTime, double earliestDepartureTime) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.car = car;
        this.travelTime = travelTime;
        this.earliestDepartureTime = earliestDepartureTime;
        this.isToCharging = (toNode instanceof ChargingNode) ? true : false;
    }

    public ParkingNode getFromNode() {
        return this.fromNode;
    }

    public Node getToNode() {
        return this.toNode;
    }

    public Car getCar() {
        return this.car;
    }

    public double getTravelTime() {
        return this.travelTime;
    }

    public double getEarliestDepartureTime() {
        return this.earliestDepartureTime;
    }
    
    public boolean isToCharging() {
    	return this.isToCharging;
    }

    @Override
    public String toString() {
        return "" + car.getCarId() + "(" + this.fromNode.getNodeId() + "->" + this.toNode.getNodeId() + ")";
    }
    
    public String detailedToString() {
    		return "[CarID: " + car.getCarId() + ", FromNode: " + this.fromNode.getNodeId() 
    		+ ", ToNode: " + this.toNode.getNodeId() + ", Duration: " + this.getTravelTime() + "]";
    }
}

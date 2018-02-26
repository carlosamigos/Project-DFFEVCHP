package code.solver.heuristics;

import code.problem.entities.Car;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;

public class CarMove {

    private ParkingNode fromNode;
    private Node toNode; //either ParkingNode or ChargingNode
    private Car car;
    private Double travelTime;
    private Double earliestDepartureTime;

    public CarMove(ParkingNode fromNode, Node toNode, Car car, Double travelTime, Double earliestDepartureTime) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.car = car;
        this.travelTime = travelTime;
        this.earliestDepartureTime = earliestDepartureTime;
    }

    public ParkingNode getFromNode() {
        return fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public Car getCar() {
        return car;
    }

    public Double getTravelTime() {
        return travelTime;
    }

    public Double getEarliestDepartureTime() {
        return earliestDepartureTime;
    }
}

package code.problem.travels;

import code.problem.entities.Operator;
import code.problem.nodes.Node;

public class OperatorArrival {

    private double arrivalTime;
    private boolean isHandling;
    private Node node;
    private Operator operator;

    public OperatorArrival(double arrivalTime, boolean isHandling, Node node, Operator operator) {
        this.arrivalTime = arrivalTime;
        this.isHandling = isHandling;
        this.node = node;
        this.operator = operator;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public boolean isHandling() {
        return isHandling;
    }

    public Node getNode() {
        return node;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return "OperatorArrival{" +
                "arrivalTime=" + arrivalTime +
                ", isHandling=" + isHandling +
                ", node=" + node +
                ", operator=" + operator +
                '}';
    }
}

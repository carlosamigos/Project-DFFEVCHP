package tio4500.simulations.Travels;

import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.Node;

public class OperatorDeparture {

    private Node node;
    private Operator operator;
    private double departureTime;
    private OperatorArrival operatorArrival;


    public OperatorDeparture(Node node, Operator operator, double departureTime, OperatorArrival operatorArrival) {
        this.node = node;
        this.operator = operator;
        this.departureTime = departureTime;
        this.operatorArrival = operatorArrival;
    }

    public Node getNode() {
        return node;
    }

    public Operator getOperator() {
        return operator;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public OperatorArrival getOperatorArrival() {
        return operatorArrival;
    }

    @Override
    public String toString() {
        return "OperatorDeparture{" +
                "node=" + node +
                ", operator=" + operator +
                ", departureTime=" + departureTime +
                ", arrivalTIme=" + getOperatorArrival().getArrivalTime()+
                '}';
    }
}

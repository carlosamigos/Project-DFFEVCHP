package code.problem.travels;

import code.problem.entities.Operator;
import code.problem.nodes.Node;

public class OperatorDeparture {

    private Node node;
    private Operator operator;
    private double departureTime;
    private OperatorArrival operatorArrival;
    private boolean isHandling;


    public OperatorDeparture(Node node, Operator operator, boolean isHandling, double departureTime, OperatorArrival operatorArrival) {
        this.node = node;
        this.operator = operator;
        this.departureTime = departureTime;
        this.operatorArrival = operatorArrival;
        this.isHandling = isHandling;
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

    public void setDepartureTime(double time){
        departureTime = time;
    }

    public boolean isHandling() {
        return isHandling;
    }

    @Override
    public String toString() {
        return "OperatorDeparture{" +
                "node=" + node +
                ", operator=" + operator +
                ", departureTime=" + departureTime +
                ", arrivalTIme=" + ((operatorArrival!=null)? operatorArrival.getArrivalTime():"null")+
                '}';
    }
}

package code.problem.travels;

import code.problem.entities.Operator;
import code.problem.nodes.Node;

public class OperatorTravel extends Travel{

    private Operator operator;

    public OperatorTravel(Operator operator, double pickupTime, Node pickupNode, Node arrivalNode, double arrivalTime) {
        super(pickupTime, pickupNode, arrivalTime, arrivalNode);
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        int fromId = (this.getPickupNode()!= null)? getPickupNode().getNodeId() : -1;
        int toId = (this.getArrivalNode()!= null)? getArrivalNode().getNodeId() : -1;
        int carId = (getCar() != null) ? getCar().getCarId() : -1;
        return "{Operator " + operator.getId() + ", car " +carId+ ", from->to = " + fromId+"->"+toId+ ", arrival time "+getArrivalTime()+"}";
    }

}

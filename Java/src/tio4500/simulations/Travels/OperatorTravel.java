package tio4500.simulations.Travels;

import com.sun.org.apache.xpath.internal.operations.Bool;
import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.Node;
import tio4500.simulations.Nodes.ParkingNode;

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
        return super.toString().substring(0,super.toString().length()-1) + ", operator=" + operator + "}";
    }

}

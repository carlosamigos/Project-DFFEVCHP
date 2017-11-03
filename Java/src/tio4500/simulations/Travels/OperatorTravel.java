package tio4500.simulations.Travels;

import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.ParkingNode;

public class OperatorTravel extends Travel{

    private Operator operator;

    public OperatorTravel(Operator operator,Car car, double pickupTime, ParkingNode pickupNode, double arrivalTime, ParkingNode arrivalNode, double pickupBatteryLevel, double arrivalBatteryLevel) {
        super(car, pickupTime, pickupNode, arrivalTime, arrivalNode, pickupBatteryLevel, arrivalBatteryLevel);
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }
}

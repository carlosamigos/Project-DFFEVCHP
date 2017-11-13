package tio4500.simulations.Travels;

import tio4500.simulations.Entities.Car;
import tio4500.simulations.Nodes.ParkingNode;

public class CustomerTravel extends Travel{


    public CustomerTravel(double pickupTime, ParkingNode pickupNode, double arrivalTime, ParkingNode arrivalNode) {
        super(pickupTime, pickupNode, arrivalTime, arrivalNode);
    }


}

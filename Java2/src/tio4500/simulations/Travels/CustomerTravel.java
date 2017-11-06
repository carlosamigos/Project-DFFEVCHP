package tio4500.simulations.Travels;

import tio4500.simulations.Entities.Car;
import tio4500.simulations.Nodes.ParkingNode;

public class CustomerTravel extends Travel{


    public CustomerTravel(Car car, double pickupTime, ParkingNode pickupNode, double arrivalTime, ParkingNode arrivalNode, double pickupBatteryLevel, double arrivalBatteryLevel) {
        super(car, pickupTime, pickupNode, arrivalTime, arrivalNode, pickupBatteryLevel, arrivalBatteryLevel);
    }

}

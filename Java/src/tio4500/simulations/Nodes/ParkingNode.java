package tio4500.simulations.Nodes;

import tio4500.simulations.Entities.Car;

import java.util.ArrayList;

public class ParkingNode extends Node{

    private ArrayList<Car> carsInNeed;
    private ArrayList<Car> carsRegular;

    public ParkingNode(int nodeId) {
        super(nodeId);
        carsInNeed = new ArrayList<>();
        carsRegular = new ArrayList<>();
    }

    public ArrayList<Car> getCarsInNeed() {
        return carsInNeed;
    }


    public ArrayList<Car> getCarsRegular() {
        return carsRegular;
    }

    public void addRegularCar(Car car){
        this.carsRegular.add(car);
    }

    public void addCarInNeed(Car car){
        this.carsInNeed.add(car);
    }

    public void removeCarFromNode(Car car){
        carsInNeed.remove(car);
        carsRegular.remove(car);
    }

    @Override
    public String toString() {
        return "ParkingNode{"+super.getNodeId()+"}";
    }
}

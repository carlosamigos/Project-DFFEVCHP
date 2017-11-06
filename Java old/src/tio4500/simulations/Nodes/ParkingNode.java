package tio4500.simulations.Nodes;

import constants.Constants;
import tio4500.simulations.Entities.Car;

import java.util.ArrayList;

public class ParkingNode extends Node{

    private ArrayList<Car> carsInNeed;
    private ArrayList<Car> carsRegular;
    private Double demandRate = 0.0;
    private Constants.nodeDemandGroup demandGroup = null;

    public ParkingNode(int nodeId) {
        super(nodeId);
        carsInNeed = new ArrayList<>();
        carsRegular = new ArrayList<>();
    }

    public ArrayList<Car> getCarsInNeed() {
        return carsInNeed;
    }

    public void setDemandRate(double rate){
        this.demandRate = rate;
    }

    public Double getDemandRate() {
        return demandRate;
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

    public Constants.nodeDemandGroup getDemandGroup() {
        return demandGroup;
    }

    public void setDemandGroup(Constants.nodeDemandGroup demandGroup) {
        this.demandGroup = demandGroup;
    }


    @Override
    public String toString() {
        return "pNode{"+super.getNodeId()+"}";
    }
}

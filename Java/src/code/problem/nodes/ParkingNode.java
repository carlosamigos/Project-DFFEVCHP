package code.problem.nodes;

import constants.SimulationConstants;

import java.util.ArrayList;

import code.problem.entities.Car;

public class ParkingNode extends Node{

    private ArrayList<Car> carsInNeed;
    private ArrayList<Car> carsRegular;
    private Double demandRate = 0.0;
    private SimulationConstants.nodeDemandGroup demandGroup = null;
    private int idealNumberOfAvailableCarsThisPeriod = 0;
    private int predictedNumberOfCarsDemandedThisPeriod = 0;
    private int carsArrivingThisPeriod = 0;

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

    public SimulationConstants.nodeDemandGroup getDemandGroup() {
        return demandGroup;
    }

    public void setDemandGroup(SimulationConstants.nodeDemandGroup demandGroup) {
        this.demandGroup = demandGroup;
    }

    public int getIdealNumberOfAvailableCars() {
        return idealNumberOfAvailableCarsThisPeriod;
    }

    public void setIdealNumberOfAvailableCarsThisPeriod(int idealNumberOfAvailableCars) {
        this.idealNumberOfAvailableCarsThisPeriod = idealNumberOfAvailableCars;
    }

    public int getPredictedNumberOfCarsDemandedThisPeriod() {
        return predictedNumberOfCarsDemandedThisPeriod;
    }

    public void setPredictedNumberOfCarsDemandedThisPeriod(int predictedNumberOfCarsDemandedThisPeriod) {
        this.predictedNumberOfCarsDemandedThisPeriod = predictedNumberOfCarsDemandedThisPeriod;
    }

    public int getCarsArrivingThisPeriod() {
        return carsArrivingThisPeriod;
    }

    public void setCarsArrivingThisPeriod(int carsArrivingThisPeriod) {
        this.carsArrivingThisPeriod = carsArrivingThisPeriod;
    }

    @Override
    public String toString() {
        return "pNode{"+super.getNodeId()+", cReg="+carsRegular.size()+", cNeed="+carsInNeed.size()+ "}";
    }
}

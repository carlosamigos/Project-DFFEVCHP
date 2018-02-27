package code.solver.heuristics.entities;

import java.util.ArrayList;

public class Operator {

	private ArrayList<CarMove> carMoves;
	private int carsBeingCharged;
	private double travelTime;
	private double startTime;
	private double costOfPostponed;
	private double costOfTravel;
	
	
	public Operator(double startTime) {
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
	}
	
	public CarMove getCarMove(int index) {
		return carMoves.get(index);
	}
	
	public void insertCarMove(CarMove carMove) {
		this.carMoves.add(carMove);
	}
	
	public void insertCarMoves(ArrayList<CarMove> carMoves) {
		this.carMoves.addAll(carMoves);
	}
	
	public int getCarsBeingCharged() {
		return this.carsBeingCharged;
	}
	
	public double getTravelTime() {
		return this.travelTime;
	}
	
	public double getStartTime() {
		return this.startTime;
	}
	
	public void addToTravelTime(double deltaTime) {
		this.travelTime += deltaTime;
	}
	
	public double getCostOfPostponed() {
		return this.costOfPostponed;
	}
	
	private void calculateCostOfPostponed() {
		this.costOfPostponed = 0.0;
	}
	
	public double getCostOfTravel() {
		return this.costOfTravel;
	}
	
	private void calculateCostOfTravel() {
		this.costOfTravel = 0.0;
	}
	
}

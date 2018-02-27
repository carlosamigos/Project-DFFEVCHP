package code.solver.heuristics.entities;

import java.util.ArrayList;

public class Operator {

	private ArrayList<CarMove> carMoves;
	private int carsBeingCharged;
	private int travelTime;
	private double costOfPostponed;
	private double costOfTravel;
	
	
	public Operator(ArrayList<CarMove> carMoves) {
		this.carMoves = carMoves;
	}
	
	public CarMove getCarMove(int index) {
		return carMoves.get(index);
	}
	
	public void insertCar(CarMove carMove) {
		this.carMoves.add(carMove);
	}
	
	public int getCarsBeingCharged() {
		return this.carsBeingCharged;
	}
	
	public int getTravelTime() {
		return this.travelTime;
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

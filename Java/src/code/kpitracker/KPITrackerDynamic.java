package code.kpitracker;

import constants.Constants;

import java.util.ArrayList;
import java.util.Collections;

import code.DynamicProblem;
import code.problem.entities.Operator;

public class KPITrackerDynamic {


    // ArrayLists are numbers for each period
    private DynamicProblem dynamicProblem;
	private ArrayList<Integer> demandsNotServed;
    private ArrayList<Integer> demandServed;
    private ArrayList<Integer> numberOfOperatorsAbandoned;
    private ArrayList<Integer> numberOfCarsSetToCharging;
    private double totalCarTravelDoneByServiceOperators;
    private double totalBikeTravelDoneByServiceOperators;
    private double electricityUsedWhenRelocatingCars;
    private ArrayList<Double> waitingTimeBeforeCarInNeedAreCharged;
    private ArrayList<Double> idleTimeForServiceOperators;
    private ArrayList<KPITrackerStatic> staticKPITrackers;


    public KPITrackerDynamic(DynamicProblem dynamicProblem) {
        int numberOfSubProblems = (Constants.END_TIME - Constants.START_TIME)/Constants.TIME_INCREMENTS - (int)(Constants.TIME_LIMIT_STATIC_PROBLEM/Constants.TIME_INCREMENTS) +1;
        this.dynamicProblem = dynamicProblem;
        demandsNotServed = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        demandServed = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        numberOfCarsSetToCharging = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        numberOfOperatorsAbandoned = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        totalCarTravelDoneByServiceOperators = 0.0;
        totalBikeTravelDoneByServiceOperators = 0.0;
        totalCarTravelDoneByServiceOperators = 0.0;
        electricityUsedWhenRelocatingCars = 0.0;
        waitingTimeBeforeCarInNeedAreCharged = new ArrayList<>();
        idleTimeForServiceOperators = new ArrayList<>();
        staticKPITrackers = new ArrayList<>();
    }

    public void addStaticKPItracker(KPITrackerStatic tracker){
        staticKPITrackers.add(tracker);
    }

    public void increaseDemandNotServedForPeriod(int period){
        // given periods start at 1
        int demandNotServed = demandsNotServed.get(period-1) + 1;
        demandsNotServed.set(period-1,demandNotServed);
    }

    public void increaseDemandServedForPeriod(int period){
        // given periods start at 1
        int current = demandServed.get(period-1);
        demandServed.set(period-1,current +1);
    }

    public void increaseNumberOfOperatorsAbandoned(int period){
        // given periods start at 1
        int current = numberOfOperatorsAbandoned.get(period-1);
        numberOfOperatorsAbandoned.set(period-1,current + 1);
    }

    public void increaseNumberOfCarsSetToCharging(int period){
        // given periods start at 1
        int current = numberOfCarsSetToCharging.get(period-1);
        numberOfCarsSetToCharging.set(period-1,current + 1);
    }

    public void increaseCarTotalTravelTimeDoneByOperator(double travelTime){
        totalCarTravelDoneByServiceOperators += travelTime;
        double batterUsed = travelTime * Constants.BATTERY_USED_PER_TIME_UNIT;
        electricityUsedWhenRelocatingCars += batterUsed;
    }

    public void increaseBikeTotalTravelTimeDoneByOperator(double travelTime){
        totalBikeTravelDoneByServiceOperators += travelTime;
    }

    public void addInNeedWaitingTime(double waitingTime){
        waitingTimeBeforeCarInNeedAreCharged.add(waitingTime);
    }

    public void updateIdleTimeForOperators(){
        for (Operator operator: dynamicProblem.getProblemInstance().getOperators()) {
            this.idleTimeForServiceOperators.add(operator.getTotalIdleTime());
        }
    }

    public ArrayList<Integer> getDemandsNotServed() {
		return demandsNotServed;
	}

	public ArrayList<Integer> getNumberOfOperatorsAbandoned() {
		return numberOfOperatorsAbandoned;
	}

	public ArrayList<Integer> getNumberOfCarsSetToCharging() {
		return numberOfCarsSetToCharging;
	}

	public double getTotalCarTravelDoneByServiceOperators() {
		return totalCarTravelDoneByServiceOperators;
	}

	public double getTotalBikeTravelDoneByServiceOperators() {
		return totalBikeTravelDoneByServiceOperators;
	}

	public double getElectricityUsedWhenRelocatingCars() {
		return electricityUsedWhenRelocatingCars;
	}

	public ArrayList<Double> getWaitingTimeBeforeCarInNeedAreCharged() {
		return waitingTimeBeforeCarInNeedAreCharged;
	}

	public ArrayList<Double> getIdleTimeForServiceOperators() {
		return idleTimeForServiceOperators;
	}

	public ArrayList<KPITrackerStatic> getStaticKPITrackers() {
		return staticKPITrackers;
	}



    public double calculateDemandServedFraction(){
        int totalDemandServed = 0;
        int totalDemandNotServed = 0;
        for (int period = 1; period <= demandServed.size(); period++) {
            totalDemandServed += demandServed.get(period-1);
            totalDemandNotServed += demandsNotServed.get(period-1);
        }
        double fraction = ((double)totalDemandServed) / (totalDemandServed + totalDemandNotServed);
        return fraction;
    }

    @Override
    public String toString() {
        return  "\n Results{" +
                "\n demandsNotServed=" + demandsNotServed +
                "\n numberOfOperatorsAbandoned=" + numberOfOperatorsAbandoned +
                "\n numberOfCarsSetToCharging=" + numberOfCarsSetToCharging +
                "\n totalCarTravelDoneByServiceOperators=" + totalCarTravelDoneByServiceOperators +
                "\n totalBikeTravelDoneByServiceOperators=" + totalBikeTravelDoneByServiceOperators +
                "\n electricityUsedWhenRelocatingCars=" + electricityUsedWhenRelocatingCars +
                "\n waitingTimeBeforeCarInNeedAreCharged=" + waitingTimeBeforeCarInNeedAreCharged +
                "\n idleTimeForServiceOperators=" + idleTimeForServiceOperators +
                '}';
    }
}

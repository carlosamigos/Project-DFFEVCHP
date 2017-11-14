package tio4500;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.Constants;
import tio4500.simulations.Entities.Operator;

import java.util.ArrayList;
import java.util.Collections;

public class KPITracker {


    // ArrayLists are numbers for each period
    private DynamicProblem dynamicProblem;
    private ArrayList<Integer> demandsNotServed;
    private ArrayList<Integer> numberOfOperatorsAbandoned;
    private ArrayList<Integer> numberOfCarsSetToCharging;
    private double totalCarTravelDoneByServiceOperators;
    private double totalBikeTravelDoneByServiceOperators;
    private double electricityUsedWhenRelocatingCars;
    private ArrayList<Double> waitingTimeBeforeCarInNeedAreCharged;
    private ArrayList<Double> idleTimeForServiceOperators;
    private ArrayList<Boolean> staticProblemSolvedToOptimality;
    private ArrayList<Double> staticProblemGap;

    public KPITracker(DynamicProblem dynamicProblem) {
        int numberOfSubProblems = (Constants.END_TIME - Constants.START_TIME)/Constants.TIME_INCREMENTS - (int)(Constants.TIME_LIMIT_STATIC_PROBLEM/Constants.TIME_INCREMENTS) +1;
        this.dynamicProblem = dynamicProblem;
        demandsNotServed = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        numberOfCarsSetToCharging = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        numberOfOperatorsAbandoned = new ArrayList<>(Collections.nCopies(numberOfSubProblems, 0));
        totalCarTravelDoneByServiceOperators = 0.0;
        totalBikeTravelDoneByServiceOperators = 0.0;
        totalCarTravelDoneByServiceOperators = 0.0;
        electricityUsedWhenRelocatingCars = 0.0;
        waitingTimeBeforeCarInNeedAreCharged = new ArrayList<>();
        idleTimeForServiceOperators = new ArrayList<>();
        staticProblemSolvedToOptimality = new ArrayList<>();
        staticProblemGap = new ArrayList<>();
    }

    void increaseDemandNotServedForPeriod(int period){
        // given periods start at 1
        int current = demandsNotServed.get(period-1);
        demandsNotServed.set(period-1,current +1);
    }

    void increaseNumberOfOperatorsAbandoned(int period){
        // given periods start at 1
        int current = numberOfOperatorsAbandoned.get(period-1);
        numberOfOperatorsAbandoned.set(period-1,current + 1);
    }

    void increaseNumberOfCarsSetToCharging(int period){
        // given periods start at 1
        int current = numberOfCarsSetToCharging.get(period-1);
        numberOfCarsSetToCharging.set(period-1,current + 1);
    }

    void increaseCarTotalTravelTimeDoneByOperator(double travelTime){
        totalCarTravelDoneByServiceOperators += travelTime;
        double batterUsed = travelTime * Constants.BATTERY_USED_PER_TIME_UNIT;
        electricityUsedWhenRelocatingCars += batterUsed;
    }

    void increaseBikeTotalTravelTimeDoneByOperator(double travelTime){
        totalBikeTravelDoneByServiceOperators += travelTime;
    }

    void addInNeedWaitingTime(double waitingTime){
        waitingTimeBeforeCarInNeedAreCharged.add(waitingTime);
    }

    void updateIdleTimeForOperators(){
        for (Operator operator: dynamicProblem.getProblemInstance().getOperators()) {
            this.idleTimeForServiceOperators.add(operator.getTotalIdleTime());
        }
    }

    void addGapForPeriod(double gap){
        this.staticProblemGap.add(gap);
    }

    void addSolvedToOptimality(boolean solvedToOptimality){
        this.staticProblemSolvedToOptimality.add(solvedToOptimality);
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

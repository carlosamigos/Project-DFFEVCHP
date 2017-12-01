package tio4500;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.Constants;
import constants.Constants.SolverType;
import tio4500.simulations.DemandRequest;
import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.ChargingNode;
import tio4500.simulations.Nodes.Node;
import tio4500.simulations.Nodes.ParkingNode;
import tio4500.simulations.Travels.CustomerTravel;
import tio4500.simulations.Travels.OperatorArrival;
import tio4500.simulations.Travels.OperatorDeparture;
import tio4500.simulations.Travels.OperatorTravel;
import tio4500.solvers.MoselSolver;
import tio4500.solvers.Solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class DynamicProblem {

    private ProblemInstance problemInstance;
    private SimulationModel simulationModel;
    private KPITrackerDynamic kpiTrackerDyanmic;
    private Solver solver;
    private String fileName;

    public DynamicProblem(ProblemInstance problemInstance, SimulationModel simulationModel, Solver solver, String fileName) {
        this.problemInstance = problemInstance;
        this.simulationModel = simulationModel;
        this.kpiTrackerDyanmic = new KPITrackerDynamic(this);
        this.solver = solver;
        this.fileName = fileName;
    }


    public void solve() {

        int subproblemNo = 1;
        ArrayList<CustomerTravel> customerTravels = new ArrayList<>();
        HashMap<Operator,OperatorTravel> operatorTravels = new HashMap<>();
        for (int time = Constants.START_TIME; time <= Constants.END_TIME - Constants.TIME_LIMIT_STATIC_PROBLEM*2; time += Constants.TIME_INCREMENTS) {
            if(Constants.PRINT_OUT_ACTIONS){
                System.out.println("\n\n");
                System.out.println("Sub problem "+subproblemNo+" starting at time: "+timeToHHMM(time));
            }

            updateOptimalNumberOfCarsInParking(time);
            predictNumberOfCarsPickedUpNextPeriod(time);
            problemInstance.writeProblemInstanceToFile();
            if(Constants.PRINT_OUT_ACTIONS){
                System.out.println("State before solving mosel: "+problemInstance + "\n");
            }
            KPITrackerStatic tracker = new KPITrackerStatic();
            StaticProblem problem = new StaticProblem(Constants.TEST_DYNAMIC_FOLDER + fileName);
            this.solver.solve(problem);
            doPeriodActions(time, time + Constants.TIME_INCREMENTS, customerTravels,operatorTravels,subproblemNo);
            subproblemNo++;
            tracker.setResults(problem.getFilePath());
            kpiTrackerDyanmic.addStaticKPItracker(tracker);
        }
        kpiTrackerDyanmic.updateIdleTimeForOperators();
        if(Constants.PRINT_OUT_ACTIONS){
            System.out.println(kpiTrackerDyanmic);
        }
    }

    public String timeToHHMM(double time){
        int hours = (int) Math.floor(time/60);
        int minutesLeft = (int) Math.round(time - hours * 60);
        String HH = "";
        String MM = "";
        if(hours < 10){
            HH = "0"+hours;
        }else{
            HH = ""+hours;
        }


        if (minutesLeft <10){
            MM = "0" + minutesLeft;
        }else{
            MM = "" + minutesLeft;
        }
        return HH + ":" + MM;
    }

    public void doPeriodActions(int startTime, int endTime, ArrayList<CustomerTravel> customerTravels, HashMap<Operator,OperatorTravel> operatorTravels, int subProblemNumber ){
        double time = startTime;
        HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures = readOperatorArrivalsAndDepartures(startTime);
        double previousTime;
        while (time < endTime){
            previousTime = time;

            DemandRequest nextDemandRequest = findNextDemandRequest(time, endTime);
            OperatorDeparture nextOperatorDepartureOrArrival = findNextOperatorDepartureOrArrival(time,endTime,operatorDepartures);
            CustomerTravel nextCustomerArrival = findNextCustomerArrival(time,endTime,customerTravels);
            OperatorTravel nextOperatorTravelArrival = findNextOperatorTravelArrival(time,endTime,operatorTravels);


            if(nextDemandRequest == null && nextOperatorDepartureOrArrival == null && nextCustomerArrival == null && nextOperatorTravelArrival==null ){
                break;
            }
            double nextDemandReqTime = nextDemandRequest != null ? nextDemandRequest.getTime() : Double.MAX_VALUE;
            double nextOperatorHappeningTime = nextOperatorDepartureOrArrival != null ? findEarliestHappeningOverTime(nextOperatorDepartureOrArrival,time) : Double.MAX_VALUE;
            double nextCustomerArrivalTime = nextCustomerArrival != null ? nextCustomerArrival.getArrivalTime() : Double.MAX_VALUE;
            double nextOperatorTravelArrivalTime = nextOperatorTravelArrival != null ? nextOperatorTravelArrival.getArrivalTime() : Double.MAX_VALUE;

            double earliestTime = Double.min(Double.min(nextDemandReqTime, nextOperatorHappeningTime),nextCustomerArrivalTime);
            time = earliestTime;
            if(Constants.PRINT_OUT_ACTIONS){
                System.out.print(timeToHHMM(time) + " : ");
            }
            if(nextOperatorTravelArrivalTime <=earliestTime ){
                // operator arrival
                Operator operator = nextOperatorTravelArrival.getOperator();
                Node arrivalNode = nextOperatorTravelArrival.getArrivalNode();
                operatorTravels.remove(operator);
                removeOperatorDepartureArrival(nextOperatorTravelArrival, operatorDepartures);
                operator.setNextOrCurrentNode(arrivalNode);
                operator.setPreviousNode(arrivalNode);
                operator.setTimeRemainingToCurrentNextNode(0);
                operator.setHandling(false);
                Car car = nextOperatorTravelArrival.getCar();
                if(Constants.PRINT_OUT_ACTIONS) {
                    System.out.println("Operator " + operator + " arrives with car " + car);
                }
                if(car != null){
                    operator.setWasHandlingToNextCurrentNode(true);
                    updateBatteryLevelOnCar(time,previousTime,car);
                    car.setPreviousNode(arrivalNode);
                    car.setCurrentNextNode(arrivalNode);
                    if(arrivalNode instanceof  ParkingNode){
                        if(car.getBatteryLevel() < Constants.SOFT_CHARGING_THRESHOLD){
                            ((ParkingNode) arrivalNode).getCarsInNeed().add(car);
                        } else {
                            ((ParkingNode) arrivalNode).getCarsRegular().add(car);
                        }

                    } else {
                        ((ChargingNode) arrivalNode).getCarsCurrentlyCharging().add(car);
                        this.kpiTrackerDyanmic.increaseNumberOfCarsSetToCharging(subProblemNumber);
                    }
                }else {
                    operator.setWasHandlingToNextCurrentNode(false);
                }
                time = nextOperatorTravelArrivalTime;
            }

            else if(nextDemandReqTime == earliestTime){
                time = nextDemandReqTime;
                // customer would like to pick up car
                //System.out.println("demand reg earliest");
                simulationModel.getDemandRequests().get(nextDemandRequest.getNode()).remove(nextDemandRequest);
                ParkingNode pNode = nextDemandRequest.getNode();
                if(isThereACarAvailableToBePickedUpAtNodeByCustomer(pNode,operatorTravels,operatorDepartures,time)){
                    // Do customer travel, but not to the same node
                    int rndIndex = new Random().nextInt(problemInstance.getParkingNodes().size());
                    ParkingNode arrivalNode = problemInstance.getParkingNodes().get(rndIndex);
                    double travelTime = problemInstance.getTravelTimesCar().get(pNode.getNodeId() - Constants.START_INDEX).get(arrivalNode.getNodeId()-Constants.START_INDEX);
                    travelTime *=  (Math.random()* (Constants.CUSTOMER_TIME_MULTIPLICATOR-1) + 1);
                    travelTime += Constants.CUSTOMER_CONSTANT_TIME_USED;
                    double arrivalTime = nextDemandReqTime + travelTime;
                    CustomerTravel newCustomerTravel = new CustomerTravel(nextDemandReqTime,pNode,arrivalTime,arrivalNode);
                    Car travelCar = findAvailableCarForCustomerInNode(pNode);
                    if(travelCar != null) {
                        newCustomerTravel.setCar(travelCar);
                        travelCar.setPreviousNode(pNode);
                        travelCar.setCurrentNextNode(arrivalNode);
                        customerTravels.add(newCustomerTravel);
                        time = nextDemandReqTime;
                        pNode.getCarsRegular().remove(travelCar);
                        pNode.getCarsInNeed().remove(travelCar);
                        if(Constants.PRINT_OUT_ACTIONS){
                            System.out.println("Customer Travel Added from node " + pNode + " at time: " + nextDemandReqTime + " with car " + travelCar);
                        }
                    } else {
                        if(Constants.PRINT_OUT_ACTIONS){
                            System.out.println("No car available for customer.");
                        }
                        this.kpiTrackerDyanmic.increaseDemandNotServedForPeriod(subProblemNumber);
                    }
                }else {
                    if(Constants.PRINT_OUT_ACTIONS){
                        System.out.println("No car available for customer.");
                    }
                    this.kpiTrackerDyanmic.increaseDemandNotServedForPeriod(subProblemNumber);
                }
            }
            else if(nextOperatorHappeningTime == earliestTime){
                time = nextOperatorHappeningTime;
                //System.out.println("next operator happening earliest");
                Operator operator = nextOperatorDepartureOrArrival.getOperator();
                if(nextOperatorHappeningTime == nextOperatorDepartureOrArrival.getDepartureTime()){
                    //operator departures
                    nextOperatorDepartureOrArrival.setDepartureTime(nextOperatorHappeningTime - 0.00001);
                    OperatorArrival arrival = nextOperatorDepartureOrArrival.getOperatorArrival();
                    if(arrival != null){
                        Node departureNode = nextOperatorDepartureOrArrival.getNode();
                        Node arrivalNode = arrival.getNode();
                        OperatorTravel travel = new OperatorTravel(operator,nextOperatorHappeningTime,departureNode, arrivalNode,arrival.getArrivalTime());
                        if(arrival.isHandling() && departureNode.equals(operator.getNextOrCurrentNode())){
                            if(((ParkingNode) departureNode).getCarsRegular().size()==0){
                                // car taken by customer, make operator inactive
                                operatorDepartures.put(operator,new ArrayList<>());
                            } else {
                                if(arrivalNode instanceof ParkingNode){
                                    //take regular car
                                    Car car = ((ParkingNode) departureNode).getCarsRegular().remove(0);
                                    if (car != null){
                                        car.setPreviousNode(travel.getPickupNode());
                                        car.setCurrentNextNode(travel.getArrivalNode());
                                        travel.setCar(car);
                                        travel.setPreviousTimeStep(nextOperatorHappeningTime);
                                        operatorTravels.put(operator,travel);
                                        operator.setArrivalTimeToNextOrCurrentNode(nextOperatorDepartureOrArrival.getOperatorArrival().getArrivalTime());
                                        operator.setNextOrCurrentNode(travel.getArrivalNode());
                                        operator.setPreviousNode(travel.getPickupNode());
                                        operator.setHandling(true);
                                        if(Constants.PRINT_OUT_ACTIONS){
                                            System.out.println("Operator travel made: "+ travel+ ", toNode="+travel.getArrivalNode());
                                        }
                                        kpiTrackerDyanmic.increaseCarTotalTravelTimeDoneByOperator(travel.getArrivalTime() - travel.getDepartureTime());
                                    } else {
                                        operator.setPreviousNode(departureNode);
                                        operator.setNextOrCurrentNode(departureNode);
                                        if(Constants.PRINT_OUT_ACTIONS){
                                            System.out.println("Car missed by operator... operator will wait.");
                                        }
                                        this.kpiTrackerDyanmic.increaseNumberOfOperatorsAbandoned(subProblemNumber);
                                    }
                                }
                                else {
                                    //take car with low, going to a charging node
                                    Car car = findInNeedCarWithLowestBatteryInNode(((ParkingNode) departureNode));
                                    if (car!= null){
                                        ((ParkingNode) departureNode).getCarsInNeed().remove(car);
                                        car.setCurrentNextNode(travel.getArrivalNode());
                                        car.setPreviousNode(travel.getPickupNode());
                                        travel.setCar(car);
                                        travel.setPreviousTimeStep(nextOperatorHappeningTime);
                                        operatorTravels.put(operator,travel);
                                        operator.setArrivalTimeToNextOrCurrentNode(nextOperatorDepartureOrArrival.getOperatorArrival().getArrivalTime());
                                        operator.setNextOrCurrentNode(travel.getArrivalNode());
                                        operator.setPreviousNode(travel.getPickupNode());
                                        operator.setHandling(true);
                                        if(Constants.PRINT_OUT_ACTIONS){
                                            System.out.println("Operator travel made: "+ travel + ", toNode="+travel.getArrivalNode());
                                        }
                                        kpiTrackerDyanmic.increaseCarTotalTravelTimeDoneByOperator(travel.getArrivalTime() - travel.getDepartureTime());
                                        kpiTrackerDyanmic.addInNeedWaitingTime(car.getTimeInInNeedState());

                                    } else {
                                        operator.setPreviousNode(departureNode);
                                        operator.setNextOrCurrentNode(departureNode);
                                        if(Constants.PRINT_OUT_ACTIONS){
                                            System.out.println("Car missed by operator... operator will wait.");
                                        }
                                        this.kpiTrackerDyanmic.increaseNumberOfOperatorsAbandoned(subProblemNumber);
                                        operatorDepartures.put(operator,new ArrayList<>());
                                    }
                                }
                            }


                        } else {
                            //no car used in travel
                            if(departureNode.equals(operator.getNextOrCurrentNode())){
                                travel.setPreviousTimeStep(nextOperatorHappeningTime);
                                operatorTravels.put(operator,travel);
                                operator.setArrivalTimeToNextOrCurrentNode(nextOperatorDepartureOrArrival.getOperatorArrival().getArrivalTime());
                                operator.setNextOrCurrentNode(travel.getArrivalNode());
                                operator.setPreviousNode(travel.getPickupNode());
                                operator.setHandling(false);
                                if(Constants.PRINT_OUT_ACTIONS){
                                    System.out.println("Operator travel made on bike: "+ travel + ", toNode="+travel.getArrivalNode());
                                }
                                kpiTrackerDyanmic.increaseBikeTotalTravelTimeDoneByOperator(travel.getArrivalTime() - travel.getDepartureTime());
                            }

                        }
                    } else{
                        // last node in operator's path
                        operatorDepartures.get(nextOperatorDepartureOrArrival.getOperator()).remove(nextOperatorDepartureOrArrival);
                    }

                } else{
                    //operator arrives but the travel was not registered
                    OperatorTravel travel = operatorTravels.get(operator);
                    operatorTravels.remove(operator);
                    operatorDepartures.get(nextOperatorDepartureOrArrival.getOperator()).remove(nextOperatorDepartureOrArrival);
                    Node arrivalNode = nextOperatorDepartureOrArrival.getOperatorArrival().getNode();
                    operator.setNextOrCurrentNode(arrivalNode);
                    operator.setPreviousNode(arrivalNode);
                    operator.setTimeRemainingToCurrentNextNode(0);
                    operator.setHandling(false);
                    if(travel != null){
                        updateIdleTimesOperator(operator,time,previousTime);
                        operator.setArrivalTimeToNextOrCurrentNode(travel.getArrivalTime());
                        Car car = travel.getCar();
                        if(Constants.PRINT_OUT_ACTIONS){
                            System.out.println("Operator " + operator + " arrives with car " +car);
                        }
                        if(car != null){
                            operator.setWasHandlingToNextCurrentNode(true);
                            updateBatteryLevelOnCar(time,previousTime,car);
                            car.setPreviousNode(arrivalNode);
                            car.setCurrentNextNode(arrivalNode);
                            if(arrivalNode instanceof  ParkingNode){
                                if(car.getBatteryLevel() < Constants.SOFT_CHARGING_THRESHOLD){
                                    ((ParkingNode) arrivalNode).getCarsInNeed().add(car);
                                } else {
                                    ((ParkingNode) arrivalNode).getCarsRegular().add(car);
                                }

                            } else {
                                ((ChargingNode) arrivalNode).getCarsCurrentlyCharging().add(car);
                                this.kpiTrackerDyanmic.increaseNumberOfCarsSetToCharging(subProblemNumber);
                            }
                        } else {
                            // arrived with bike
                            operator.setWasHandlingToNextCurrentNode(false);
                        }
                    }
                }

            }
            else if(nextCustomerArrivalTime == earliestTime){
                time = nextCustomerArrivalTime;
                // Customer arrives with car
                customerTravels.remove(nextCustomerArrival);
                Node arrivalNode = nextCustomerArrival.getArrivalNode();
                Car car = nextCustomerArrival.getCar();
                updateBatteryLevelOnCar(time,previousTime,car);
                car.setCurrentNextNode(arrivalNode);
                car.setPreviousNode(arrivalNode);
                if(Constants.PRINT_OUT_ACTIONS){
                    System.out.println("Customer arrives with car "+ car + " in node " + arrivalNode);
                }
                if(car.getBatteryLevel() < Constants.SOFT_CHARGING_THRESHOLD){
                    // assuming that if close to charging station, customer sets to charging
                    if(problemInstance.getParkingNodeToChargingNode().get(arrivalNode)!=null){
                        //charging node nearby
                        double random = Math.random();
                        if(random < Constants.PROBABILITY_CUSTOMERS_CHARGE){
                            //set car to charging
                            ChargingNode cNode = problemInstance.getParkingNodeToChargingNode().get(arrivalNode);
                            if(checkIfCarIsAllowedToSetToChargingForCustomerInNode(cNode, operatorTravels)){
                                car.setCurrentNextNode(cNode);
                                car.setPreviousNode(cNode);
                                cNode.getCarsCurrentlyCharging().add(car);
                                this.kpiTrackerDyanmic.increaseNumberOfCarsSetToCharging(subProblemNumber);
                                if(Constants.PRINT_OUT_ACTIONS){
                                    System.out.println("Customer set a car to charging");
                                }
                            }else {
                                ((ParkingNode)arrivalNode).getCarsInNeed().add(car);
                            }
                        }else {
                            ((ParkingNode)arrivalNode).getCarsInNeed().add(car);
                        }
                    }else{
                        ((ParkingNode)arrivalNode).getCarsInNeed().add(car);
                    }
                } else {
                    ((ParkingNode)arrivalNode).getCarsRegular().add(car);
                }

            }
            updateBatteryLevels(time,previousTime);
            updateTimeInNeedState(time,previousTime);
            updateIdleTimesOperators(time,previousTime);


        }
        updateBatteryLevels(endTime,time);
        updateRemainingTravelTimesForOperators(endTime,operatorTravels);
        updateNumberOfCarsTakenByCustomers(customerTravels);
        updateTimeInNeedState(endTime,time);
        updateIdleTimesOperators(endTime,time);
    }

    private boolean checkIfCarIsAllowedToSetToChargingForCustomerInNode(ChargingNode node, HashMap<Operator,OperatorTravel> operatorTravels){
        int numberOfOperatorsPlanningToChargeNodes = 0;
        for (Operator operator: operatorTravels.keySet()) {
            OperatorTravel travel = operatorTravels.get(operator);
            if(travel.getArrivalNode().equals(node) && travel.getCar() != null){
                //operator is planning to charge
                numberOfOperatorsPlanningToChargeNodes ++;
            }
        }
        int availableSpacesRightNow = node.getNumberOfTotalChargingSlots() - node.getCarsCurrentlyCharging().size();
        return availableSpacesRightNow - numberOfOperatorsPlanningToChargeNodes > 0;
    }

    private void updateTimeInNeedState(double time, double previousTime){
        for (ParkingNode pNode : problemInstance.getParkingNodes()) {
            for (Car car: pNode.getCarsInNeed()) {
                car.setTimeInInNeedState(car.getTimeInInNeedState() + (time - previousTime));
            }
        }
    }

    private void updateIdleTimesOperator(Operator operator,double time, double previousTime){
        if(operator.getNextOrCurrentNode().equals(operator.getPreviousNode())){
            // stands still
            double idleTimeStart = operator.getArrivalTimeToNextOrCurrentNode();
            if(operator.getNextOrCurrentNode() instanceof ParkingNode){
                idleTimeStart = operator.wasHandlingToNextCurrentNode() ? idleTimeStart + problemInstance.getHandlingTimeP() : idleTimeStart;
                if(idleTimeStart > time){
                    return;
                }
                double newPreviousTime = previousTime > idleTimeStart ? previousTime : idleTimeStart;
                double timeDiff = time - newPreviousTime;
                if(time > idleTimeStart){
                    operator.setTotalIdleTime(operator.getTotalIdleTime() + timeDiff);
                }
            } else {
                idleTimeStart = operator.wasHandlingToNextCurrentNode() ? idleTimeStart + problemInstance.getHandlingTimeC() : idleTimeStart;
                if(idleTimeStart > time){
                    return;
                }
                double newPreviousTime = previousTime > idleTimeStart ? previousTime : idleTimeStart;
                double timeDiff = time - newPreviousTime;
                if(time > idleTimeStart){
                    operator.setTotalIdleTime(operator.getTotalIdleTime() + timeDiff);
                }
            }
        }
    }

    private void updateIdleTimesOperators(double time, double previousTime){
        for ( Operator operator : problemInstance.getOperators()) {
            updateIdleTimesOperator(operator, time, previousTime);
        }
    }


    private Car findInNeedCarWithLowestBatteryInNode(ParkingNode node){
        Car lowestBatteryCar = null;
        for (Car car : node.getCarsInNeed()) {
            if(lowestBatteryCar == null){
                lowestBatteryCar = car;
            } else if(car.getBatteryLevel() < lowestBatteryCar.getBatteryLevel()){
                lowestBatteryCar = car;
            }
        }
        return lowestBatteryCar;
    }

    public void updateNumberOfCarsTakenByCustomers(ArrayList<CustomerTravel> customerTravels){
        problemInstance.setNumberOfCarsTakenByCustomers(customerTravels.size());
    }

    public void updateRemainingTravelTimesForOperators(double endTime,HashMap<Operator,OperatorTravel> operatorTravels){
        for (Operator operator : problemInstance.getOperators()) {
            OperatorTravel travel = operatorTravels.get(operator);
            if(travel != null){
                double remainingTime = travel.getArrivalTime()> endTime ? travel.getArrivalTime()-endTime : 0;
                operator.setTimeRemainingToCurrentNextNode(remainingTime);

            } else{
                operator.setTimeRemainingToCurrentNextNode(0);
            }
        }
    }

    public void updateBatteryLevels(double time, double previousTime){
        for (Car car : problemInstance.getCars()) {
            if(!car.getPreviousNode().equals(car.getCurrentNextNode())){
                // car is on the run
                double newBatteryLevel = (car.getBatteryLevel() - (time - previousTime)*Constants.BATTERY_USED_PER_TIME_UNIT) > 0 ? (car.getBatteryLevel() - (time - previousTime)*Constants.BATTERY_USED_PER_TIME_UNIT) : 0;
                car.setBatteryLevel(newBatteryLevel);
            } else if(car.getCurrentNextNode() instanceof ChargingNode && car.getPreviousNode() instanceof ChargingNode){
                // car is charging
                car.setBatteryLevel(car.getBatteryLevel() + (time - previousTime)*Constants.BATTERY_CHARGED_PER_TIME_UNIT);
                car.setRemainingChargingTime((1.0-car.getBatteryLevel())*Constants.CHARGING_TIME_FULL);
                if(car.getBatteryLevel() >=1.0){
                    car.setRemainingChargingTime(0);
                    car.setBatteryLevel(1.0);
                    ChargingNode chargingNode = (ChargingNode) car.getCurrentNextNode();
                    ParkingNode parkingNode = problemInstance.getChargingToParkingNode().get(chargingNode);
                    chargingNode.getCarsCurrentlyCharging().remove(car);
                    parkingNode.getCarsRegular().add(car);
                    car.setCurrentNextNode(parkingNode);
                    car.setPreviousNode(parkingNode);
                }
            }
        }
    }

    private void updateBatteryLevelOnCar(double time, double previousTime, Car car){
        if(!car.getPreviousNode().equals(car.getCurrentNextNode())){
            // car is on the run
            double newBatteryLevel = (car.getBatteryLevel() - (time - previousTime)*Constants.BATTERY_USED_PER_TIME_UNIT) > 0 ? (car.getBatteryLevel() - (time - previousTime)*Constants.BATTERY_USED_PER_TIME_UNIT) : 0;
            car.setBatteryLevel(newBatteryLevel);
        } else if(car.getCurrentNextNode() instanceof ChargingNode && car.getPreviousNode() instanceof ChargingNode){
            // car is charging
            car.setBatteryLevel(car.getBatteryLevel() + (time - previousTime)*Constants.BATTERY_CHARGED_PER_TIME_UNIT);
            car.setRemainingChargingTime((1.0-car.getBatteryLevel())*Constants.CHARGING_TIME_FULL);
            if(car.getBatteryLevel() >=1.0){
                car.setRemainingChargingTime(0);
                car.setBatteryLevel(1.0);
                ChargingNode chargingNode = (ChargingNode) car.getCurrentNextNode();
                ParkingNode parkingNode = problemInstance.getChargingToParkingNode().get(chargingNode);
                chargingNode.getCarsCurrentlyCharging().remove(car);
                parkingNode.getCarsRegular().add(car);
                car.setCurrentNextNode(parkingNode);
                car.setPreviousNode(parkingNode);
            }
        }
    }

    private Car findAvailableCarForCustomerInNode(ParkingNode node){
        if(node.getCarsRegular().size()>0){
            return node.getCarsRegular().get(0);
        }
        for (Car car : node.getCarsInNeed()) {
            if(car.getBatteryLevel() > Constants.HARD_CHARGING_THRESHOLD){
                return car;
            }
        }
        return null;
    }

    private boolean isThereACarAvailableToBePickedUpAtNodeByCustomer(ParkingNode node,HashMap<Operator,OperatorTravel> operatorTravels,
                                                                     HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures, double time){
        //travels only contain operators travelling at the moment
        int carsNeededByOperatorsTheNextMinutes = 0;
        for (Operator operator : operatorTravels.keySet()) {
            OperatorTravel operatorTravel = operatorTravels.get(operator);
            if(operatorDepartures != null && operatorDepartures.get(operator) != null ){
                for (OperatorDeparture departure: operatorDepartures.get(operator)) {
                    Node arrivalNode = operatorTravel.getArrivalNode();
                    if(arrivalNode.equals(departure.getNode())){
                        if(departure.getDepartureTime() > time && departure.getDepartureTime() < time + Constants.LOCK_TIME_CAR_FOR_OPERATOR && departure.getNode().equals(node)){
                            //if handles to parking:
                            if(departure.getOperatorArrival() != null && departure.getOperatorArrival().getNode() instanceof ParkingNode && departure.isHandling()){
                                carsNeededByOperatorsTheNextMinutes +=1;
                                break;
                            }

                        }
                    }
                }
            }
        }
        //find number of cars in need that are available for customers:
        int inNeedAvailable = 0;
        for (ParkingNode pNode: problemInstance.getParkingNodes()) {
            for (Car car: pNode.getCarsInNeed()) {
                if(car.getBatteryLevel() > Constants.HARD_CHARGING_THRESHOLD){
                    inNeedAvailable++;
                }
            }
        }
        return node.getCarsRegular().size() + inNeedAvailable - carsNeededByOperatorsTheNextMinutes > 0;
    }

    private void removeOperatorDepartureArrival(OperatorTravel operatorTravel,  HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures){
        double arrivalTime = operatorTravel.getArrivalTime();
        Operator operator = operatorTravel.getOperator();
        OperatorDeparture departureToRemove = null;
        if(operatorDepartures != null && operatorDepartures.get(operator) != null){
            for (OperatorDeparture departure : operatorDepartures.get(operator)) {
                if(departure.getOperatorArrival() != null && departure.getOperatorArrival().getArrivalTime() == arrivalTime){
                    departureToRemove = departure;
                }
            }
        }
        if(departureToRemove != null){
            operatorDepartures.get(operator).remove(departureToRemove);
        }
    }

    private CustomerTravel findNextCustomerArrival(double time, double endTime, ArrayList<CustomerTravel> customerTravels){
        CustomerTravel earliestOperatorHappening = null;
        double earliestHappeningTime = 0;
        for (CustomerTravel customerTravel : customerTravels) {
            double arrivalTime = customerTravel.getArrivalTime();
            if(earliestOperatorHappening == null && arrivalTime >= time && arrivalTime < endTime){
                earliestOperatorHappening = customerTravel;
                earliestHappeningTime = arrivalTime;
            } else if (earliestOperatorHappening != null && arrivalTime < earliestHappeningTime && arrivalTime >= time && arrivalTime < endTime){
                earliestOperatorHappening = customerTravel;
                earliestHappeningTime = arrivalTime;
            }
        }
        return earliestOperatorHappening;
    }

    private OperatorDeparture findNextOperatorDepartureOrArrival(double time, double endTime, HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures){
        OperatorDeparture earliestOperatorHappening = null;
        double earliestHappeningTime = 0;
        for (Operator operator : operatorDepartures.keySet()) {
            for (OperatorDeparture departure : operatorDepartures.get(operator)) {
                double minTimeOverTimeLimit = findEarliestHappeningOverTime(departure,time);
                if(earliestOperatorHappening == null && minTimeOverTimeLimit >= time && minTimeOverTimeLimit < endTime ){
                    earliestOperatorHappening = departure;
                    earliestHappeningTime = minTimeOverTimeLimit;
                } else if ( earliestOperatorHappening != null && minTimeOverTimeLimit < earliestHappeningTime && minTimeOverTimeLimit >= time && minTimeOverTimeLimit < endTime){
                    earliestOperatorHappening = departure;
                    earliestHappeningTime = minTimeOverTimeLimit;
                }
            }
        }
        return earliestOperatorHappening;
    }

    private double findEarliestHappeningOverTime(OperatorDeparture departure,double time){
        double arrivalTime = ((departure.getOperatorArrival()!=null)?departure.getOperatorArrival().getArrivalTime() : departure.getDepartureTime());
        double departureTime = departure.getDepartureTime();
        arrivalTime = arrivalTime >= time ? arrivalTime : Double.MAX_VALUE;
        departureTime = departureTime >= time ? departureTime : Double.MAX_VALUE;
        return Math.min(arrivalTime,departureTime);
    }

    public DemandRequest findNextDemandRequest(double time, double endTime){
        //after time
        DemandRequest nextDemandRequest = null;
        for (ParkingNode pNode : problemInstance.getParkingNodes()) {
            if(simulationModel.getDemandRequests().get(pNode) != null){
                for (DemandRequest req: simulationModel.getDemandRequests().get(pNode)) {
                    if(nextDemandRequest == null && req.getTime() >= time && req.getTime() <endTime ){
                        nextDemandRequest = req;
                    } else {
                        if(nextDemandRequest != null && req.getTime() >= time && req.getTime() < nextDemandRequest.getTime() && req.getTime() <endTime ){
                            nextDemandRequest = req;
                        }
                    }
                }
            }

        }
        return nextDemandRequest;
    }

    private HashMap<Operator,ArrayList<OperatorDeparture>> readOperatorArrivalsAndDepartures(int startTime){
        HashMap<Operator,ArrayList<OperatorArrival>> arrivals = new HashMap<>();

        try {
            FileReader fileReader = new FileReader(Constants.MOSEL_OUTPUT_REAL + fileName + ".txt");
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            boolean noIntegerSolutionFound = false;
            while (line != null) {
                if(noIntegerSolutionFound){
                    arrivals = new HashMap<>();
                    break;
                }
                line.trim();
                int lenOfLine = line.length();
                if(line.substring(lenOfLine-1,lenOfLine).contains(",")){
                    noIntegerSolutionFound= true;
                    arrivals = new HashMap<>();
                    if(Constants.PRINT_OUT_ACTIONS){
                        System.out.println("No Integer Solution found");
                    }
                    break;
                }
                String[] stringList = line.split(":");
                int operatorId = Integer.parseInt(stringList[0].trim()) - (1-Constants.START_INDEX);
                Operator operator = problemInstance.getOperatorMap().get(operatorId);
                String[] stringList2 = stringList[1].trim().split("\\),\\(");
                for (String tuple : stringList2) {
                    tuple = tuple.replace("(","");
                    tuple = tuple.replace(")","");
                    String[] tupleList = tuple.split(",");
                    int nodeId = Integer.parseInt(tupleList[0]) - (1-Constants.START_INDEX);
                    Node node = problemInstance.getNodeMap().get(nodeId);
                    if(node == null){
                        continue;
                    }
                    boolean isHandling;
                    try{
                        isHandling = Integer.parseInt(tupleList[2])==1;
                    } catch (NumberFormatException e){
                        arrivals = new HashMap<>();
                        if(Constants.PRINT_OUT_ACTIONS){
                            System.out.println("No Integer Solution found");
                        }
                        noIntegerSolutionFound = true;
                        break;
                    }
                    double arrivalTime = Double.parseDouble(tupleList[3]) + startTime;
                    OperatorArrival operatorArrival = new OperatorArrival(arrivalTime,isHandling,node,operator);

                    addArrivalsToMap(arrivals,operatorArrival);
                }
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            // Make departures
            double travelTimeBetween, departureTime;
            HashMap<Operator,ArrayList<OperatorDeparture>> departures = new HashMap<>();
            for (Operator operator : arrivals.keySet()) {
                for (int operatorArrivalIndex = 0; operatorArrivalIndex < arrivals.get(operator).size()-1; operatorArrivalIndex++) {
                    OperatorArrival fromArrival = arrivals.get(operator).get(operatorArrivalIndex);
                    OperatorArrival toArrival = arrivals.get(operator).get(operatorArrivalIndex + 1);
                    travelTimeBetween = toArrival.isHandling() ? problemInstance.getTravelTimesCar().get(fromArrival.getNode().getNodeId() - Constants.START_INDEX).get(toArrival.getNode().getNodeId() - Constants.START_INDEX)
                            : problemInstance.getTravelTimesBike().get(fromArrival.getNode().getNodeId() - Constants.START_INDEX).get(toArrival.getNode().getNodeId() - Constants.START_INDEX);
                    departureTime = toArrival.getArrivalTime() - travelTimeBetween;
                    OperatorDeparture departure = new OperatorDeparture(fromArrival.getNode(), operator, toArrival.isHandling(), departureTime, toArrival);
                    addDepartureToMap(departures,departure);
                }
                OperatorArrival fromArrival = arrivals.get(operator).get(arrivals.get(operator).size()-1);
                departureTime = fromArrival.getArrivalTime();
                OperatorDeparture departure = new OperatorDeparture(fromArrival.getNode(), operator, false, departureTime, null);
                addDepartureToMap(departures,departure);
            }
            br.close();
            return departures;


        }catch (IOException e){
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }

    private OperatorTravel findNextOperatorTravelArrival(double time, double endTime, HashMap<Operator,OperatorTravel> operatorTravels){
        OperatorTravel earliestOperatorArrival = null;
        for (Operator operator : operatorTravels.keySet()) {
            OperatorTravel operatorTravel = operatorTravels.get(operator);
            if(earliestOperatorArrival == null && operatorTravel.getArrivalTime() >= time && operatorTravel.getArrivalTime() < endTime){
                earliestOperatorArrival = operatorTravel;
            } else if (earliestOperatorArrival != null && operatorTravel.getArrivalTime() >= time && operatorTravel.getArrivalTime() < endTime && operatorTravel.getArrivalTime() < earliestOperatorArrival.getArrivalTime()){
                earliestOperatorArrival = operatorTravel;
            }
        }
        return earliestOperatorArrival;

    }

    private void addDepartureToMap(HashMap<Operator,ArrayList<OperatorDeparture>> departures, OperatorDeparture departure){
        if (departures.get(departure.getOperator()) == null){
            departures.put(departure.getOperator(), new ArrayList<>());
        }
        departures.get(departure.getOperator()).add(departure);
    }

    private void addArrivalsToMap(HashMap<Operator,ArrayList<OperatorArrival>> arrivals, OperatorArrival operatorArrival){
        if (arrivals.get(operatorArrival.getOperator()) == null){
            arrivals.put(operatorArrival.getOperator(), new ArrayList<>());
        }
        arrivals.get(operatorArrival.getOperator()).add(operatorArrival);
    }

    private void updateOptimalNumberOfCarsInParking(double time){
        predictNumberOfCarsPickedUpNextPeriod(time);
        double totalCarsPredicted = 0;
        double nextPeriodDemand;
        HashMap<ParkingNode,Double> map = new HashMap<>();
        for (ParkingNode pNode: problemInstance.getParkingNodes()) {
            if(pNode.getDemandGroup().equals(Constants.nodeDemandGroup.MIDDAY_RUSH)){
                nextPeriodDemand = simulationModel.findExpectedNumberOfArrivalsMiddayRushBetween(time+Constants.TIME_LIMIT_STATIC_PROBLEM, time + Constants.TIME_LIMIT_STATIC_PROBLEM*2);
            } else if (pNode.getDemandGroup().equals(Constants.nodeDemandGroup.MORNING_RUSH)){
                nextPeriodDemand = simulationModel.findExpectedNumberOfArrivalsMorningRushBetween(time+Constants.TIME_LIMIT_STATIC_PROBLEM, time + Constants.TIME_LIMIT_STATIC_PROBLEM*2);
            } else {
                nextPeriodDemand = simulationModel.findExpectedNumberOfArrivalsNormalBetween(time+Constants.TIME_LIMIT_STATIC_PROBLEM, time + Constants.TIME_LIMIT_STATIC_PROBLEM*2);
            }
            totalCarsPredicted+= nextPeriodDemand/2;
            map.put(pNode,nextPeriodDemand/2);
        }
        int numberOfCarsAvailableNextPeriod = 0;
        for (Car car : problemInstance.getCars()) {
            if (car.getRemainingChargingTime()==0){
                numberOfCarsAvailableNextPeriod+=1;
            }
        }
        for (ChargingNode cNode: problemInstance.getChargingNodes()) {
            for (Car car: cNode.getCarsCurrentlyCharging()) {
                if(car.getRemainingChargingTime() < Constants.TIME_LIMIT_STATIC_PROBLEM){
                    numberOfCarsAvailableNextPeriod+=1;
                }
            }
        }
        for (ParkingNode pNode : problemInstance.getParkingNodes()) {
            numberOfCarsAvailableNextPeriod -= pNode.getPredictedNumberOfCarsDemandedThisPeriod();
        }
        for (ParkingNode pNode : map.keySet()) {
            int demandInteger = (int) Math.floor(map.get(pNode) / totalCarsPredicted * numberOfCarsAvailableNextPeriod);
            if(time + Constants.TIME_LIMIT_STATIC_PROBLEM*2 > Constants.END_TIME){
                demandInteger = (int) Math.floor(numberOfCarsAvailableNextPeriod / problemInstance.getParkingNodes().size());
            }
            pNode.setIdealNumberOfAvailableCarsThisPeriod(demandInteger);
        }
    }

    private void predictNumberOfCarsPickedUpNextPeriod(double time){
        double nextPeriodDemand;
        if(time + Constants.TIME_LIMIT_STATIC_PROBLEM <= Constants.END_TIME){
            for (ParkingNode pNode: problemInstance.getParkingNodes()) {
                if(pNode.getDemandGroup().equals(Constants.nodeDemandGroup.MIDDAY_RUSH)){
                    nextPeriodDemand = simulationModel.findExpectedNumberOfArrivalsMiddayRushBetween(time, time + Constants.TIME_LIMIT_STATIC_PROBLEM);
                } else if (pNode.getDemandGroup().equals(Constants.nodeDemandGroup.MORNING_RUSH)){
                    nextPeriodDemand = simulationModel.findExpectedNumberOfArrivalsMorningRushBetween(time, time + Constants.TIME_LIMIT_STATIC_PROBLEM);
                } else {
                    nextPeriodDemand = simulationModel.findExpectedNumberOfArrivalsNormalBetween(time, time + Constants.TIME_LIMIT_STATIC_PROBLEM);
                }
                int demandInt = (int) Math.floor(nextPeriodDemand/2);
                pNode.setPredictedNumberOfCarsDemandedThisPeriod(demandInt);
            }
        } else {
            for (ParkingNode pNode: problemInstance.getParkingNodes()) {
                pNode.setPredictedNumberOfCarsDemandedThisPeriod(1);
            }
        }
    }

    public ProblemInstance getProblemInstance(){
        return this.problemInstance;
    }

    public KPITrackerDynamic getKpiTrackerDyanmic() {
        return kpiTrackerDyanmic;
    }

}

package tio4500;

import constants.Constants;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class DynamicProblem {

    private ProblemInstance problemInstance;
    private SimulationModel simulationModel;

    public DynamicProblem(ProblemInstance problemInstance, SimulationModel simulationModel) {
        this.problemInstance = problemInstance;
        this.simulationModel = simulationModel;
    }

    public void solve() {


        int subproblemNo = 1;
        ArrayList<CustomerTravel> customerTravels = new ArrayList<>();
        for (int time = Constants.START_TIME; time < Constants.END_TIME; time += Constants.TIME_INCREMENTS) {
            System.out.println("\n\n");
            System.out.println("Sub problem "+subproblemNo+" starting at time: "+time);
            updateOptimalNumberOfCarsInParking(time);
            predictNumberOfCarsPickedUpNextPeriod(time);
            problemInstance.writeProblemInstanceToFile();
            System.out.println("State before solving mosel: "+problemInstance + "\n");
            StaticProblem staticProblem = new StaticProblem();
            staticProblem.compile();
            staticProblem.solve();
            //System.out.println("Objective value: "+staticProblem.getModel().getObjectiveValue());
            doPeriodActions(time, time + Constants.TIME_INCREMENTS, customerTravels);
            subproblemNo++;
        }
    }

    public void doPeriodActions(int startTime, int endTime, ArrayList<CustomerTravel> customerTravels ){
        double time = startTime;
        HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures = readOperatorArrivalsAndDepartures(startTime);
        HashMap<Operator,OperatorTravel> operatorTravels = new HashMap<>();

        double previousTime;
        while (time < endTime){
            previousTime = time;
            DemandRequest nextDemandRequest = findNextDemandRequest(time);
            OperatorDeparture nextOperatorDepartureOrArrival = findNextOperatorDepartureOrArrival(time,operatorDepartures);
            CustomerTravel nextCustomerArrival = findNextCustomerArrival(time,customerTravels);
            if(nextDemandRequest == null && nextOperatorDepartureOrArrival == null && nextCustomerArrival == null){
                break;
            }
            double nextDemandReqTime = nextDemandRequest != null ? nextDemandRequest.getTime() : Double.MAX_VALUE;
            double nextOperatorHappeningTime = nextOperatorDepartureOrArrival != null ? findEarliestHappeningOverTime(nextOperatorDepartureOrArrival,time) : Double.MAX_VALUE;
            double nextCustomerArrivalTime = nextCustomerArrival != null ? nextCustomerArrival.getArrivalTime() : Double.MAX_VALUE;
            double earliestTime = Double.min(Double.min(nextDemandReqTime, nextOperatorHappeningTime),nextCustomerArrivalTime);
            time = earliestTime;

            if(nextDemandReqTime == earliestTime){
                // customer would like to pick up car
                //System.out.println("demand reg earliest");
                simulationModel.getDemandRequests().get(nextDemandRequest.getNode()).remove(nextDemandRequest);
                ParkingNode pNode = nextDemandRequest.getNode();
                if(isThereACarAvailableToBePickedUpAtNodeByCustomer(pNode,operatorTravels,operatorDepartures,time)){
                    // Do customer travel:
                    Car availableCar = findAvailableCarForCustomerInNode(pNode);
                    int rndIndex = new Random().nextInt(problemInstance.getParkingNodes().size());
                    ParkingNode arrivalNode = problemInstance.getParkingNodes().get(rndIndex);
                    double travelTime = problemInstance.getTravelTimesCar().get(pNode.getNodeId() - Constants.START_INDEX).get(arrivalNode.getNodeId()-Constants.START_INDEX);
                    double arrivalTime = nextDemandReqTime + travelTime;
                    CustomerTravel newCustomerTravel = new CustomerTravel(nextDemandReqTime,pNode,arrivalTime,arrivalNode);
                    Car travelCar = findAvailableCarForCustomerInNode(pNode);
                    if(travelCar != null){
                        newCustomerTravel.setCar(travelCar);
                        travelCar.setPreviousNode(pNode);
                        travelCar.setCurrentNextNode(arrivalNode);
                        customerTravels.add(newCustomerTravel);
                        time = nextDemandReqTime;
                        pNode.getCarsRegular().remove(travelCar);
                        //System.out.println("Customer Travel Added from node " + pNode + " at time: "+ nextDemandReqTime);
                    }
                }
            }
            else if(nextOperatorHappeningTime == earliestTime){
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
                        if(arrival.isHandling()){
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
                                        operator.setNextOrCurrentNode(travel.getArrivalNode());
                                        operator.setPreviousNode(travel.getPickupNode());
                                        operator.setHandling(true);
                                        // System.out.println("Operator travel made: "+ travel+ ", toNode="+travel.getArrivalNode());
                                    } else {
                                        // System.out.println("Car missed by operator... operator will wait.");
                                    }
                                }
                                else {
                                    //take car with low battery
                                    Car car = ((ParkingNode) departureNode).getCarsInNeed().remove(0);
                                    if (car!= null){
                                        car.setCurrentNextNode(travel.getArrivalNode());
                                        car.setPreviousNode(travel.getPickupNode());
                                        travel.setCar(car);
                                        travel.setPreviousTimeStep(nextOperatorHappeningTime);
                                        operatorTravels.put(operator,travel);
                                        operator.setNextOrCurrentNode(travel.getArrivalNode());
                                        operator.setPreviousNode(travel.getPickupNode());
                                        operator.setHandling(true);
                                        // System.out.println("Operator travel made: "+ travel + ", toNode="+travel.getArrivalNode());
                                    }
                                }
                            }


                        } else {
                            //no car used in travel
                            travel.setPreviousTimeStep(nextOperatorHappeningTime);
                            operatorTravels.put(operator,travel);
                            operator.setNextOrCurrentNode(travel.getArrivalNode());
                            operator.setPreviousNode(travel.getPickupNode());
                            operator.setHandling(false);
                            //System.out.println("Operator travel made: "+ travel + ", toNode="+travel.getArrivalNode());
                        }
                    } else{
                        // last node in operator's path
                        operatorDepartures.get(nextOperatorDepartureOrArrival.getOperator()).remove(nextOperatorDepartureOrArrival);
                    }

                } else{
                    //operator arrives
                    OperatorTravel travel = operatorTravels.get(operator);
                    operatorTravels.remove(operator);
                    operatorDepartures.get(nextOperatorDepartureOrArrival.getOperator()).remove(nextOperatorDepartureOrArrival);
                    Node arrivalNode = nextOperatorDepartureOrArrival.getOperatorArrival().getNode();
                    operator.setNextOrCurrentNode(arrivalNode);
                    operator.setPreviousNode(arrivalNode);
                    operator.setTimeRemainingToCurrentNextNode(0);
                    operator.setHandling(false);
                    if(travel != null){
                        Car car = travel.getCar();
                        if(car != null){
                            car.setPreviousNode(arrivalNode);
                            car.setCurrentNextNode(arrivalNode);
                            if(arrivalNode instanceof  ParkingNode){
                                if(car.getBatteryLevel() < Constants.HARD_CHARGING_THRESHOLD){
                                    ((ParkingNode) arrivalNode).getCarsInNeed().add(car);
                                } else {
                                    ((ParkingNode) arrivalNode).getCarsRegular().add(car);
                                }

                            } else {
                                ((ChargingNode) arrivalNode).getCarsCurrentlyCharging().add(car);
                            }
                        }
                    }
                }
                time = nextOperatorHappeningTime;
            }
            else if(nextCustomerArrivalTime == earliestTime){
                // Customer arrives with car
                //  System.out.println("nextCustomerArrivalTime earliest");
                customerTravels.remove(nextCustomerArrival);
                Node arrivalNode = nextCustomerArrival.getArrivalNode();
                Car car = nextCustomerArrival.getCar();
                car.setCurrentNextNode(arrivalNode);
                car.setPreviousNode(arrivalNode);
                if(car.getBatteryLevel() < Constants.HARD_CHARGING_THRESHOLD){
                    // assuming that if close to charging station, customer sets to charging
                    ((ParkingNode)arrivalNode).getCarsInNeed().add(car);
                } else {
                    ((ParkingNode)arrivalNode).getCarsRegular().add(car);
                }
            }

            updateBatteryLevels(time,previousTime);
        }
        updateBatteryLevels(endTime,time);
        updateRemainingTravelTimesForOperators(startTime,operatorTravels);
    }


    public void updateRemainingTravelTimesForOperators(double startTime, HashMap<Operator,OperatorTravel> operatorTravels){
        double endTime = startTime + Constants.TIME_INCREMENTS;
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
                car.setBatteryLevel(car.getBatteryLevel() - (time - previousTime)*Constants.BATTERY_USED_PER_TIME_UNIT);
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



    private Car findAvailableCarForCustomerInNode(ParkingNode node){
        if(node.getCarsRegular().size()>0){
            return node.getCarsRegular().get(0);
        }else{
            return null;
        }
    }

    private boolean isThereACarAvailableToBePickedUpAtNodeByCustomer(ParkingNode node,HashMap<Operator,OperatorTravel> operatorTravels,
                                                                     HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures, double time){
        //travels only contain operators travelling at the moment
        int carsNeededByOperatorsTheNextMinutes = 0;
        for (Operator operator : operatorTravels.keySet()) {
            OperatorTravel operatorTravel = operatorTravels.get(operator);
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
        return node.getCarsRegular().size() - carsNeededByOperatorsTheNextMinutes > 0;

    }

    private CustomerTravel findNextCustomerArrival(double time, ArrayList<CustomerTravel> customerTravels){
        CustomerTravel earliestOperatorHappening = null;
        double earliestHappeningTime = 0;
        for (CustomerTravel customerTravel : customerTravels) {
            double arrivalTime = customerTravel.getArrivalTime();
            if(earliestOperatorHappening == null && arrivalTime >= time){
                earliestOperatorHappening = customerTravel;
                earliestHappeningTime = arrivalTime;
            } else if (arrivalTime < earliestHappeningTime && arrivalTime >= time){
                earliestOperatorHappening = customerTravel;
                earliestHappeningTime = arrivalTime;
            }
        }
        return earliestOperatorHappening;
    }





    private OperatorDeparture findNextOperatorDepartureOrArrival(double time, HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures){
        OperatorDeparture earliestOperatorHappening = null;
        double earliestHappeningTime = 0;
        for (Operator operator : operatorDepartures.keySet()) {
            for (OperatorDeparture departure : operatorDepartures.get(operator)) {
                double minTimeOverTimeLimit = findEarliestHappeningOverTime(departure,time);
                if(earliestOperatorHappening == null && minTimeOverTimeLimit >= time){
                    earliestOperatorHappening = departure;
                    earliestHappeningTime = minTimeOverTimeLimit;
                } else if (minTimeOverTimeLimit < earliestHappeningTime && minTimeOverTimeLimit >= time){
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

    public DemandRequest findNextDemandRequest(double time){
        //after time
        DemandRequest nextDemandRequest = null;
        for (ParkingNode pNode : problemInstance.getParkingNodes()) {
            for (DemandRequest req: simulationModel.getDemandRequests().get(pNode)) {
                if(nextDemandRequest == null && req.getTime() >= time){
                    nextDemandRequest = req;
                } else {
                    if(req.getTime() >= time && req.getTime() < nextDemandRequest.getTime() ){
                        nextDemandRequest = req;
                    }
                }
            }
        }
        return nextDemandRequest;
    }

    private HashMap<Operator,ArrayList<OperatorDeparture>> readOperatorArrivalsAndDepartures(int startTime){
        HashMap<Operator,ArrayList<OperatorArrival>> arrivals = new HashMap<>();

        try {
            FileReader fileReader = new FileReader(Constants.MOSEL_OUTPUT + Constants.OUTPUT_REAL_SERVICE_PATHS + Integer.toString(Constants.EXAMPLE_NUMBER) + ".txt");
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                line.trim();
                int lenOfLine = line.length();
                if(line.substring(lenOfLine-3,lenOfLine).contains("->")){
                    arrivals = new HashMap<>();
                    System.out.println("No Integer Solution found");
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
                        System.out.println("No Integer Solution found");
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
            totalCarsPredicted+= nextPeriodDemand;
            map.put(pNode,nextPeriodDemand);
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
                int demandInt = (int) Math.round(nextPeriodDemand);
                pNode.setPredictedNumberOfCarsDemandedThisPeriod(demandInt);
            }
        } else {
            for (ParkingNode pNode: problemInstance.getParkingNodes()) {
                pNode.setPredictedNumberOfCarsDemandedThisPeriod(1);
            }
        }

    }

}

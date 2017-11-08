package tio4500;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
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

        System.out.println();
        int subproblemNo = 0;
        ArrayList<CustomerTravel> customerTravels = new ArrayList<>();
        for (int time = Constants.START_TIME; time < Constants.END_TIME; time += Constants.TIME_INCREMENTS) {
            problemInstance.writeProblemInstanceToFile();
            StaticProblem staticProblem = new StaticProblem();
            staticProblem.compile();
            staticProblem.solve();
            //generateNextSubproblem();
            //System.out.println("Objective value: "+staticProblem.getModel().getObjectiveValue());


            //TODO: update all states until next iteration
            System.out.println();
            System.out.println("time: "+time);
            doPeriodActions(time, time + Constants.TIME_INCREMENTS, customerTravels);


            subproblemNo++;
        }
    }

    public void doPeriodActions(int startTime, int endTime, ArrayList<CustomerTravel> customerTravels ){
        double time = startTime;
        HashMap<Operator,ArrayList<OperatorDeparture>> operatorDepartures = readOperatorArrivalsAndDepartures(startTime);
        HashMap<Operator,OperatorTravel> operatorTravels = new HashMap<>();

        double previousTime = time;
        while (time < endTime){
            previousTime = time;
            System.out.println();
            System.out.println(time);

            DemandRequest nextDemandRequest = findNextDemandRequest(time);
            OperatorDeparture nextOperatorDepartureOrArrival = findNextOperatorDepartureOrArrival(time,operatorDepartures);
            CustomerTravel nextCustomerArrival = findNextCustomerArrival(time,customerTravels);

            System.out.println("NextDemand req: "+nextDemandRequest);
            System.out.println("Earliest operator departure/arrival: "+ nextOperatorDepartureOrArrival);
            System.out.println("Next customer arrival: "+ nextCustomerArrival);

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
                System.out.println("demand reg earliest");
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
                        travelCar.setTimeRemainingToCurrentNextNode(travelTime);
                        travelCar.setPreviousTimeStep(nextDemandReqTime);
                        customerTravels.add(newCustomerTravel);
                        time = nextDemandReqTime;
                        pNode.getCarsRegular().remove(travelCar);
                        System.out.println("Customer Travel Added from node " + pNode + " at time: "+ nextDemandReqTime);
                    }
                }
            }
            else if(nextOperatorHappeningTime == earliestTime){
                System.out.println("next operator happening earliest");
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

                            if(arrivalNode instanceof ParkingNode){
                                Car car = ((ParkingNode) departureNode).getCarsRegular().remove(0);
                                if (car != null){
                                    car.setPreviousNode(travel.getPickupNode());
                                    car.setCurrentNextNode(travel.getArrivalNode());
                                    travel.setCar(car);
                                    travel.setPreviousTimeStep(nextOperatorHappeningTime);
                                    operatorTravels.put(operator,travel);
                                    System.out.println("Operator travel made: "+ travel);
                                } else {
                                    System.out.println("Car missed by operator... operator will wait.");
                                }
                            }
                            else {
                                System.out.println(departureNode +""+ arrivalNode +""+ operator +""+travel);
                                Car car = ((ParkingNode) departureNode).getCarsInNeed().remove(0);
                                if (car!= null){
                                    car.setCurrentNextNode(travel.getArrivalNode());
                                    car.setPreviousNode(travel.getPickupNode());
                                    travel.setCar(car);
                                    travel.setPreviousTimeStep(nextOperatorHappeningTime);
                                    operatorTravels.put(operator,travel);
                                    System.out.println("Operator travel made: "+ travel);
                                }
                            }
                        } else {
                            //no car
                            travel.setPreviousTimeStep(nextOperatorHappeningTime);
                            operatorTravels.put(operator,travel);
                            System.out.println("Operator travel made: "+ travel);
                        }
                    } else{
                        operatorDepartures.get(nextOperatorDepartureOrArrival.getOperator()).remove(nextOperatorDepartureOrArrival);
                    }

                } else{
                    //operator arrives
                    System.out.println("Operator: " + operator);
                    OperatorTravel travel = operatorTravels.get(operator);
                    if (travel != null){
                        operatorTravels.remove(operator);
                        operatorDepartures.get(nextOperatorDepartureOrArrival.getOperator()).remove(nextOperatorDepartureOrArrival);
                        Node arrivalNode = travel.getArrivalNode();
                        operator.setNextOrCurrentNode(arrivalNode);
                        operator.setPreviousNode(arrivalNode);
                        operator.setTimeRemainingToCurrentNextNode(0);
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
                System.out.println("nextCustomerArrivalTime earliest");
                customerTravels.remove(nextCustomerArrival);
                Node arrivalNode = nextCustomerArrival.getArrivalNode();
                Car car = nextCustomerArrival.getCar();
                car.setCurrentNextNode(arrivalNode);
                car.setPreviousNode(arrivalNode);
                if(car.getBatteryLevel() < Constants.HARD_CHARGING_THRESHOLD){
                    ((ParkingNode)arrivalNode).getCarsInNeed().add(car);
                } else {
                    ((ParkingNode)arrivalNode).getCarsRegular().add(car);
                }
            }
            // TODO: do something to update newly charged cars
            updateBatteryLevels(time,previousTime);
        }
    }

    public void updateBatteryLevels(double time, double previousTime){
        System.out.println(time + " " + previousTime);
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
                        if(departure.getOperatorArrival().getNode() instanceof ParkingNode && departure.isHandling()){
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



    private OperatorTravel findNextOperatorArrival(double time,HashMap<Operator,ArrayList<OperatorTravel>> operatorTravels){
        OperatorTravel earliestOperatorHappening = null;
        double earliestHappeningTime = 0;
        for (Operator operator : operatorTravels.keySet()) {
            for (OperatorTravel operatorTravel : operatorTravels.get(operator)) {
                double arrivalTime = operatorTravel.getArrivalTime();
                if(earliestOperatorHappening == null && arrivalTime >= time){
                    earliestOperatorHappening = operatorTravel;
                    earliestHappeningTime = arrivalTime;
                } else if (arrivalTime < earliestHappeningTime && arrivalTime >= time){
                    earliestOperatorHappening = operatorTravel;
                    earliestHappeningTime = arrivalTime;
                }
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

    public void generateNextSubproblem() {
        throw new NotImplementedException();
    }

    private HashMap<Operator,ArrayList<OperatorDeparture>> readOperatorArrivalsAndDepartures(int startTime){
        HashMap<Operator,ArrayList<OperatorArrival>> arrivals = new HashMap<>();

        try {
            FileReader fileReader = new FileReader(Constants.MOSEL_OUTPUT + Constants.OUTPUT_REAL_SERVICE_PATHS);
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {

                line.trim();
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
                    boolean isHandling = Integer.parseInt(tupleList[2])==1;
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

}

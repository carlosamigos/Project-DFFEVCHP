package tio4500;

import constants.Constants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tio4500.simulations.DemandRequest;
import tio4500.simulations.Entities.Operator;
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
        for (int time = Constants.START_TIME; time < Constants.START_TIME + Constants.TOTAL_TIME_DURING_DAY; time += Constants.TIME_INCREMENTS) {
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
        HashMap<Operator,ArrayList<OperatorTravel>> operatorTravels = new HashMap<>();


        while (time < endTime){

            //do something to update newly charged cars

            System.out.println(time);
            DemandRequest nextDemandRequest = findNextDemandRequest(time);
            OperatorDeparture nextOperatorDepartureOrArrival = findNextOperatorDepartureOrArrival(time,operatorDepartures);
            CustomerTravel nextCustomerArrival = findNextCustomerArrival(time,customerTravels);


            System.out.println("NextDemand req: "+nextDemandRequest);
            System.out.println("Earliest departure: "+ nextOperatorDepartureOrArrival);
            System.out.println("Next customer arrival: "+ nextCustomerArrival);


            if(nextDemandRequest == null && nextOperatorDepartureOrArrival == null && nextCustomerArrival == null){
                break;
            }

            double nextDemandReqTime = nextDemandRequest != null ? nextDemandRequest.getTime() : Double.MAX_VALUE;
            double nextOperatorHappeningTime = nextOperatorDepartureOrArrival != null ? findEarliestHappeningOverTime(nextOperatorDepartureOrArrival,time) : Double.MAX_VALUE;
            double nextCustomerArrivalTime = nextCustomerArrival != null ? nextCustomerArrival.getArrivalTime() : Double.MAX_VALUE;

            double earliestTime = Double.min(Double.min(nextDemandReqTime, nextOperatorHappeningTime),nextCustomerArrivalTime);
            time +=10;



        }
    }


    private CustomerTravel findNextCustomerArrival(double time, ArrayList<CustomerTravel> customerTravels){

        return null;
    }

    private void doOneAction(double time, DemandRequest nextDemandRequest, OperatorDeparture nextOperatorDeparture,
                             CustomerTravel nextCustomerArrival, OperatorTravel nextOperatorArrival,
                             HashMap<Operator,ArrayList<OperatorTravel>> operatorTravels, HashMap<Operator,
            ArrayList<OperatorDeparture>> operatorDepartures, ArrayList<CustomerTravel> customerTravels){




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
        double arrivalTime = departure.getOperatorArrival().getArrivalTime();
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
                    OperatorDeparture departure = new OperatorDeparture(fromArrival.getNode(), operator, departureTime, toArrival);
                    addDepartureToMap(departures,departure);
                }
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

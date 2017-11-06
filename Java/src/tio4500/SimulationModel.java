package tio4500;


import com.sun.tools.internal.jxc.ap.Const;
import constants.Constants;
import tio4500.simulations.DemandRequest.DemandRequest;
import tio4500.simulations.Nodes.ParkingNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class SimulationModel {

    private int dayNumber;
    private ProblemInstance problemInstance;
    private HashMap<ParkingNode,ArrayList<DemandRequest>> demandRequests;

    public SimulationModel(int dayNumber, ProblemInstance problemInstance) {
        this.dayNumber = dayNumber;
        this.problemInstance = problemInstance;
        this.demandRequests = new HashMap<>();
    }

    public void createNewDaySimulationModel(){
        System.out.println("Creating new simulation model...");
        //simulate pickups
        for (ParkingNode pNode : problemInstance.getParkingNodes()) {
            createAllDemandRequestsForNode(pNode);
        }
        System.out.println(demandRequests);
        System.out.println("Finished creating new simulation model...");

    }

    public void saveDaySimulationModel(){
        System.out.println("Saving simulation model");
        try{
            NumberFormat formatter = new DecimalFormat("#0.000");
            PrintWriter writer = new PrintWriter(Constants.SIMULATIONS_FOLDER + Constants.DEMAND_REQUESTS + "_day_"+Integer.toString(dayNumber)+"_probleminstance_"+Integer.toString(problemInstance.getExampleNumber())+".txt", "UTF-8");
            for (ParkingNode pNode : demandRequests.keySet()) {
                for (DemandRequest req : demandRequests.get(pNode)) {
                    writer.println(Integer.toString(req.getNode().getNodeId())+","+formatter.format(req.getTime()));
                }
            }
            writer.close();
        }
        catch (IOException e){
            System.out.println("Simulation day could not be saved");
        }




    }

    public void readSimulationModelFromFile(){
        System.out.println("Reading simulation model from file");

    }

    private double getDemandRateForNodeAtTime(ParkingNode parkingNode, double time){
        if(parkingNode.getDemandGroup()== Constants.nodeDemandGroup.MIDDAY_RUSH){
            return (Constants.HIGH_RATE_LAMBDA - Constants.LOW_RATE_LAMBDA)*Math.sin((time-Constants.START_TIME)*Math.asin(1)/Constants.TOTAL_TIME_DURING_DAY)+Constants.LOW_RATE_LAMBDA;
        } else if (parkingNode.getDemandGroup()== Constants.nodeDemandGroup.MORNING_RUSH){
            return (Constants.HIGH_RATE_LAMBDA - Constants.LOW_RATE_LAMBDA)*Math.cos((time-Constants.START_TIME)*Math.asin(1)/Constants.TOTAL_TIME_DURING_DAY)+Constants.LOW_RATE_LAMBDA;
        } else {
            return Constants.MEDIUM_RATE_LAMBDA;
        }
    }

    private double drawFromExponentialDistribution(double rate){
        return -Math.log(1.0 - Math.random())/rate;
    }

    private void createAllDemandRequestsForNode(ParkingNode parkingNode){
        double startTime = Constants.START_TIME;
        double time = startTime+0;
        while (time < Constants.TOTAL_TIME_DURING_DAY){
            time += drawFromExponentialDistribution(1.0/getDemandRateForNodeAtTime(parkingNode,time));
            DemandRequest newDemandRequest = new DemandRequest(parkingNode,time);
            addDemandRequestToMap(newDemandRequest);
        }
    }

    private void addDemandRequestToMap(DemandRequest request){
        if (demandRequests.get(request.getNode()) == null){
            demandRequests.put(request.getNode(), new ArrayList<>());
        } else {
            demandRequests.get(request.getNode()).add(request);
        }
    }




}

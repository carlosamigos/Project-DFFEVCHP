package code.simulation;


import constants.Constants;
import constants.FileConstants;
import constants.SimulationConstants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import code.problem.ProblemInstance;
import code.problem.nodes.ParkingNode;

public class SimulationModel {

    private int dayNumber;
    private ProblemInstance problemInstance;
    private HashMap<ParkingNode,ArrayList<DemandRequest>> demandRequests;

    private String dayNumberString = "DAY_NUMBER";
    private String startIndexString  = "START_INDEX";
    private String problemInstanceFileName = "PROBLEM_INSTANCE_NUMBER";

    public SimulationModel(int dayNumber, ProblemInstance problemInstance) {
        this.dayNumber = dayNumber;
        this.problemInstance = problemInstance;
        this.demandRequests = new HashMap<>();
    }

    public void createNewDaySimulationModel(){
        //simulate pickups
        for (ParkingNode pNode : problemInstance.getParkingNodes()) {
            createAllDemandRequestsForNode(pNode);
        }
    }

    public void saveDaySimulationModel(){
        try{
            NumberFormat formatter = new DecimalFormat("#0.000");
            PrintWriter writer = new PrintWriter(FileConstants.SIMULATIONS_FOLDER + FileConstants.DEMAND_REQUESTS + "_day_"+Integer.toString(dayNumber)+"_problemInstance_"+problemInstance.getFileName() + ".txt");
            writer.println(dayNumberString+" : "+Integer.toString(dayNumber));
            writer.println(startIndexString+" : "+Integer.toString(Constants.START_INDEX));
            writer.println(problemInstanceFileName +" : "+problemInstance.getFileName());
            for (ParkingNode pNode : demandRequests.keySet()) {
                for (DemandRequest req : demandRequests.get(pNode)) {
                    writer.println(Integer.toString(req.getNode().getNodeId())+":"+formatter.format(req.getTime()));
                }
            }
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Simulation day could not be saved.");
        }
    }

    public void readSimulationModelFromFile(){
        demandRequests = new HashMap<>();
        try{
            String readString = FileConstants.SIMULATIONS_FOLDER + FileConstants.DEMAND_REQUESTS + "_day_"+Integer.toString(dayNumber)+"_problemInstance_"+problemInstance.getFileName() + ".txt";
            FileReader fileReader = new FileReader(readString);
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                if((line.contains(dayNumberString) || line.contains(problemInstanceFileName) || line.contains(startIndexString))){
                    line.trim(); line = line.replace("\n","");
                    String[] parts = line.split(":");
                    String type = parts[0].trim();
                    if(type.contains(dayNumberString)){
                        int number = Integer.parseInt(parts[1].trim());
                        this.dayNumber = number;
                    } else if(type.contains(startIndexString)){
                        int number = Integer.parseInt(parts[1].trim());
                        if(number != Constants.START_INDEX){
                            throw new IllegalArgumentException();
                        }
                    }
                }
                else {
                    line.trim(); line = line.replace("\n","");
                    String[] parts = line.split(":");
                    int parkingNodeId = Integer.parseInt(parts[0].trim());
                    parts[1] = parts[1].replace(",",".");
                    double number = Double.parseDouble(parts[1].trim());
                    try{
                        ParkingNode pNode = (ParkingNode) problemInstance.getNodeMap().get(parkingNodeId);
                        DemandRequest demandRequest = new DemandRequest(pNode, number);
                        addDemandRequestToMap(demandRequest);
                    } catch (Exception e){
                        System.out.println("ERROR: Parking node index wrong in simulation file");
                        return;
                    }
                }

                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

        } catch (IOException e){
            System.out.println("Simulation file not found: " + e.getMessage());
            e.printStackTrace();
            return;
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            return;
        } finally {
        
        }
    }

    private double getDemandRateForNodeAtTime(ParkingNode parkingNode, double time){
        if(parkingNode.getDemandGroup()== SimulationConstants.nodeDemandGroup.MIDDAY_RUSH){
            return (SimulationConstants.HIGH_RATE_LAMBDA - SimulationConstants.LOW_RATE_LAMBDA) * 
            		Math.sin((time-Constants.START_TIME) * 
            				Math.asin(1)/Constants.END_TIME)+SimulationConstants.LOW_RATE_LAMBDA;
        } else if (parkingNode.getDemandGroup()== SimulationConstants.nodeDemandGroup.MORNING_RUSH){
            return (SimulationConstants.HIGH_RATE_LAMBDA - SimulationConstants.LOW_RATE_LAMBDA) * 
            		Math.cos((time-Constants.START_TIME) * 
            				Math.asin(1)/Constants.END_TIME)+SimulationConstants.LOW_RATE_LAMBDA;
        } else {
            return SimulationConstants.MEDIUM_RATE_LAMBDA;
        }
    }

    private double drawFromExponentialDistribution(double rate){
        return -Math.log(1.0 - Math.random())/rate;
    }

    private void createAllDemandRequestsForNode(ParkingNode parkingNode){
        double startTime = Constants.START_TIME;
        double time = startTime+0;
        while (time < Constants.END_TIME){
            time += drawFromExponentialDistribution(getDemandRateForNodeAtTime(parkingNode,time));
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

    public HashMap<ParkingNode, ArrayList<DemandRequest>> getDemandRequests() {
        return demandRequests;
    }

    public double findExpectedNumberOfArrivalsMorningRushBetween(double start, double end){
        if(end > Constants.END_TIME){
           return  (lambdaMorningRushIntegral(Constants.START_TIME)- lambdaMorningRushIntegral(Constants.START_TIME + Constants.TIME_INCREMENTS));
        }
        double result = (lambdaMorningRushIntegral(end)- lambdaMorningRushIntegral(start));
        return result;
    }

    public double findExpectedNumberOfArrivalsMiddayRushBetween(double start, double end){
        if(end > Constants.END_TIME){
            return  (lambdaMiddayRushIntegral(Constants.START_TIME)- lambdaMiddayRushIntegral(Constants.START_TIME + Constants.TIME_INCREMENTS));
        }
        double result = (lambdaMiddayRushIntegral(end)- lambdaMiddayRushIntegral(start));
        return result;
    }

    public double findExpectedNumberOfArrivalsNormalBetween(double start, double end){
        return (end-start)*SimulationConstants.MEDIUM_RATE_LAMBDA;
    }

    private double lambdaMorningRushIntegral(double time){
        double low = SimulationConstants.LOW_RATE_LAMBDA;
        double high = SimulationConstants.HIGH_RATE_LAMBDA;
        double gradient = -(high-low)/(Constants.END_TIME-Constants.START_TIME);
        double intersection = high - gradient*Constants.START_TIME;
        return 0.5*gradient*time*time + intersection*time;
    }

    private double lambdaMiddayRushIntegral(double time){
        double low = SimulationConstants.LOW_RATE_LAMBDA;
        double high = SimulationConstants.HIGH_RATE_LAMBDA;
        double gradient = (high-low)/(Constants.END_TIME-Constants.START_TIME);
        double intersection = low - gradient*Constants.START_TIME;
        return 0.5*gradient*time*time + intersection*time;
    }


    @Override
    public String toString() {
        return "SimulationModel{" +
                "dayNumber=" + dayNumber +
                ", demandRequests=" + demandRequests +
                '}';
    }
}

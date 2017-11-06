package tio4500;

import constants.Constants;
import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.ChargingNode;
import tio4500.simulations.Nodes.Node;
import tio4500.simulations.Nodes.ParkingNode;
import tio4500.simulations.Travels.CustomerTravel;
import tio4500.simulations.Travels.OperatorTravel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ProblemInstance {

    private int exampleNumber;
    private ArrayList<ParkingNode> parkingNodes;
    private ArrayList<ChargingNode> chargingNodes;
    private ArrayList<Car> cars;
    private ArrayList<Operator> operators;
    private ArrayList<OperatorTravel> operatorTravels;
    private ArrayList<CustomerTravel> customerTravels;
    private ArrayList<ArrayList<Double>> travelTimesBike;
    private ArrayList<ArrayList<Double>> travelTimesCar;

    private HashMap<Integer,Node> nodeMap;
    private HashMap<Integer,Operator> operatorMap;
    private HashSet<String> stateSpecificKeys = new HashSet<>();
    private HashMap<ChargingNode,ParkingNode> chargingToParkingNode;

    private int numPNodes = 0;
    private int numCNodes = 0;
    private int numROperators = 0;

    private HashMap<String, String> inputFileMap = new HashMap<>();
    private HashMap<String, String> inputFileMapRaw = new HashMap<>();

    public ProblemInstance(int exampleNumber) {
        this.exampleNumber = exampleNumber;
        this.parkingNodes = new ArrayList<>();
        this.chargingNodes = new ArrayList<>();
        this.cars = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.operatorTravels = new ArrayList<>();
        this.customerTravels = new ArrayList<>();
        this.travelTimesBike = new ArrayList<>();
        this.travelTimesCar = new ArrayList<>();
        nodeMap = new HashMap<>();
        operatorMap = new HashMap<>();
        chargingToParkingNode = new HashMap<>();
        try {
            readProblemFromFile();
        } catch (IOException e){
            System.out.println("File could not be read for example "+exampleNumber);
        }
        handleInputFileMap();
        addStateSpecificStrings();
        makeChargingToparkingNodeMap();

    }

    private void addStateSpecificStrings(){
        stateSpecificKeys.add("chargingSlotsAvailable");
        stateSpecificKeys.add("travelTimeToParkingA");
        stateSpecificKeys.add("travelTimeToOriginR");
        stateSpecificKeys.add("initialInNeedP");
        stateSpecificKeys.add("idealStateP");
        stateSpecificKeys.add("parkingNodeAOperator");
        stateSpecificKeys.add("initialRegularInP");
        stateSpecificKeys.add("startNodeROperator");
        stateSpecificKeys.add("chargingNodeAOperator");
        stateSpecificKeys.add("parkingNodeAOperator");
        stateSpecificKeys.add("numAOperators");
    }

    private void readProblemFromFile() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(Constants.INITIAL_STATE_FOLDER_FILE +Integer.toString(exampleNumber) + ".txt"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            boolean inMatrix = false;
            String matrixKey = "";
            String matrixString = "";
            while (line != null) {
                line.trim();
                if(line.length() == 0){
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                    continue;
                }
                sb.append(line);
                if(line.contains("[") && line.contains("]") && !inMatrix){
                    String[] parts = line.split(":");
                    inputFileMap.put(parts[0].trim(),parts[1].trim());
                }else if(!line.contains("[") && !inMatrix){
                    String[] parts = line.split(":");
                    inputFileMap.put(parts[0].trim(),parts[1].trim());
                }
                else {
                    //matrix
                    inMatrix = true;
                    if(line.contains("[")){
                        //first line
                        String[] parts = line.split(":");
                        matrixKey = parts[0].trim();
                        matrixString = matrixString + parts[1].trim() +",";
                    } else {
                        matrixString = matrixString + line.trim() +",";
                    }
                    if(line.contains("]")){
                        inMatrix = false;
                        matrixString = matrixString.substring(0,matrixString.length()-1);
                        inputFileMap.put(matrixKey,matrixString);
                        matrixKey = "";
                        matrixString = "";
                    }
                }
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        } finally {
            br.close();
        }
    }

    private void handleInputFileMap(){
        numPNodes = Integer.parseInt(inputFileMap.get("numPNodes"));
        numCNodes = Integer.parseInt(inputFileMap.get("numCNodes"));
        numROperators = Integer.parseInt(inputFileMap.get("numROperators"));
        setUpNodesAndCars();
        setUpOperators();
        addInitialDemandRatesToNodes();
    }

    private void setUpOperators(){
        String startNodesString = inputFileMap.get("startNodeROperator");
        String[] startNodeList = startNodesString.substring(1,startNodesString.length()-1).split(" ");
        for (int operatorId = Constants.START_INDEX; operatorId < startNodeList.length+Constants.START_INDEX; operatorId++) {
            int nodeId = Integer.parseInt(startNodeList[operatorId-Constants.START_INDEX]);
            Node node = nodeMap.get(nodeId);
            Operator newOperator = new Operator(operatorId);
            newOperator.setNextOrCurrentNode(node);
            operators.add(newOperator);
            operatorMap.put(operatorId,newOperator);
        }
    }

    private void setUpNodesAndCars() {
        String[] totalChargingSlots = inputFileMap.get("totalNumberOfChargingSlots").substring(1, inputFileMap.get("totalNumberOfChargingSlots").length() - 1).split(" ");
        ArrayList<Integer> totalChargingSlotsArray = new ArrayList<>();
        int i;
        for (i = 0; i < totalChargingSlots.length; i++) {
            totalChargingSlotsArray.add(Integer.parseInt(totalChargingSlots[i]));
        }

        String[] availableChargingSlots = inputFileMap.get("chargingSlotsAvailable").substring(1, inputFileMap.get("chargingSlotsAvailable").length() - 1).split(" ");
        ArrayList<Integer> availableChargingSlotsArray = new ArrayList<>();
        for (i = 0; i < availableChargingSlots.length; i++) {
            availableChargingSlotsArray.add(Integer.parseInt(availableChargingSlots[i]));
        }
        String[] initialRegularInP = inputFileMap.get("initialRegularInP").substring(1, inputFileMap.get("initialRegularInP").length() - 1).split(" ");
        ArrayList<Integer> initialRegularInPArray = new ArrayList<>();
        for (i = 0; i < initialRegularInP.length; i++) {
            initialRegularInPArray.add(Integer.parseInt(initialRegularInP[i]));
        }
        String[] initialInNeedP = inputFileMap.get("initialInNeedP").substring(1, inputFileMap.get("initialInNeedP").length() - 1).split(" ");
        ArrayList<Integer> initialInNeedPArray = new ArrayList<>();
        for (i = 0; i < initialInNeedP.length; i++) {
            initialInNeedPArray.add(Integer.parseInt(initialInNeedP[i]));
        }
        String[] initialHandling = inputFileMap.get("initialHandling").substring(1, inputFileMap.get("initialHandling").length() - 1).split(" ");
        ArrayList<Integer> initialHandlingArray = new ArrayList<>();
        for (i = 0; i < initialHandling.length; i++) {
            initialHandlingArray.add(Integer.parseInt(initialHandling[i]));
        }
        String[] remainingChargingTime = inputFileMap.get("travelTimeToParkingA").substring(1, inputFileMap.get("travelTimeToParkingA").length() - 1).split(" ");
        ArrayList<Double> remainingChargingTimeArray = new ArrayList<>();
        for (i = 0; i < remainingChargingTime.length; i++) {
            remainingChargingTimeArray.add(Double.parseDouble(remainingChargingTime[i]));
        }

        // Create nodes
        int carId = Constants.START_INDEX;
        for (i = Constants.START_INDEX; i < numPNodes+Constants.START_INDEX; i++) {
            ParkingNode newParkingNode = new ParkingNode(i);
            parkingNodes.add(newParkingNode);
            addNodeToNodeMap(newParkingNode);
            for (int j = 0; j < initialRegularInPArray.get(i-Constants.START_INDEX); j++) {
                Car newRegularCar = new Car(carId,1.0);
                newRegularCar.setCurrentNextNode(newParkingNode);
                newRegularCar.setPreviousNode(newParkingNode);
                carId++;
                newParkingNode.addRegularCar(newRegularCar);
                cars.add(newRegularCar);
            }
            for (int j = 0; j < initialInNeedPArray.get(i-Constants.START_INDEX); j++) {
                Car newCarInNeed = new Car(carId,Constants.CHARGING_THRESHOLD*0.999);
                newCarInNeed.setCurrentNextNode(newParkingNode);
                newCarInNeed.setPreviousNode(newParkingNode);
                carId++;
                newParkingNode.addCarInNeed(newCarInNeed);
                cars.add(newCarInNeed);
            }
        }
        for (i = numPNodes+Constants.START_INDEX; i < numCNodes + numPNodes+Constants.START_INDEX; i++) {
            ChargingNode newChargingNodes = new ChargingNode(i);
            newChargingNodes.setNumberOfCarsCharging(0);
            newChargingNodes.setNumberOfTotalChargingSpots(totalChargingSlotsArray.get(i - numPNodes-Constants.START_INDEX));
            chargingNodes.add(newChargingNodes);
            addNodeToNodeMap(newChargingNodes);
        }

        // Get travel times vehicle
        String timeString = inputFileMap.get("travelTimeVehicle");
        String[] timeRows = timeString.substring(1, timeString.length()-1).split(",");
        for (String timeRow : timeRows) {
            ArrayList<Double> newTimeRow = new ArrayList<>();
            for (String timeDouble : timeRow.split(" ")) {
                newTimeRow.add(Double.parseDouble(timeDouble));
            }
            travelTimesCar.add(newTimeRow);
        }

        // Get travel times bike
        timeString = inputFileMap.get("travelTimeBike");
        timeRows = timeString.substring(1, timeString.length()-1).split(",");
        for (String timeRow : timeRows) {
            ArrayList<Double> newTimeRow = new ArrayList<>();
            for (String timeDouble : timeRow.split(" ")) {
                newTimeRow.add(Double.parseDouble(timeDouble));
            }
            travelTimesBike.add(newTimeRow);
        }






    }

    private void addNodeToNodeMap(Node node){
        this.nodeMap.put(node.getNodeId(),node);
    }

    private void addInitialDemandRatesToNodes(){
        int nodesWithVaryingDemand;
        int nodesWithHighDemand;
        try{
            nodesWithVaryingDemand =  (int) Math.round(numPNodes*Constants.PERCENTAGE_AFFECTED_BY_RUSH_HOUR);
            nodesWithHighDemand = (int) Math.round(nodesWithVaryingDemand*Constants.PERCENTAGE_RUSH_HOUR_SPLIT);
        } catch (Exception e){
            System.out.println("Casting Error");
            nodesWithVaryingDemand = 0;
            nodesWithHighDemand = 0;
        }

        for (int i = Constants.START_INDEX; i < numPNodes + Constants.START_INDEX; i++) {
            ParkingNode parkingNode = (ParkingNode) nodeMap.get(i);
            if(i >= nodesWithVaryingDemand + Constants.START_INDEX){
                parkingNode.setDemandGroup(Constants.nodeDemandGroup.NEUTRAL);
            }else if( i < nodesWithHighDemand+Constants.START_INDEX){
                parkingNode.setDemandGroup(Constants.nodeDemandGroup.MORNING_RUSH);
            } else {
                parkingNode.setDemandGroup(Constants.nodeDemandGroup.MIDDAY_RUSH);
            }
        }
    }

    public int getExampleNumber() {
        return exampleNumber;
    }

    public ArrayList<ParkingNode> getParkingNodes() {
        return parkingNodes;
    }

    public ArrayList<ChargingNode> getChargingNodes() {
        return chargingNodes;
    }

    public ArrayList<Operator> getOperators() {
        return operators;
    }

    public ArrayList<OperatorTravel> getOperatorTravels() {
        return operatorTravels;
    }

    public ArrayList<CustomerTravel> getCustomerTravels() {
        return customerTravels;
    }

    public ArrayList<ArrayList<Double>> getTravelTimesBike() {
        return travelTimesBike;
    }

    public void setTravelTimesBike(ArrayList<ArrayList<Double>> travelTimesBike) {
        this.travelTimesBike = travelTimesBike;
    }

    public ArrayList<ArrayList<Double>> getTravelTimesCar() {
        return travelTimesCar;
    }

    public void setTravelTimesCar(ArrayList<ArrayList<Double>> travelTimesCar) {
        this.travelTimesCar = travelTimesCar;
    }

    public HashMap<Integer, Node> getNodeMap() {
        return nodeMap;
    }

    public HashMap<Integer, Operator> getOperatorMap() {
        return operatorMap;
    }

    private void makeChargingToparkingNodeMap(){
        String[] cToParray = inputFileMap.get("cToP").substring(1,inputFileMap.get("cToP").length()-1).split(" ");
        for (int i = 0; i < cToParray.length; i++) {
            int parkingId = Integer.parseInt(cToParray[i]);
            int chargingId = i + Constants.START_INDEX + parkingNodes.size();
            if(nodeMap.get(chargingId).getClass() != ChargingNode.class || nodeMap.get(parkingId).getClass() != ParkingNode.class){
                System.out.println("index error when accessing charging node in node map in makeChargingToparkingNodeMap in ProblemInstance");
                System.exit(1);
            } else {
                ChargingNode cNode = (ChargingNode) nodeMap.get(chargingId);
                ParkingNode pNode = (ParkingNode) nodeMap.get(parkingId);
                chargingToParkingNode.put(cNode,pNode);
            }
        }
    }


    public void writeProblemInstanceToFile(){
        System.out.println("Writing state to file...");
        // ASSUMING ALL STATES ARE CONSISTENT. WRITING AS IS.
        try{
            PrintWriter writer = new PrintWriter(Constants.STATE_FOLDER + "exampleState"+Integer.toString(exampleNumber)+".txt", "UTF-8");
            for (String key :inputFileMap.keySet()) {
                if(!stateSpecificKeys.contains(key)){
                    writer.println(key + " : " + inputFileMap.get(key));
                }
            }
            writer.println();

            // chargingSlotsAvailable
            String chargingSlotsAvailableArray = "[";
            for (ChargingNode cNode : chargingNodes) {
                chargingSlotsAvailableArray += cNode.findNumberOfChargingSpotsAvailableDuringNextPeriod() + " ";
            }
            writer.println("chargingSlotsAvailable : "+ chargingSlotsAvailableArray.substring(0,chargingSlotsAvailableArray.length()-1)+"]");

            // Artificial operators
            int numArtificialOperators = 0;
            String travelTimeToParkingA = "[";
            String chargingNodeAOperator = "[";
            String parkingNodeAOperator = "[";

            for (ChargingNode cNode : chargingNodes) {
                for(Car car : cNode.getCarsCurrentlyCharging()){
                    if(car.getRemainingChargingTime() < Constants.TIME_INCREMENTS){
                        assert(car.getPreviousNode().getClass() == ChargingNode.class);
                        numArtificialOperators ++;
                        travelTimeToParkingA += car.getRemainingChargingTime()+ " ";
                        chargingNodeAOperator += car.getPreviousNode().getNodeId() + " ";
                        parkingNodeAOperator += chargingToParkingNode.get(car.getPreviousNode()).getNodeId() + " ";
                    }
                }
            }
            if(travelTimeToParkingA.length() > 1){
                writer.println("numAOperators : " + Integer.toString(numArtificialOperators));
                writer.println("travelTimeToParkingA : " + travelTimeToParkingA.substring(0,travelTimeToParkingA.length()-1)+"]");
                writer.println("chargingNodeAOperator : " + chargingNodeAOperator.substring(0,chargingNodeAOperator.length()-1)+"]");
                writer.println("parkingNodeAOperator : " + parkingNodeAOperator.substring(0,parkingNodeAOperator.length()-1)+"]");
            } else {
                writer.println("numAOperators : " + Integer.toString(numArtificialOperators));
                writer.println("travelTimeToParkingA : " + travelTimeToParkingA+"]");
                writer.println("chargingNodeAOperator : " + chargingNodeAOperator+"]");
                writer.println("parkingNodeAOperator : " + parkingNodeAOperator+"]");
            }

            // Real operators
            String travelTimeToOriginR = "[";
            String startNodeROperator = "[";
            for (Operator operator : operators) {
                travelTimeToOriginR += operator.getTimeRemainingToCurrentNextNode() + " ";
                startNodeROperator += operator.getPreviousNode().getNodeId()+ " ";
            }
            writer.println("travelTimeToOriginR : "+travelTimeToOriginR.substring(0,travelTimeToOriginR.length()-1)+ "]");
            writer.println("startNodeROperator : "+startNodeROperator.substring(0,startNodeROperator.length()-1)+ "]");

            // initialInNeedP
            String initialInNeedP = "[";
            String idealStateP = "[";
            String initialRegularInP = "[";
            for (ParkingNode pNode : parkingNodes) {
                initialInNeedP += pNode.getCarsInNeed().size() + " ";
                idealStateP += pNode.getIdealNumberOfAvailableCars() + " ";
                initialRegularInP += pNode.getCarsRegular().size() + " ";
            }
            writer.println("initialInNeedP : "+ initialInNeedP.substring(0,initialInNeedP.length()-1)+ "]");
            writer.println("idealStateP : "+ idealStateP.substring(0,idealStateP.length()-1)+ "]");
            writer.println("initialRegularInP : "+ initialRegularInP.substring(0,initialRegularInP.length()-1)+ "]");





            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Written to file.");

    }

    @Override
    public String toString() {
        return "\nProblemInstance" + exampleNumber +":"+
                "\n\t  parkingNodes=" + parkingNodes +
                "\n\t  chargingNodes=" + chargingNodes +
                "\n\t  cars=" + cars +
                "\n\t  operators=" + operators +
                "\n\t  operatorTravels=" + operatorTravels +
                "\n\t  customerTravels=" + customerTravels +
                "\n\t  travelTimesBike=" + travelTimesBike +
                "\n\t  travelTimesCar=" + travelTimesCar + "\n";
    }
}

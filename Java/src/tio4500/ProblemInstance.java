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
import java.util.*;

public class ProblemInstance {

    private int exampleNumber;
    private ArrayList<ParkingNode> parkingNodes;
    private ArrayList<ChargingNode> chargingNodes;
    private ArrayList<Car> cars;
    private ArrayList<Operator> operators;
    private ArrayList<ArrayList<Double>> travelTimesBike;
    private ArrayList<ArrayList<Double>> travelTimesCar;

    private HashMap<Integer,Node> nodeMap;
    private HashMap<Integer,Operator> operatorMap;
    private HashSet<String> stateSpecificKeys = new HashSet<>();
    private HashMap<ChargingNode,ParkingNode> chargingToParkingNode;
    private HashMap<ParkingNode,ChargingNode> parkingNodeToChargingNode;

    private int numPNodes = 0;
    private int numCNodes = 0;
    private int numROperators = 0;
    private double handlingTimeP = 0;
    private double handlingTimeC = 0;


    private HashMap<String, String> inputFileMap = new HashMap<>();
    private HashMap<String, String> inputFileMapRaw = new HashMap<>();

    private String chargingSlotsAvailableString = "";
    private String travelTimeToOriginRString = "";
    private String tavelTimeToParkingAString = "";
    private String initialRegularInPString = "";
    private String initialInNeedPString = "";
    private String idealStatePString = "";
    private String demandPString  = "";
    private String initialHandlingString = "";
    private int numberOfCarsTakenByCustomers = 0;

    public ProblemInstance(int exampleNumber) {
        this.exampleNumber = exampleNumber;
        this.parkingNodes = new ArrayList<>();
        this.chargingNodes = new ArrayList<>();
        this.cars = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.travelTimesBike = new ArrayList<>();
        this.travelTimesCar = new ArrayList<>();
        nodeMap = new HashMap<>();
        operatorMap = new HashMap<>();
        chargingToParkingNode = new HashMap<>();
        parkingNodeToChargingNode = new HashMap<>();
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
        stateSpecificKeys.add("finishedDuringC");
        stateSpecificKeys.add("demandP");
        stateSpecificKeys.add("timeLimitLastVisit");
        stateSpecificKeys.add("timeLimit");
        stateSpecificKeys.add("initialHandling");
        stateSpecificKeys.add("mode");
        stateSpecificKeys.add("numVisits");
    }

    private void readProblemFromFile() throws IOException{
        //System.out.println("reading file: "+ Constants.INITIAL_STATE_FOLDER_FILE +Integer.toString(exampleNumber) + ".txt");
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
        handlingTimeC = Integer.parseInt(inputFileMap.get("handlingTimeC"));
        handlingTimeP = Integer.parseInt(inputFileMap.get("handlingTimeP"));
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
        int i;
        ArrayList<Integer> totalChargingSlotsArray = new ArrayList<>();
        if(inputFileMap.get("totalNumberOfChargingSlots").length() >2){
            String[] totalChargingSlots = inputFileMap.get("totalNumberOfChargingSlots").replace("[","").replace("]","").split(" ");
            for (i = 0; i < totalChargingSlots.length; i++) {
                totalChargingSlotsArray.add(Integer.parseInt(totalChargingSlots[i]));
            }
        }
        ArrayList<Integer> availableChargingSlotsArray = new ArrayList<>();
        if(inputFileMap.get("chargingSlotsAvailable").length() >2){
            String[] availableChargingSlots = inputFileMap.get("chargingSlotsAvailable").replace("[","").replace("]","").split(" ");
            for (i = 0; i < availableChargingSlots.length; i++) {
                availableChargingSlotsArray.add(Integer.parseInt(availableChargingSlots[i]));
            }
        }
        ArrayList<Integer> initialRegularInPArray = new ArrayList<>();
        if(inputFileMap.get("initialRegularInP").length() >2){
            String[] initialRegularInP = inputFileMap.get("initialRegularInP").replace("[","").replace("]","").split(" ");
            for (i = 0; i < initialRegularInP.length; i++) {
                initialRegularInPArray.add(Integer.parseInt(initialRegularInP[i]));
            }
        }

        ArrayList<Integer> initialInNeedPArray = new ArrayList<>();
        if(inputFileMap.get("initialInNeedP").length() >2){
            String[] initialInNeedP = inputFileMap.get("initialInNeedP").replace("[","").replace("]","").split(" ");
            for (i = 0; i < initialInNeedP.length; i++) {
                initialInNeedPArray.add(Integer.parseInt(initialInNeedP[i]));
            }
        }

        ArrayList<Integer> initialHandlingArray = new ArrayList<>();
        if(inputFileMap.get("initialHandling").length() >2){
            String[] initialHandling = inputFileMap.get("initialHandling").replace("[","").replace("]","").split(" ");
            for (i = 0; i < initialHandling.length; i++) {
                initialHandlingArray.add(Integer.parseInt(initialHandling[i]));
            }
        }

        ArrayList<Double> remainingChargingTimeArray = new ArrayList<>();
        if(inputFileMap.get("travelTimeToParkingA").length() >2){
            String[] remainingChargingTime = inputFileMap.get("travelTimeToParkingA").replace("[","").replace("]","").split(" ");
            for (i = 0; i < remainingChargingTime.length; i++) {
                remainingChargingTimeArray.add(Double.parseDouble(remainingChargingTime[i]));

            }
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
                Car newCarInNeed = new Car(carId,Constants.SOFT_CHARGING_THRESHOLD*0.999);
                newCarInNeed.setCurrentNextNode(newParkingNode);
                newCarInNeed.setPreviousNode(newParkingNode);
                carId++;
                newParkingNode.addCarInNeed(newCarInNeed);
                cars.add(newCarInNeed);
            }
        }
        for (i = numPNodes+Constants.START_INDEX; i < numCNodes + numPNodes+Constants.START_INDEX; i++) {
            ChargingNode newChargingNodes = new ChargingNode(i);
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
            if( !(nodeMap.get(chargingId) instanceof ChargingNode) || !(nodeMap.get(parkingId) instanceof ParkingNode)){
                System.out.println("index error when accessing charging node in node map in makeChargingToparkingNodeMap in ProblemInstance");
                System.exit(1);
            } else {
                ChargingNode cNode = (ChargingNode) nodeMap.get(chargingId);
                ParkingNode pNode = (ParkingNode) nodeMap.get(parkingId);
                chargingToParkingNode.put(cNode,pNode);
                parkingNodeToChargingNode.put(pNode,cNode);
            }
        }
    }


    public void writeProblemInstanceToFile(){
        //System.out.println("Writing state to file...");
        // ASSUMING ALL STATES ARE CONSISTENT. WRITING AS IS.
        try{
            PrintWriter writer = new PrintWriter(Constants.STATE_FOLDER + "exampleState"+Integer.toString(exampleNumber)+".txt", "UTF-8");
            for (String key :inputFileMap.keySet()) {
                if(!stateSpecificKeys.contains(key)){
                    if(key.equals("travelTimeVehicle")){
                        writer.println(key  + " : " + inputFileMap.get(key).replace(","," "));
                    }else if (key.equals("travelTimeBike")){
                        writer.println(key + " : " + inputFileMap.get(key).replace(","," "));
                    }else{
                        writer.println(key + " : " + inputFileMap.get(key));
                    }
                }
            }
            writer.println();
            //Time limits
            writer.println("timeLimitLastVisit : "+ Constants.TIME_LIMIT_LAST_VISIT);
            writer.println("timeLimit : "+ Constants.TIME_LIMIT_STATIC_PROBLEM);

            // chargingSlotsAvailable and finished during period
            String chargingSlotsAvailableArray = "[";
            String finishedDuringCArray = "[";
            HashMap<ChargingNode,Integer> chargingSlotsAvailableMap = new HashMap<>();
            HashMap<ChargingNode,Integer> carsFinishedChargingMap = new HashMap<>();
            for (ChargingNode cNode : chargingNodes) {
                int carsFinishedChargingDuringPeriod = cNode.findNumberOfCarsFinishingChargingDuringNextPeriod();
                int chargingSpotsAvailableNow = cNode.getNumberOfTotalChargingSlots() - cNode.getCarsCurrentlyCharging().size();
                chargingSlotsAvailableArray += (carsFinishedChargingDuringPeriod + chargingSpotsAvailableNow) +" ";
                finishedDuringCArray += carsFinishedChargingDuringPeriod + " ";
                chargingSlotsAvailableMap.put(cNode,chargingSpotsAvailableNow);
                carsFinishedChargingMap.put(cNode,carsFinishedChargingDuringPeriod);
            }
            writer.println("chargingSlotsAvailable : "+ chargingSlotsAvailableArray.substring(0,chargingSlotsAvailableArray.length()-1)+"]");
            writer.println("finishedDuringC : "+ finishedDuringCArray.substring(0,finishedDuringCArray.length()-1)+"]");
            chargingSlotsAvailableString = chargingSlotsAvailableArray.substring(0,chargingSlotsAvailableArray.length()-1)+"]";

            // Artificial operators
            int numArtificialOperators = 0;
            String travelTimeToParkingA = "[";
            String chargingNodeAOperator = "[";
            String parkingNodeAOperator = "[";
            HashMap<ParkingNode,Integer> numberOfCarsArtificiallyArrivingToParkingNode = new HashMap<>();
            for (ChargingNode cNode : chargingNodes) {
                for(Car car : cNode.getCarsCurrentlyCharging()){
                    if(car.getRemainingChargingTime() < Constants.TIME_LIMIT_STATIC_PROBLEM){
                        numArtificialOperators ++;
                        travelTimeToParkingA += car.getRemainingChargingTime()+ " ";
                        chargingNodeAOperator += cNode.getNodeId() + " ";
                        parkingNodeAOperator += chargingToParkingNode.get(cNode).getNodeId() + " ";
                        if(numberOfCarsArtificiallyArrivingToParkingNode.get(chargingToParkingNode.get(cNode)) == null){
                            numberOfCarsArtificiallyArrivingToParkingNode.put(chargingToParkingNode.get(cNode), 0);
                        }
                        numberOfCarsArtificiallyArrivingToParkingNode.put(chargingToParkingNode.get(cNode), numberOfCarsArtificiallyArrivingToParkingNode.get(chargingToParkingNode.get(cNode)) +1);
                    }
                }
            }
            if(numArtificialOperators > 0){
                writer.println("numAOperators : " + Integer.toString(numArtificialOperators));
                writer.println("travelTimeToParkingA : " + travelTimeToParkingA.substring(0,travelTimeToParkingA.length()-1)+"]");
                tavelTimeToParkingAString = travelTimeToParkingA.substring(0,travelTimeToParkingA.length()-1)+"]";
                writer.println("chargingNodeAOperator : " + chargingNodeAOperator.substring(0,chargingNodeAOperator.length()-1)+"]");
                writer.println("parkingNodeAOperator : " + parkingNodeAOperator.substring(0,parkingNodeAOperator.length()-1)+"]");
            } else {
                writer.println("numAOperators : " + Integer.toString(numArtificialOperators));
                writer.println("travelTimeToParkingA : " + travelTimeToParkingA+"]");
                tavelTimeToParkingAString = travelTimeToParkingA+"]";
                writer.println("chargingNodeAOperator : " + chargingNodeAOperator+"]");
                writer.println("parkingNodeAOperator : " + parkingNodeAOperator+"]");
            }



            // Real operators
            String travelTimeToOriginR = "[";
            String startNodeROperator = "[";
            HashMap<Node,Integer> numberOfOperatorsStartingInNode = new HashMap<>();
            for (Operator operator : operators) {
                travelTimeToOriginR += operator.getTimeRemainingToCurrentNextNode() + " ";
                startNodeROperator += operator.getNextOrCurrentNode().getNodeId()+ " ";
                Node startNode = operator.getNextOrCurrentNode();
                if(numberOfOperatorsStartingInNode.get(startNode) == null){
                    numberOfOperatorsStartingInNode.put(startNode,0);
                }
                numberOfOperatorsStartingInNode.put(startNode, numberOfOperatorsStartingInNode.get(startNode) + 1);
            }
            writer.println("travelTimeToOriginR : "+travelTimeToOriginR.substring(0,travelTimeToOriginR.length()-1)+ "]");
            travelTimeToOriginRString = travelTimeToOriginR.substring(0,travelTimeToOriginR.length()-1)+ "]";
            writer.println("startNodeROperator : "+startNodeROperator.substring(0,startNodeROperator.length()-1)+ "]");

            // initialInNeedP, idealStateP, initialRegularInP and demandP
            String initialInNeedP = "[";
            String idealStateP = "[";
            String initialRegularInP = "[";
            String demandP = "[";
            int totalNumberOfCarsInNeed = 0;
            for (ParkingNode pNode : parkingNodes) {
                initialInNeedP += pNode.getCarsInNeed().size() + " ";
                totalNumberOfCarsInNeed += pNode.getCarsInNeed().size();
                idealStateP += pNode.getIdealNumberOfAvailableCars() + " ";
                initialRegularInP += pNode.getCarsRegular().size() + " ";
                demandP += pNode.getPredictedNumberOfCarsDemandedThisPeriod() + " ";
            }
            writer.println("initialInNeedP : "+ initialInNeedP.substring(0,initialInNeedP.length()-1)+ "]");
            initialInNeedPString = initialInNeedP.substring(0,initialInNeedP.length()-1)+ "]";
            writer.println("idealStateP : "+ idealStateP.substring(0,idealStateP.length()-1)+ "]");
            idealStatePString = idealStateP.substring(0,idealStateP.length()-1)+ "]";
            writer.println("initialRegularInP : "+ initialRegularInP.substring(0,initialRegularInP.length()-1)+ "]");
            initialRegularInPString = initialRegularInP.substring(0,initialRegularInP.length()-1)+ "]";
            writer.println("demandP : "+ demandP.substring(0,demandP.length()-1)+ "]");
            demandPString = demandP.substring(0,demandP.length()-1)+ "]";


            //initial handling
            String initialHandling = "[";
            HashMap<Node,Integer> numberOfInitialHandlersToInNode = new HashMap<>();
            for (Operator operator : operators) {
                initialHandling += (operator.isHandling() ? 1:0) + " ";
                if(operator.isHandling()){
                    Node node = operator.getNextOrCurrentNode();
                    if(numberOfInitialHandlersToInNode.get(node) == null){
                        numberOfInitialHandlersToInNode.put(node, 0 );
                    }
                    numberOfInitialHandlersToInNode.put(node, numberOfInitialHandlersToInNode.get(node) + 1);
                }
            }
            writer.println("initialHandling :" + initialHandling.substring(0, initialHandling.length()-1) + "]");

            // Set mode
            writer.println("mode : "+Constants.OBJECTIVE_MODE);

            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e){
            System.out.println(e.getMessage());
        }
        //System.out.println("Written to file.");

    }

    public ArrayList<Car> getCars() {
        return cars;
    }

    public HashMap<ChargingNode, ParkingNode> getChargingToParkingNode() {
        return chargingToParkingNode;
    }

    public HashMap<ParkingNode, ChargingNode> getParkingNodeToChargingNode() {
        return parkingNodeToChargingNode;
    }

    public int getNumberOfCarsTakenByCustomers() {
        return numberOfCarsTakenByCustomers;
    }

    public void setNumberOfCarsTakenByCustomers(int numberOfCarsTakenByCustomers) {
        this.numberOfCarsTakenByCustomers = numberOfCarsTakenByCustomers;
    }

    public double getHandlingTimeP() {
        return handlingTimeP;
    }

    public double getHandlingTimeC() {
        return handlingTimeC;
    }

    @Override
    public String toString() {
        Collections.sort(cars);
        String carsCharging = "[";
        int numberOfCarsCharging = 0;
        for (ChargingNode node: this.chargingNodes) {
            carsCharging += node.getCarsCurrentlyCharging().size() + " ";
            numberOfCarsCharging += node.getCarsCurrentlyCharging().size();
        }
        carsCharging = carsCharging.substring(0,carsCharging.length() -1) + "]";

        String[] initialRegularInPStringArray = initialRegularInPString.substring(1,initialRegularInPString.length()-1).split(" ");
        int numberOfInitialRegular = 0;
        for (String s: initialRegularInPStringArray) {
            numberOfInitialRegular += Integer.parseInt(s);
        }

        String[] initialInNeedPStringArray = initialInNeedPString.substring(1,initialInNeedPString.length()-1).split(" ");
        int numberOfInitialInNeedP = 0;
        for (String s: initialInNeedPStringArray) {
            numberOfInitialInNeedP += Integer.parseInt(s);
        }

        int numberInitialhandling = 0;
        initialHandlingString = "[";
        for (Operator operator : operators) {
            numberInitialhandling += (operator.isHandling() ? 1:0);
            initialHandlingString += (operator.isHandling() ? 1:0) + " ";
        }
        initialHandlingString = initialHandlingString.substring(0, initialHandlingString.length() -1 ) + "]";


        int totalCarsInSystem = numberOfCarsCharging + numberOfInitialInNeedP + numberOfInitialRegular + numberOfCarsTakenByCustomers + numberInitialhandling;

        // chargingSlotsAvailable and finished during period
        String chargingSlotsAvailableArray = "[";
        for (ChargingNode cNode : chargingNodes) {
            int carsFinishedChargingDuringPeriod = cNode.findNumberOfCarsFinishingChargingDuringNextPeriod();
            int chargingSpotsAvailableNow = cNode.getNumberOfTotalChargingSlots() - cNode.getCarsCurrentlyCharging().size();
            chargingSlotsAvailableArray += (carsFinishedChargingDuringPeriod + chargingSpotsAvailableNow) +" ";
        }
        chargingSlotsAvailableArray = chargingSlotsAvailableArray.substring(0,chargingSlotsAvailableArray.length()-1) + "]";

        return  "\n\t  ProblemInstance " + exampleNumber +":"+
                "\n\t  parkingNodes              = " + parkingNodes +
                "\n\t  chargingNodes             = " + chargingNodes +
                "\n\t  cars                      = " + cars +
                "\n\t  operators                 = " + operators +
                "\n\t  chargingSlotsAvailable    = " + chargingSlotsAvailableArray +
                "\n\t  carsCharging              = " + carsCharging +
                "\n\t  travelTimeToOriginRString = " + travelTimeToOriginRString +
                "\n\t  initialHandling           = " + initialHandlingString +
                "\n\t  tavelTimeToParkingAString = " + tavelTimeToParkingAString+
                "\n\t  initialRegularInPString   = " + initialRegularInPString +"(total = " + numberOfInitialRegular+")"+
                "\n\t  initialInNeedPString      = " + initialInNeedPString +"(total = " + numberOfInitialInNeedP+")"+
                "\n\t  idealStatePString         = " + idealStatePString +
                "\n\t  demandPString             = " + demandPString  +
                "\n\t  carsTakenByCustomers      = " + numberOfCarsTakenByCustomers +
                "\n\t  totalCarsInSystem         = " + totalCarsInSystem;
    }
}

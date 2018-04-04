package code.problem;

import code.solver.heuristics.entities.CarMove;
import constants.Constants;
import constants.FileConstants;
import constants.SimulationConstants;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import code.problem.entities.Car;
import code.problem.entities.Operator;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;
import utils.ChromosomeGenerator;
import utils.MathHelper;

@SuppressWarnings("serial")
public class ProblemInstance implements Serializable{

	private String filePath;
    private String fileName;
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
    private int carsInNeedOfCharging = 0;
    private double maxTravelTimeCar = Double.MAX_VALUE;
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


    public ProblemInstance(String filePath) {
    	this.filePath = filePath;
    	String[] split = filePath.split("/");
        this.fileName = split[split.length-1];
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
            handleInputFileMap();
            addStateSpecificStrings();
            makeChargingToparkingNodeMap();
        } catch (IOException e){
            System.out.println("File could not be read for "+fileName + " in problemInstance");
            System.exit(1);
        }
        updateParameters();


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
        stateSpecificKeys.add("visitList");
        stateSpecificKeys.add("numVisits");
        stateSpecificKeys.add("costOfDeviation");
        stateSpecificKeys.add("costOfPostponedCharging");

    }

    private void readProblemFromFile() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(this.filePath + ".txt"));
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
        String timeRemainingString = inputFileMap.get("travelTimeToOriginR");
        String[] timeRemainingList = timeRemainingString.substring(1,timeRemainingString.length()-1).split(" ");
        for (int operatorId = Constants.START_INDEX; operatorId < startNodeList.length+Constants.START_INDEX; operatorId++) {
            int nodeId = Integer.parseInt(startNodeList[operatorId-Constants.START_INDEX]);
            Node node = nodeMap.get(nodeId);
            Operator newOperator = new Operator(operatorId);
            newOperator.setNextOrCurrentNode(node);
            Double remainingTime = Double.parseDouble(timeRemainingList[operatorId-Constants.START_INDEX]);
            newOperator.setTimeRemainingToCurrentNextNode(remainingTime);
            operators.add(newOperator);
            operatorMap.put(operatorId,newOperator);
        }
        this.numROperators = this.operators.size();
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

        ArrayList<Integer> initialIdealStateArray = new ArrayList<>();
        if(inputFileMap.get("idealStateP").length() > 2){
            String[] idealStateStringArray = inputFileMap.get("idealStateP").replace("[","").replace("]","").split(" ");
            for(i = 0; i < idealStateStringArray.length; i ++){
                initialIdealStateArray.add(Integer.parseInt(idealStateStringArray[i]));
            }
        }

        // Create nodes
        int carId = Constants.START_INDEX;
        for (i = Constants.START_INDEX; i < numPNodes+Constants.START_INDEX; i++) {
            ParkingNode newParkingNode = new ParkingNode(i);
            newParkingNode.setIdealNumberOfAvailableCarsThisPeriod(initialIdealStateArray.get(i - Constants.START_INDEX));
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
                Car newCarInNeed = new Car(carId,SimulationConstants.SOFT_CHARGING_THRESHOLD*0.999);
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
                double time = Double.parseDouble(timeDouble);
                newTimeRow.add(time);
                this.maxTravelTimeCar = Double.max(this.maxTravelTimeCar,time);
            }
            travelTimesCar.add(newTimeRow);
        }

        // Get travel times bike
        timeString = inputFileMap.get("travelTimeBike");
        timeRows = timeString.substring(1, timeString.length()-1).split(",");
        for (String timeRow : timeRows) {
            ArrayList<Double> newTimeRow = new ArrayList<>();
            for (String timeDouble : timeRow.split(" ")) {
                double time = Double.parseDouble(timeDouble);
                newTimeRow.add(time);
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
            nodesWithVaryingDemand =  (int) Math.round(numPNodes*SimulationConstants.PERCENTAGE_AFFECTED_BY_RUSH_HOUR);
            nodesWithHighDemand = (int) Math.round(nodesWithVaryingDemand*SimulationConstants.PERCENTAGE_RUSH_HOUR_SPLIT);
        } catch (Exception e){
            System.out.println("Casting Error");
            nodesWithVaryingDemand = 0;
            nodesWithHighDemand = 0;
        }

        for (int i = Constants.START_INDEX; i < numPNodes + Constants.START_INDEX; i++) {
            ParkingNode parkingNode = (ParkingNode) nodeMap.get(i);
            if(i >= nodesWithVaryingDemand + Constants.START_INDEX){
                parkingNode.setDemandGroup(SimulationConstants.nodeDemandGroup.NEUTRAL);
            }else if( i < nodesWithHighDemand+Constants.START_INDEX){
                parkingNode.setDemandGroup(SimulationConstants.nodeDemandGroup.MORNING_RUSH);
            } else {
                parkingNode.setDemandGroup(SimulationConstants.nodeDemandGroup.MIDDAY_RUSH);
            }
        }
    }

    public String getFileName() {
        return fileName;
    }
    
    public String getFilePath() {
    	return this.filePath;
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
    
    public double getTravelTimeBike(Node n1, Node n2) {
    		return this.travelTimesBike.get(n1.getNodeId()-Constants.START_INDEX).get(n2.getNodeId()-Constants.START_INDEX);
    }
    
    public double getTravelTimeCar(Node n1, Node n2) {
		return this.travelTimesCar.get(n1.getNodeId()-Constants.START_INDEX).get(n2.getNodeId()-Constants.START_INDEX);
}

    public ArrayList<ArrayList<Double>> getTravelTimesCar() {
        return travelTimesCar;
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
        try{
            PrintWriter writer = new PrintWriter(FileConstants.TEST_DYNAMIC_FOLDER + fileName +".txt", "UTF-8");
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
                chargingSlotsAvailableMap.put(cNode,chargingSpotsAvailableNow + carsFinishedChargingDuringPeriod);
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

            //Calculate number of visits
            HashMap<Node,Integer> lambdaMap = new HashMap<>();
            HashMap<Node,Integer> thetaMap = new HashMap<>();
            HashMap<Node,Integer> tauMap = new HashMap<>();
            HashMap<Node,Integer> phiMap = new HashMap<>();

            for (ParkingNode node : parkingNodes) {
                // lambda
                addToHashNodeIntMap(lambdaMap, node, numberOfCarsArtificiallyArrivingToParkingNode.get(node));
                addToHashNodeIntMap(lambdaMap, node, numberOfInitialHandlersToInNode.get(node));
                // theta
                addToHashNodeIntMap(thetaMap, node, numberOfCarsArtificiallyArrivingToParkingNode.get(node));
                addToHashNodeIntMap(thetaMap, node, numberOfOperatorsStartingInNode.get(node));
                // tau
                addToHashNodeIntMap(tauMap, node, numberOfCarsArtificiallyArrivingToParkingNode.get(node));
                // phi
                addToHashNodeIntMap(phiMap, node, numberOfInitialHandlersToInNode.get(node));
            }

            for (ChargingNode node : chargingNodes) {
                // lambda
                addToHashNodeIntMap(lambdaMap, node, numberOfInitialHandlersToInNode.get(node));
                // theta
                addToHashNodeIntMap(thetaMap, node, numberOfOperatorsStartingInNode.get(node));
                // tau
                addToHashNodeIntMap(tauMap, node, 0);
                // phi
                addToHashNodeIntMap(phiMap, node, numberOfInitialHandlersToInNode.get(node));
            }
            String visitList = "[";
            int maxVisit = 0;
            for (ParkingNode node : parkingNodes) {
                int initRegular = node.getCarsRegular().size();
                int ideal = node.getIdealNumberOfAvailableCars();
                int demand = node.getPredictedNumberOfCarsDemandedThisPeriod();
                int inNeed = node.getCarsInNeed().size();
                int left = initRegular - (ideal + demand);
                int right = thetaMap.get(node) - lambdaMap.get(node);
                int omega = (left <= right ? lambdaMap.get(node) : thetaMap.get(node));
                boolean isDeficit = initRegular + lambdaMap.get(node) - (ideal + demand) < 0;
                boolean isSurplus = initRegular + lambdaMap.get(node) - (ideal + demand) > 0;
                boolean isNeither = initRegular + lambdaMap.get(node) - (ideal + demand) == 0;
                int visits;
                if(isDeficit){
                    visits = (ideal + demand) - (initRegular  + lambdaMap.get(node)) + inNeed + thetaMap.get(node);
                }else if (isSurplus){
                    visits = (initRegular + tauMap.get(node)) - (ideal + demand) + inNeed + omega;
                } else {
                    visits = (inNeed) + thetaMap.get(node);
                }
                maxVisit = Math.max(visits, maxVisit);
                visitList += Math.max(visits,2) + " ";
            }
            for (ChargingNode node : chargingNodes) {
                int inNeedThatMayReachNode = 0;
                for (ParkingNode pNode : parkingNodes) {
                    if(travelTimesCar.get(pNode.getNodeId() - Constants.START_INDEX).get(node.getNodeId()- Constants.START_INDEX) <= Constants.TIME_LIMIT_STATIC_PROBLEM  ){
                        inNeedThatMayReachNode += pNode.getCarsInNeed().size();
                    }
                }
                int second = chargingSlotsAvailableMap.get(node) - phiMap.get(node);
                int min = Math.min(inNeedThatMayReachNode, second) + carsFinishedChargingMap.get(node) + thetaMap.get(node);
                min =  Math.max(2,min);
                visitList += min + " ";
                maxVisit = Math.max(min, maxVisit);
            }
            for (Operator operator : operators) {
                visitList += "1 1 ";
            }
            visitList = visitList.substring(0, visitList.length() -1) + "]";
            writer.println("visitList : " + visitList);
            writer.println("numVisits : " + maxVisit);


            // add cost parameters
            writer.println("costOfPostponedCharging : "+ Constants.COST_POSTPONED);
            writer.println("costOfDeviation : "+ Constants.COST_DEVIATION);



            // Add Parameters for car moves mosel solver ----------------------
            // Num car moves p først
            HashMap<Car, ArrayList<CarMove>> carMoves = ChromosomeGenerator.generateCarMovesFrom(this);
            int numCarMovesP = 0;
            int numCarMovesC = 0;
            int numCars = carMoves.keySet().size();
            int numTasks = (int) Constants.TIME_LIMIT_STATIC_PROBLEM / 10;
            int numDeficitNodes = 0;
            ArrayList<Integer> deficitTranslate = new ArrayList<>();
            ArrayList<Integer> deficitCarsInNode = new ArrayList<>();
            ArrayList<Integer> carMoveCarsP = new ArrayList<>();
            ArrayList<Integer> carMoveOriginP = new ArrayList<>();
            ArrayList<Integer> carMoveDestinationP = new ArrayList<>();
            ArrayList<Double> carMoveHandlingTimeP = new ArrayList<>();
            ArrayList<Double> carMoveStartingTimeP = new ArrayList<>();
            ArrayList<Integer> carMoveCarsC = new ArrayList<>();
            ArrayList<Integer> carMoveOriginC = new ArrayList<>();
            ArrayList<Integer> carMoveDestinationC = new ArrayList<>();
            ArrayList<Double> carMoveHandlingTimeC = new ArrayList<>();
            ArrayList<Double> carMoveStartingTimeC = new ArrayList<>();
            int numCarsInCNeedNodes = 0; // Det er antall noder med biler som må lades
            ArrayList<Integer> carsInNeedCTranslate = new ArrayList<>();
            ArrayList<Integer> carsInNeedNodes = new ArrayList<>();
            ArrayList<Double> bigMP = new ArrayList<>();
            ArrayList<Double> bigMC = new ArrayList<>();


            // find num deficit nodes:
            HashSet<ParkingNode> deficitNodes = new HashSet<>();
            HashSet<ParkingNode> nodesWithCarsInNeed = new HashSet<>();
            for(ParkingNode parkingNode : parkingNodes){
                if(ChromosomeGenerator.findNumberOfCarsToMoveIn(parkingNode, this) <  0){
                    deficitNodes.add(parkingNode);
                    deficitTranslate.add(parkingNode.getNodeId());
                    deficitCarsInNode.add(-1 * ChromosomeGenerator.findNumberOfCarsToMoveIn(parkingNode, this) );
                }if(parkingNode.getCarsInNeed().size() > 0){
                    nodesWithCarsInNeed.add(parkingNode);
                    carsInNeedCTranslate.add(parkingNode.getNodeId());
                    carsInNeedNodes.add(parkingNode.getCarsInNeed().size());
                }
            }
            numCarsInCNeedNodes = nodesWithCarsInNeed.size();
            numDeficitNodes = deficitNodes.size();

            ArrayList<Car> cars = new ArrayList<>(carMoves.keySet());
            for(Car car : cars){
                for(CarMove carMove : carMoves.get(car)){
                    double bigM = -1;
                    for(Car car2 : cars){
                        for(CarMove carMove2 : carMoves.get(car2)){
                            for(ParkingNode parkingNode : parkingNodes){
                                if(ChromosomeGenerator.findNumberOfCarsToMoveIn(parkingNode, this) > 0
                                        || parkingNode.getCarsInNeed().size() > 0){
                                    bigM = Math.max(getTravelTimeBike(carMove.getToNode(), parkingNode)
                                            - (getTravelTimeBike(carMove2.getToNode(), parkingNode)
                                            + carMove2.getTravelTime()), bigM);
                                }
                            }
                        }
                    }
                    bigM = MathHelper.round(bigM, 2);
                    if(carMove.isToCharging()){
                        numCarMovesC ++;
                        carMoveCarsC.add(car.getCarId());
                        carMoveOriginC.add(carMove.getFromNode().getNodeId());
                        carMoveDestinationC.add(carMove.getToNode().getNodeId());
                        carMoveHandlingTimeC.add(carMove.getTravelTime()); // only travel time?
                        carMoveStartingTimeC.add(carMove.getEarliestDepartureTime());
                        bigMC.add(bigM);
                    } else {
                        numCarMovesP ++;
                        carMoveCarsP.add(car.getCarId());
                        carMoveOriginP.add(carMove.getFromNode().getNodeId());
                        carMoveDestinationP.add(carMove.getToNode().getNodeId());
                        carMoveHandlingTimeP.add(carMove.getTravelTime()); // only travel time?
                        carMoveStartingTimeP.add(carMove.getEarliestDepartureTime());
                        bigMP.add(bigM);
                    }


                }
            }

            ArrayList<Integer> carMoveCars = new ArrayList<>(carMoveCarsP);
            carMoveCars.addAll(carMoveCarsC);
            ArrayList<Integer> carMoveOrigin = new ArrayList<>(carMoveOriginP);
            carMoveOrigin.addAll(carMoveOriginC);
            ArrayList<Integer> carMoveDestination = new ArrayList<>(carMoveDestinationP);
            carMoveDestination.addAll(carMoveDestinationC);
            ArrayList<Double> carMoveHandlingTime = new ArrayList<>(carMoveHandlingTimeP);
            carMoveHandlingTime.addAll(carMoveHandlingTimeC);
            ArrayList<Double> carMoveStartingTime = new ArrayList<>(carMoveStartingTimeP);
            carMoveStartingTime.addAll(carMoveStartingTimeC);
            ArrayList<Double> bigMCars = new ArrayList<>(bigMP);
            bigMCars.addAll(bigMC);

            writer.println();
            writer.println("numCarMovesP : " + numCarMovesP);
            writer.println("numCarMovesC : " + numCarMovesC);
            writer.println("numCars : " + numCars);
            writer.println("numTasks : " + numTasks);
            writer.println("numDeficitNodes : " + numDeficitNodes);
            writer.println();
            writer.println("deficitTranslate : " + integerArrayListToString(deficitTranslate, " "));
            writer.println("deficitCarsInNode : " + integerArrayListToString(deficitCarsInNode, " "));
            writer.println("carMoveCars : " + integerArrayListToString(carMoveCars, " "));
            writer.println("carMoveOrigin : " + integerArrayListToString(carMoveOrigin, " "));
            writer.println("carMoveDestination : " + integerArrayListToString(carMoveDestination, " "));
            writer.println("carMoveHandlingTime : " + doubleArrayListToString(carMoveHandlingTime, " "));
            writer.println("carMoveStartingTime : " + doubleArrayListToString(carMoveStartingTime, " "));
            writer.println("numCarsInCNeedNodes : " + numCarsInCNeedNodes);
            writer.println("carsInNeedCTranslate : " + integerArrayListToString(carsInNeedCTranslate, " "));
            writer.println("carsInNeedNodes : " + integerArrayListToString(carsInNeedNodes, " "));
            writer.println("bigMCars : " + doubleArrayListToString(bigMCars, " "));


            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e){
            System.out.println(e.getMessage());
        }
    }

    private String integerArrayListToString(ArrayList<Integer> arrayList, String delimiter){
        return "[" + arrayList.stream().map(Object::toString)
                .collect(Collectors.joining(delimiter)) + "]";
    }

    private String doubleArrayListToString(ArrayList<Double> arrayList, String delimiter){
        return "[" + arrayList.stream().map(Object::toString)
                .collect(Collectors.joining(delimiter)) + "]";
    }

    private void addToHashNodeIntMap(HashMap<Node,Integer> map, Node node, Integer elem ){
        if(map.get(node) == null){
            map.put(node, (elem==null ? 0 : elem));
        } else{
            map.put(node, map.get(node) + (elem==null ? 0 : elem));
        }
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
    
    public void setNumberOfCarsTakenByCustomers(int numberOfCarsTakenByCustomers) {
        this.numberOfCarsTakenByCustomers = numberOfCarsTakenByCustomers;
    }

    public double getMaxTravelTimeCar() {
        return maxTravelTimeCar;
    }

    public double getHandlingTimeP() {
        return handlingTimeP;
    }

    public double getHandlingTimeC() {
        return handlingTimeC;
    }

    public void updateCarsInNeedOfCharging(){
        this.carsInNeedOfCharging = 0;
        for(ParkingNode parkingNode : parkingNodes){
            carsInNeedOfCharging += parkingNode.getCarsInNeed().size();
        }
    }

    private void updateNumberOfAvailableChargingStations(){
        // Do not consider the number of cars that will finish charging.
        // Available = total slots - (num charging + number of operators arriving with car)
        HashMap<ChargingNode, Integer> availableChargingStations = new HashMap<>();
        for(ChargingNode chargingNode : chargingNodes){
            availableChargingStations.put(chargingNode, chargingNode.getNumberOfTotalChargingSlots() - chargingNode.getCarsCurrentlyCharging().size());
        }
        for(Operator operator : operators){
            Node arrivalNode = operator.getNextOrCurrentNode();
            if(arrivalNode instanceof ChargingNode && operator.getTimeRemainingToCurrentNextNode() > 0 && operator.getTimeRemainingToCurrentNextNode() < Constants.TIME_LIMIT_STATIC_PROBLEM){
                ChargingNode cArrivalNode = (ChargingNode) arrivalNode;
                availableChargingStations.put(cArrivalNode, availableChargingStations.get(cArrivalNode)  - 1);
            }
        }
        for(ChargingNode chargingNode : chargingNodes){
            chargingNode.setNumberOfAvailableChargingSpotsNextPeriod(availableChargingStations.get(chargingNode));
        }
    }

    private void updateNumberOfArrivingCarsInParkingNodes(){
        // Both cars finishing charging and those operators arriving
        HashMap<ParkingNode, Integer> carsArriving = new HashMap<>();
        for(ParkingNode parkingNode : parkingNodes){
            carsArriving.put(parkingNode, 0);
        }
        // Find cars finishing charging
        for(ChargingNode chargingNode : chargingNodes){
            for(Car car : chargingNode.getCarsCurrentlyCharging()){
                if(car.getRemainingChargingTime() < Constants.TIME_LIMIT_STATIC_PROBLEM){
                    ParkingNode associatedParkingNode = chargingToParkingNode.get(chargingNode);
                    carsArriving.put(associatedParkingNode, carsArriving.get(associatedParkingNode) + 1);
                }
            }
        }
        // Find operators arriving to parking node
        for(Operator operator : operators){
            if( operator.getTimeRemainingToCurrentNextNode() > 0
                    && Constants.TIME_LIMIT_STATIC_PROBLEM > operator.getTimeRemainingToCurrentNextNode()
                    && operator.getNextOrCurrentNode() instanceof ParkingNode){
                ParkingNode parkingNode = (ParkingNode) operator.getNextOrCurrentNode();
                carsArriving.put(parkingNode, carsArriving.get(parkingNode) + 1);
            }
        }
        for(ParkingNode parkingNode : parkingNodes){
            parkingNode.setCarsArrivingThisPeriod(carsArriving.get(parkingNode));
        }

    }

    public int getCarsInNeedOfCharging() {
        return carsInNeedOfCharging;
    }

    public int getNumROperators() {
        return numROperators;
    }

    public void updateParameters(){
        updateNumberOfAvailableChargingStations();
        updateCarsInNeedOfCharging();
        updateNumberOfArrivingCarsInParkingNodes();
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

        return  "\n\t  ProblemInstance " + fileName +":"+
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

package tio4500;

import constants.Constants;
import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.ChargingNode;
import tio4500.simulations.Nodes.Node;
import tio4500.simulations.Nodes.ParkingNode;
import tio4500.simulations.Travels.CustomerTravel;
import tio4500.simulations.Travels.OperatorTravel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

    private int numPNodes = 0;
    private int numCNodes = 0;
    private int numROperators = 0;

    private HashMap<String, String> inputFileMap = new HashMap<>();

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
        try {
            readProblemFromFile();
        } catch (IOException e){
            System.out.println("File could not be read for example "+exampleNumber);
        }
        handleInputFileMap();
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
            newOperator.setCurrentNode(node);
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

        int carId = Constants.START_INDEX;
        for (i = Constants.START_INDEX; i < numPNodes+Constants.START_INDEX; i++) {
            ParkingNode newParkingNode = new ParkingNode(i);
            parkingNodes.add(newParkingNode);
            addNodeToNodeMap(newParkingNode);
            for (int j = 0; j < initialRegularInPArray.get(i-Constants.START_INDEX); j++) {
                Car newRegularCar = new Car(carId,1.0,newParkingNode);
                carId++;
                newParkingNode.addRegularCar(newRegularCar);
                cars.add(newRegularCar);
            }
            for (int j = 0; j < initialInNeedPArray.get(i-Constants.START_INDEX); j++) {
                Car newCarInNeed = new Car(carId,Constants.CHARGING_THRESHOLD*0.999,newParkingNode);
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

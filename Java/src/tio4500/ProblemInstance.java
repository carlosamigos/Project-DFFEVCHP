package tio4500;

import constants.Constants;
import tio4500.simulations.Entities.Car;
import tio4500.simulations.Entities.Operator;
import tio4500.simulations.Nodes.ChargingNode;
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
    private ArrayList<Car> rentalCars;
    private ArrayList<Operator> operators;
    private ArrayList<OperatorTravel> operatorTravels;
    private ArrayList<CustomerTravel> customerTravels;
    private ArrayList<ArrayList<Double>> travelTimesBike = new ArrayList<>();
    private ArrayList<ArrayList<Double>> travelTimesCar = new ArrayList<>();

    private int numPNodes = 0;
    private int numCNodes = 0;
    private int numROperators = 0;

    private HashMap<String, String> inputFileMap = new HashMap<>();

    public ProblemInstance(int exampleNumber) {
        this.exampleNumber = exampleNumber;
        this.parkingNodes = new ArrayList<>();
        this.chargingNodes = new ArrayList<>();
        this.rentalCars = new ArrayList<>();
        this.operators = new ArrayList<>();
        this.operatorTravels = new ArrayList<>();
        this.customerTravels = new ArrayList<>();
        try {
            readProblemFromFile();
        } catch (IOException e){
            System.out.println("File could not be read for example "+exampleNumber);
        }
        System.out.println(inputFileMap);
        handleInputFileMap();


    }

    public void readProblemFromFile() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(Constants.INITIAL_STATE_FOLDER + "example"+Integer.toString(exampleNumber) + ".txt"));
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
            String everything = sb.toString();
        } finally {
            br.close();
        }
    }

    public void handleInputFileMap(){
        numPNodes = Integer.parseInt(inputFileMap.get("numPNodes"));
        numCNodes = Integer.parseInt(inputFileMap.get("numCNodes"));
        numROperators = Integer.parseInt(inputFileMap.get("numROperators"));
        //TODO
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

    public ArrayList<Car> getRentalCars() {
        return rentalCars;
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

}

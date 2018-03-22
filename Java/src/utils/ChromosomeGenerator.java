package utils;

import code.problem.ProblemInstance;
import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import constants.HeuristicsConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChromosomeGenerator {


    public static HashMap<Car, ArrayList<CarMove>> generateCarMovesFrom(ProblemInstance problemInstance){


        //TODO: Generate car moves that corresponds to incomming cars, and cars that finishes charging
        HashMap<Car, ArrayList<CarMove>> carMoves = new HashMap<>();
        HashSet<Car> carsUsed = new HashSet<>();
        ArrayList<Car> carsInNeedOfCharging = new ArrayList<>();
        ArrayList<ParkingNode> senderNodes = new ArrayList<>();
        ArrayList<ParkingNode> receiverNodes = new ArrayList<>();

        // Find sender and receiver nodes
        findSenderAndReceiverNodes(problemInstance, senderNodes, receiverNodes);

        // Find cars that needs charging
        findCarsInNeedOfCharging(problemInstance, carsInNeedOfCharging);

        // Create car moves for regular cars (doesn't use cars finishing charging at the moment)
        createCarMovesForRegularCars(carMoves, senderNodes, receiverNodes, carsUsed, problemInstance);

        // Create car moves for cars in need of charging
        createCarMovesForCarsInNeedOfCharging(carMoves, problemInstance);

        return carMoves;
    }


    // Return diff from ideal to current number of cars
    public static Integer findNumberOfCarsToMoveIn(ParkingNode parkingNode, ProblemInstance problemInstance){
        return parkingNode.getCarsRegular().size() - parkingNode.getIdealNumberOfAvailableCars();
    }

    private static void createCarMovesForRegularCars(HashMap<Car, ArrayList<CarMove>> carMoves, ArrayList<ParkingNode> senderNodes,
                                                     ArrayList<ParkingNode> receiverNodes, HashSet<Car> carsUsed,
                                                     ProblemInstance problemInstance) throws NullPointerException{

        double carMoveTimeThreshold = HeuristicsConstants.MAX_THRESHOLD_CARMOVE_DISTANCE;
        double travelTime;
        for(ParkingNode senderNode : senderNodes){
            int numberOfCarsToMove = findNumberOfCarsToMoveIn(senderNode,problemInstance);
            if(numberOfCarsToMove > senderNode.getCarsRegular().size()){
                throw new NullPointerException("Too few cars in node compared to ideal state");
            }
            for(Car carInCarMove : senderNode.getCarsRegular().subList(0, numberOfCarsToMove)){
                carMoves.put(carInCarMove, new ArrayList<>());
                for(ParkingNode receiverNode : receiverNodes){
                		travelTime = problemInstance.getTravelTimeCar(senderNode, receiverNode);
                		if(travelTime < problemInstance.getMaxTravelTimeCar() * carMoveTimeThreshold && carInCarMove.getCurrentNextNode() == carInCarMove.getPreviousNode()){
                			carMoves.get(carInCarMove).add( new CarMove(senderNode, receiverNode, carInCarMove, travelTime, 0.0));
                    }
                }

            }
        }
    }


    private static void findSenderAndReceiverNodes(ProblemInstance problemInstance, ArrayList<ParkingNode> senderNodes,
                                                   ArrayList<ParkingNode> receiverNodes){
        // Sender nodes: findNumberOfCarsToMoveIn > 0
        // Receiver nodes: findNumberOfCarsToMoveIn < 0
        for(ParkingNode parkingNode : problemInstance.getParkingNodes()){
            if(findNumberOfCarsToMoveIn(parkingNode, problemInstance) > 0 ){
                senderNodes.add(parkingNode);
            }
            else if (findNumberOfCarsToMoveIn(parkingNode, problemInstance) < 0 ){
                receiverNodes.add(parkingNode);
            }
        }
    }

    private static void findCarsInNeedOfCharging(ProblemInstance problemInstance, ArrayList<Car> carsInNeedOfCharging){
        for (ParkingNode parkingNode: problemInstance.getParkingNodes()) {
            for(Car car : parkingNode.getCarsInNeed()){
                carsInNeedOfCharging.add(car);
            }
        }
    }


    private static void createCarMovesForCarsInNeedOfCharging(HashMap<Car, ArrayList<CarMove>> carMoves, ProblemInstance problemInstance){
        for(ParkingNode parkingNode : problemInstance.getParkingNodes()){
            for(Car car : parkingNode.getCarsInNeed()){
                carMoves.put(car, new ArrayList<>());
                for(ChargingNode chargingNode : problemInstance.getChargingNodes()){
                		double travelTime = problemInstance.getTravelTimeCar(parkingNode, chargingNode);
                		carMoves.get(car).add(new CarMove(parkingNode, chargingNode, car, travelTime, 0.0));
                }
            }
        }
    }

    public static String generateToStringFrom(HashMap<Car, ArrayList<CarMove>> carMoves){
        String ret = "\n";
        for(Car car: carMoves.keySet()){
            ret += "Car " + car.getCarId() + ": ";
            for(CarMove carMove : carMoves.get(car)){
                ret += carMove.toString();
            }
            ret += "\n";
        }
        return ret;
    }




}

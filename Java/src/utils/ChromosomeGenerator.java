package utils;

import code.problem.ProblemInstance;
import code.problem.entities.Car;
import code.problem.entities.Operator;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import constants.Constants;
import constants.HeuristicsConstants;
import constants.SimulationConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChromosomeGenerator {


    public static HashMap<Car, ArrayList<CarMove>> generateCarMovesFrom(ProblemInstance problemInstance){

        HashMap<Car, ArrayList<CarMove>> carMoves = new HashMap<>();
        ArrayList<Car> carsInNeedOfCharging = new ArrayList<>();
        ArrayList<ParkingNode> senderNodes = new ArrayList<>();
        ArrayList<ParkingNode> receiverNodes = new ArrayList<>();
        HashMap<ParkingNode, ArrayList<Car>> incomingCarsToParkingNodes = new HashMap<>();
        HashMap<Car,Double> remainingTravelTime = new HashMap<>();


        // Find incoming cars over the next TIME_LIMIT minutes
        findIncomingCars(incomingCarsToParkingNodes,remainingTravelTime, problemInstance);

        // Find sender and receiver nodes
        findSenderAndReceiverNodes(problemInstance, senderNodes, receiverNodes, incomingCarsToParkingNodes);

        // Find cars that needs charging
        findCarsInNeedOfCharging(problemInstance, carsInNeedOfCharging);

        // Create car moves for regular cars that are not charging, but may be incoming. For incoming:
        // (only necessary when nodes changes from deficit to surplus)
        createCarMovesForRegularCars(carMoves, senderNodes, receiverNodes, problemInstance,
                incomingCarsToParkingNodes, remainingTravelTime);

        // Create car moves for cars in need of charging
        createCarMovesForCarsInNeedOfCharging(carMoves, problemInstance);

        // Create car moves for cars finishing charging
        createCarMovesForCarsFinishingCharging(carMoves, problemInstance, receiverNodes);


        return carMoves;
    }

    private static void findIncomingCars(HashMap<ParkingNode, ArrayList<Car>> incomingCarsToParkingNodes,
                                        HashMap<Car,Double> remainingTravelTime, ProblemInstance problemInstance){
        for(Operator operator : problemInstance.getOperators()){
            Car car = operator.getCar();
            Node arrivalNode = operator.getNextOrCurrentNode();
            if(car != null && operator.isHandling() && arrivalNode instanceof ParkingNode
                    && operator.getTimeRemainingToCurrentNextNode() <
                    Constants.TIME_LIMIT_STATIC_PROBLEM * SimulationConstants.INCOMING_CARS_CONSIDERATION_PROPORTION){
                ParkingNode pNode = (ParkingNode) arrivalNode;
                if(incomingCarsToParkingNodes.get(pNode) == null){
                    incomingCarsToParkingNodes.put( pNode, new ArrayList<>());
                }
                incomingCarsToParkingNodes.get(pNode).add(car);
                remainingTravelTime.put(car, operator.getTimeRemainingToCurrentNextNode());
            }
        }
    }

    // Return diff from ideal to current number of cars, not taking into account number of arriving
    public static Integer findNumberOfCarsToMoveIn(ParkingNode parkingNode, ProblemInstance problemInstance){
        return parkingNode.getCarsRegular().size() - parkingNode.getIdealNumberOfAvailableCars();
    }

    private static void createCarMovesForRegularCars(HashMap<Car, ArrayList<CarMove>> carMoves, ArrayList<ParkingNode> senderNodes,
                                                     ArrayList<ParkingNode> receiverNodes,
                                                     ProblemInstance problemInstance,
                                                     HashMap<ParkingNode, ArrayList<Car>> incomingCarsToParkingNodes,
                HashMap<Car,Double> remainingTravelTime) throws NullPointerException{

        double carMoveTimeThreshold = HeuristicsConstants.MAX_THRESHOLD_CARMOVE_DISTANCE;
        double travelTime;
        for(ParkingNode senderNode : senderNodes){
            int incomingCars = (incomingCarsToParkingNodes.get(senderNode) == null)
                    ? 0 : incomingCarsToParkingNodes.get(senderNode).size();
            int numberOfCarsToMove = findNumberOfCarsToMoveIn(senderNode,problemInstance) + incomingCars;
            if(numberOfCarsToMove - incomingCars > senderNode.getCarsRegular().size()){
                throw new NullPointerException("Too few cars in node compared to ideal state");
            }
            int carsAdded = 0;
            for(Car carInCarMove : senderNode.getCarsRegular().subList(0, numberOfCarsToMove - incomingCars)){
                carMoves.put(carInCarMove, new ArrayList<>());
                for(ParkingNode receiverNode : receiverNodes){
                    travelTime = problemInstance.getTravelTimeCar(senderNode, receiverNode) + problemInstance.getHandlingTimeP();
                    if(travelTime < problemInstance.getMaxTravelTimeCar() * carMoveTimeThreshold && carInCarMove.getCurrentNextNode() == carInCarMove.getPreviousNode()){
                        carMoves.get(carInCarMove).add( new CarMove(senderNode, receiverNode, carInCarMove, travelTime, 0.0));
                    }
                }
                carsAdded += 1;
                if(carsAdded >= numberOfCarsToMove){
                    break;
                }
            }
            if(incomingCarsToParkingNodes.get(senderNode) != null) {
                for (Car car : incomingCarsToParkingNodes.get(senderNode)) {
                    carMoves.put(car, new ArrayList<>());
                    for (ParkingNode receiverNode : receiverNodes) {
                        travelTime = problemInstance.getTravelTimeCar(senderNode, receiverNode) + problemInstance.getHandlingTimeP();
                        if (travelTime < problemInstance.getMaxTravelTimeCar() * carMoveTimeThreshold && car.getCurrentNextNode() == car.getPreviousNode()) {
                            carMoves.get(car).add(new CarMove(senderNode, receiverNode, car, travelTime, remainingTravelTime.get(car)));
                        }
                    }
                    carsAdded += 1;
                    if (carsAdded >= numberOfCarsToMove) {
                        break;
                    }
                }
            }
        }
    }

    private static void findSenderAndReceiverNodes(ProblemInstance problemInstance, ArrayList<ParkingNode> senderNodes,
                                                   ArrayList<ParkingNode> receiverNodes,
                                                   HashMap<ParkingNode, ArrayList<Car>> incomingCarsToParkingNodes){
        // Sender nodes: findNumberOfCarsToMoveIn > 0
        // Receiver nodes: findNumberOfCarsToMoveIn < 0
        for(ParkingNode parkingNode : problemInstance.getParkingNodes()){
            int incomingCars = (incomingCarsToParkingNodes.get(parkingNode) == null)
                    ? 0 : incomingCarsToParkingNodes.get(parkingNode).size();
            if(findNumberOfCarsToMoveIn(parkingNode, problemInstance) + incomingCars > 0 ){
                senderNodes.add(parkingNode);
            }
            else if (findNumberOfCarsToMoveIn(parkingNode, problemInstance) + incomingCars < 0 ){
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
                		double travelTime = problemInstance.getTravelTimeCar(parkingNode, chargingNode) + problemInstance.getHandlingTimeC();
                		carMoves.get(car).add(new CarMove(parkingNode, chargingNode, car, travelTime, 0.0));
                }
            }
        }
    }

    public static void createCarMovesForCarsFinishingCharging(HashMap<Car, ArrayList<CarMove>> carMoves, ProblemInstance problemInstance,
                                                              ArrayList<ParkingNode> receiverNodes){
        for(ChargingNode chargingNode : problemInstance.getChargingNodes()){
            for(Car car : chargingNode.getCarsCurrentlyCharging()){
                if(car.getRemainingChargingTime() < Constants.TIME_LIMIT_STATIC_PROBLEM){
                    // Car finishes charging
                    carMoves.put(car, new ArrayList<>());
                    ParkingNode fromNode = problemInstance.getChargingToParkingNode().get(chargingNode);
                    for(ParkingNode toNode : receiverNodes){
                        double travelTime = problemInstance.getTravelTimeCar(fromNode, toNode) + problemInstance.getHandlingTimeP();
                        double startTime = car.getRemainingChargingTime();
                        carMoves.get(car).add(new CarMove(fromNode, toNode, car, travelTime, startTime));
                    }
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

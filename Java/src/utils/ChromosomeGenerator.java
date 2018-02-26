package utils;

import code.problem.ProblemInstance;
import code.problem.entities.Car;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.CarMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChromosomeGenerator {



    public static HashMap<Car, ArrayList<CarMove>> generateCarMovesFrom(ProblemInstance problemInstance){

        HashSet<Car> carsUsed = new HashSet<>();
        ArrayList<Car> carsInNeedOfCharging = new ArrayList<>();
        ArrayList<ParkingNode> senderNodes = new ArrayList<>();
        ArrayList<ParkingNode> receiverNodes = new ArrayList<>();

        // Find sender and receiver nodes
        findSenderAndReceiverNodes(problemInstance, senderNodes, receiverNodes);

        // Find cars that needs charging
        //Car car = new Car();
        



        return null;
    }


    // Return diff from ideal to current number of cars
    private static Integer findNumberOfCarsToMoveIn(ParkingNode parkingNode, ProblemInstance problemInstance){
        return parkingNode.getCarsRegular().size() - parkingNode.getIdealNumberOfAvailableCars();
    }

    private static void findSenderAndReceiverNodes(ProblemInstance problemInstance, ArrayList<ParkingNode> senderNodes, ArrayList<ParkingNode> receiverNodes){
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

            }

        }
    }




}

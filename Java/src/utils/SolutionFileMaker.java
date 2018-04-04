package utils;

import code.problem.ProblemInstance;
import code.problem.entities.Operator;
import code.problem.nodes.Node;
import code.solver.heuristics.alns.ALNSIndividual;
import code.solver.heuristics.alns.BestIndividual;
import code.solver.heuristics.entities.CarMove;
import constants.Constants;
import constants.FileConstants;

import java.util.ArrayList;

public class SolutionFileMaker {



    public static void writeSolutionToFile(BestIndividual individual, ProblemInstance problemInstance, String fileName){
        ArrayList<ArrayList<CarMove>> operators = individual.getOperators();
        // Format: operator id: (node number, visitNumber - not important, isHandling, arrival time in node)
        String writeString = "";
        String operatorString;
        for(int i = 0; i < problemInstance.getNumROperators(); i++){
            Operator operator = problemInstance.getOperators().get(i);
            double currentTime = operator.getTimeRemainingToCurrentNextNode();
            Node previousNode = operator.getNextOrCurrentNode();
            int numberOfNodes = problemInstance.getNodeMap().keySet().size();
            int artificialNodeNumber = numberOfNodes + i + 1;
            // Add artificial node:
            operatorString = "" + (i + Constants.START_INDEX) + ": (" + artificialNodeNumber + ",1,0,0),";
            // Add operator's start node
            operatorString += "(" + previousNode.getNodeId() + ",0,0," + currentTime + "),";
            for(CarMove carMove : operators.get(i)){
                double travelTimeFromPrevNodeToFirstNodeInCarMove = problemInstance
                        .getTravelTimeBike(previousNode,carMove.getFromNode());
                double timeOfArrival = currentTime + travelTimeFromPrevNodeToFirstNodeInCarMove;
                if(!previousNode.equals(carMove.getFromNode())){
                    operatorString += "(" +carMove.getFromNode().getNodeId() + ",0,0," + MathHelper.round(timeOfArrival,2) + "),";
                }
                currentTime += getTravelTime(previousNode, carMove, currentTime, problemInstance);
                previousNode = carMove.getToNode();
                operatorString += "(" + carMove.getToNode().getNodeId() + ",0,1," + MathHelper.round(currentTime,2) + "),";
            }
            operatorString = operatorString.substring(0, operatorString.length() - 1) + "\n";
            writeString += operatorString;
        }
        FileHandler fileHandler = new FileHandler(FileConstants.OPERATOR_PATH_OUTPUT_FOLDER + fileName, false);
        fileHandler.writeFile(writeString);
    }
    
    public static double getTravelTime(Node previous, CarMove move, double currentTime, ProblemInstance problemInstance) {
		double travelTimeBike = getTravelTimeBike(previous, move.getFromNode(), problemInstance);
		return travelTimeBike + Math.max(0, move.getEarliestDepartureTime() - (currentTime + travelTimeBike) )
				+ move.getTravelTime();
	}
	
	private static double getTravelTimeBike(Node n1, Node n2, ProblemInstance problemInstance) {
		return problemInstance.getTravelTimeBike(n1, n2);
	}

}

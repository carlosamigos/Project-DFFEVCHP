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
            int artificialStartNodeNumber = numberOfNodes + i + 1;
            // Add artificial node:
            operatorString = "" + (i + Constants.START_INDEX) + ": (" + artificialStartNodeNumber + ",1,0,0),";
            // Add operator's start node
            operatorString += "(" + previousNode.getNodeId() + ",0,"+ (operator.isHandling() ? "1": "0") +"," + currentTime + "),";
            for(CarMove carMove : operators.get(i)){


                if(!previousNode.equals(carMove.getFromNode())){
                    currentTime += getTravelTimeBike(previousNode, carMove.getFromNode(), problemInstance);
                    operatorString += "(" +carMove.getFromNode().getNodeId() + ",0,0," + MathHelper.round(currentTime,2) + "),";
                }
                currentTime += carMove.getTravelTime();
                previousNode = carMove.getToNode();
                operatorString += "(" + carMove.getToNode().getNodeId() + ",0,1," + MathHelper.round(currentTime,2) + "),";
            }
            // Add artificial destination node
            int artificialEndNodeNumber = artificialStartNodeNumber + problemInstance.getNumROperators();
            operatorString += "(" + artificialEndNodeNumber + ",1,0,60)\n";
            writeString += operatorString;
        }
        FileHandler fileHandler = new FileHandler(FileConstants.OPERATOR_PATH_OUTPUT_FOLDER + fileName, false);
        fileHandler.writeFile(writeString);
    }
    

	private static double getTravelTimeBike(Node n1, Node n2, ProblemInstance problemInstance) {
		return problemInstance.getTravelTimeBike(n1, n2);
	}

}

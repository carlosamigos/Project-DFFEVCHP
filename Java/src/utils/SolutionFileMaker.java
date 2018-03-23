package utils;

import code.problem.nodes.Node;
import code.solver.heuristics.alns.ALNSIndividual;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import constants.FileConstants;

import java.util.ArrayList;

public class SolutionFileMaker {



    public static void writeSolutionToFile(ALNSIndividual individual, String fileName){
        ArrayList<Object> operators = individual.getOperators();
        // Format: operator id: (node number, visitNumber - not important, isHandling, arrival time in node)
        String writeString = "";
        String operatorString;
        for(Object obj : operators){
            Operator operator = (Operator) obj;
            double currentTime = operator.getStartTime();
            Node previousNode = operator.getStartNode();
            int numberOfNodes = individual.getProblemInstance().getNodeMap().keySet().size();
            int artificialNodeNumber = numberOfNodes + operator.id + 1;
            // Add artificial node:
            operatorString = "" + operator.id + ": (" + artificialNodeNumber + ",1,0,0),";
            // Add operator's start node
            operatorString += "(" + operator.getStartNode().getNodeId() + ",0,0," + operator.getStartTime() + "),";
            for(CarMove carMove : operator.getCarMoveCopy()){
                double travelTimeFromPrevNodeToFirstNodeInCarMove = individual.getProblemInstance()
                        .getTravelTimeBike(previousNode,carMove.getFromNode());
                double timeOfArrival = currentTime + travelTimeFromPrevNodeToFirstNodeInCarMove;
                if(!previousNode.equals(carMove.getFromNode())){
                    operatorString += "(" +carMove.getFromNode().getNodeId() + ",0,0," + MathHelper.round(timeOfArrival,2) + "),";
                }
                currentTime += operator.getTravelTime(previousNode, carMove, currentTime);
                previousNode = carMove.getToNode();
                operatorString += "(" + carMove.getToNode().getNodeId() + ",0,1," + MathHelper.round(currentTime,2) + "),";
            }
            operatorString = operatorString.substring(0, operatorString.length() - 1) + "\n";
            writeString += operatorString;
        }
        FileHandler fileHandler = new FileHandler(FileConstants.OPERATOR_PATH_OUTPUT_FOLDER + fileName, false);
        fileHandler.writeFile(writeString);
    }

}

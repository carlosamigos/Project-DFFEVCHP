package unittests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import code.problem.ProblemInstance;
import code.solver.heuristics.entities.Operator;
import constants.Constants;
import constants.FileConstants;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;

class OperatorTest {

	Operator operator;
	
	@BeforeEach
	void setUp() throws Exception {
		ProblemInstance instance = new ProblemInstance(FileConstants.TEST_STATIC_FOLDER + "deltaFitnessTest");
		Node startNode = instance.getOperators().get(0).getNextOrCurrentNode();
		double startingTime = instance.getOperators().get(0).getTimeRemainingToCurrentNextNode();
		HashMap<ChargingNode, Integer> capacities = new HashMap<>();
		for (int i = 0; i < instance.getChargingNodes().size(); i++) {
			capacities.put(instance.getChargingNodes().get(i), instance.getChargingNodes().get(i).getNumberOfAvailableChargingSpotsNextPeriod());
		}
		System.out.println("#########");
		ArrayList<ArrayList<Double>> travelTimes = instance.getTravelTimesBike();
		for(ArrayList<Double> outer : travelTimes) {
			for(double time : outer) {
				System.out.print(time + " ");
			}
			System.out.println("");
		}
		ArrayList<ChargingNode> cNodes = instance.getChargingNodes();
		System.out.println(startNode.getNodeId());
		System.out.println(instance.getTravelTimeBike(startNode, startNode));
		operator =  new Operator(startingTime, Constants.TIME_LIMIT_STATIC_PROBLEM, startNode, instance, capacities);
	}

	@Test
	void testGetDeltaFitnessInsertProblemInstance() {
		assertEquals(10,10);
	}

	@Test
	void testGetDeltaFitnessRemoveProblemInstance() {
		assertEquals(10.0, 11.0);
	}

}

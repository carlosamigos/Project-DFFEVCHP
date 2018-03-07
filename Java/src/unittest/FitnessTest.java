package unittest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import constants.HeuristicsConstants;

class FitnessTest {

	
	static Operator operator;
	
	static ParkingNode pNode1 = new ParkingNode(1);
	static ParkingNode pNode2 = new ParkingNode(2);
	static ChargingNode cNode = new ChargingNode(3);
	static HashMap<ChargingNode, Integer> capacities = new HashMap<>();
	
	private static double startTime = 0.0;
	
	static double[][] travelTimesBike = new double[][] {
		new double[] {0.0, 1.0, 1.0},
		new double[] {2.0, 0.0, 1.0},
		new double[] {1.0, 2.0, 0.0}
	};
	
	static ArrayList<ArrayList<Double>> travelTimes = new ArrayList<>();
	
	@BeforeEach
	void setUp() throws Exception {
		for(double[] times : travelTimesBike) {
			ArrayList<Double> insert = new ArrayList<>();
			for(double d : times) {
				insert.add(d);
			}
			travelTimes.add(insert);
		}
		
		cNode.setNumberOfAvailableChargingSpotsNextPeriod(1);
	}
	
	@DisplayName("Fitness calculation: Operator")
	@ParameterizedTest(name = "foo")
	@MethodSource(value = { "testOperatorFitness" })
	void testOperatorFitness(ArrayList<CarMove> carMoves, double expectedFitness, double endTime) {
		capacities.put(cNode, 0);
		operator = new Operator(startTime, endTime, pNode1, travelTimes, capacities, 0);
		operator.addCarMoves(carMoves);
		operator.calculateFitness();
		assertEquals(expectedFitness, operator.getFitness());
	}
	
	@SuppressWarnings({ "unused", "serial" })
	private static List<Arguments> testOperatorFitness() {
		
		ArrayList<CarMove> carMoves1 = new ArrayList<CarMove>() {{
			add(new CarMove(pNode1, pNode2, null, 1, 0));
			add(new CarMove(pNode2, cNode, null, 2, 0));
			add(new CarMove(pNode2, pNode1, null, 1, 0));
			add(new CarMove(pNode1, cNode, null, 1, 0));
		}};
		double endTime1 = 6.0;
		double fitness1 = - HeuristicsConstants.TABU_CHARGING_UNIT_REWARD * (endTime1 - 3.0);
		
		double endTime2 = 10.0;
		double fitness2 = (- HeuristicsConstants.TABU_CHARGING_UNIT_REWARD 
				* ((endTime2 - 3.0) + (endTime2 - 7.0)))
				+ HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
		return Arrays.asList(
				Arguments.of(carMoves1, fitness1, endTime1),
				Arguments.of(carMoves1, fitness2, endTime2));
	}
}

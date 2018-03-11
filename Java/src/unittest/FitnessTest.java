package unittest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.tabusearch.TSIndividual;
import constants.HeuristicsConstants;

class FitnessTest {

	
	private static Operator operator;
	private static ParkingNode pNode1 = new ParkingNode(1);
	private static ParkingNode pNode2 = new ParkingNode(2);
	private static ChargingNode cNode = new ChargingNode(3);
	private static HashMap<ChargingNode, Integer> capacities = new HashMap<>();
	private static TSIndividual individual =  new TSIndividual(capacities);
	
	private static double startTime = 0.0;
	
	@SuppressWarnings("serial")
	private static ArrayList<ArrayList<Double>> travelTimes = new ArrayList<ArrayList<Double>>() {{
		add(new ArrayList<Double>() {{
			add(0.0); add(1.0); add(1.0);
		}});
		add(new ArrayList<Double>() {{
			add(2.0); add(0.0); add(1.0);
		}});
		add(new ArrayList<Double>() {{
			add(1.0); add(2.0); add(0.0);
		}});
	}};
	
	@BeforeEach
	void setUp() throws Exception {
		cNode.setNumberOfAvailableChargingSpotsNextPeriod(1);
		
	}
	
	@DisplayName("Fitness calculation: Operator")
	@ParameterizedTest(name = "{0}")
	@MethodSource(value = { "testOperatorFitness" })
	void testOperatorFitness(String testName, ArrayList<CarMove> carMoves, double expectedFitness, double endTime) {
		capacities.put(cNode, 0);
		operator = new Operator(startTime, endTime, pNode1, travelTimes, 0, individual, null, null);
		operator.addCarMoves(carMoves);
		operator.getFitness();
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
				Arguments.of("Normal case", carMoves1, fitness1, endTime1),
				Arguments.of("Capacity broken", carMoves1, fitness2, endTime2));
	}
}

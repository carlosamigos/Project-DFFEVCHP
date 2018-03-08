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
import code.solver.heuristics.mutators.IntraMove;
import code.solver.heuristics.mutators.Mutation;
import constants.HeuristicsConstants;

class MutationTests {

	private static Operator operator;
	private static ParkingNode pNode1 = new ParkingNode(1);
	private static ParkingNode pNode2 = new ParkingNode(2);
	private static ChargingNode cNode = new ChargingNode(3);
	private static HashMap<ChargingNode, Integer> capacities = new HashMap<>();
	private static double startTime = 0.0;
	private static double endTime = 6.0;
	
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
		operator = new Operator(startTime, endTime, pNode1, travelTimes, 0, null);
	}

	@DisplayName("Mutation tests: Perform Intra")
	@ParameterizedTest(name = "{0}")
	@MethodSource(value = { "testOperatorFitness" })
	void testOperatorFitness(String testName, ArrayList<CarMove> carMoves, IntraMove intraMove) {
		operator.addCarMoves(carMoves);
		
		assertEquals(0.0, operator.getFitness());
	}
	
	@SuppressWarnings({ "unused", "serial" })
	private static List<Arguments> testOperatorFitness() {
		ArrayList<CarMove> carMoves =  new ArrayList<CarMove>() {{
			add(new CarMove(pNode1, pNode2, null, 1, 0));
			add(new CarMove(pNode2, cNode, null, 2, 0));
			add(new CarMove(pNode2, pNode1, null, 1, 0));
			add(new CarMove(pNode1, cNode, null, 1, 0));
			add(new CarMove(pNode1, pNode2, null, 1, 0));
		}};
		
		Mutation mutation1 = new IntraMove(operator, 0, 4);
		Mutation mutation2 = new IntraMove(operator, 4, 0);
		Mutation mutation3 = new IntraMove(operator, 1, 3);
		
		
		return Arrays.asList(
				Arguments.of("First to last", carMoves, mutation1),
				Arguments.of("Last to first", carMoves, mutation2),
				Arguments.of("Middle to middle", carMoves, mutation3));
	}

}

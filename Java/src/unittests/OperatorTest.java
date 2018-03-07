package unittests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.Insert;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;

public class OperatorTest {

	Operator operator;
	ChargingNode cNode;
	ParkingNode pNode;
	
	private static double startTime = 0.0;
	private static double endTime = 4.0;
	
	double[][] travelTimesBike = new double[][] {
		new double[] {0.0, 2.0},
		new double[] {2.0, 0.0}
	};
	
	@BeforeEach
	void setUp() throws Exception {
		ArrayList<ArrayList<Double>> travelTimes = new ArrayList<>();
		for(double[] times : travelTimesBike) {
			ArrayList<Double> insert = new ArrayList<>();
			for(double d : times) {
				insert.add(d);
			}
			travelTimes.add(insert);
		}
		
		cNode = new ChargingNode(2);
		cNode.setNumberOfAvailableChargingSpotsNextPeriod(2);
		HashMap<ChargingNode, Integer> capacities = new HashMap<>();
		capacities.put(cNode, 0);
		
		pNode = new ParkingNode(1);
		operator = new Operator(startTime, endTime, pNode, travelTimes, capacities);
		
		
	}

	/*
	 * The list of car moves consists of two car moves, both moves are from a parking node to a parking node
	 * A charging move is inserted. The move should be rewarded according to its end time. 
	 */
	@DisplayName("Delta fitness test: Insertion with non charging")
	@ParameterizedTest(name = "Insert at {0} should return fitness {1}")
	@CsvSource({
		"0, -2.0",
		"1, -1.0",
		"2,  0.0"
	})
	void insertDeltaWithoutOtherChargingMoves(int index, double expectedFitness) {
		CarMove c1 = new CarMove(pNode, pNode, null, 1, 0);
		CarMove c2 = new CarMove(pNode, pNode, null, 1, 0);
		operator.addCarMove(c1);
		operator.addCarMove(c2);
		
		CarMove insertMove = new CarMove(pNode, cNode, null, 2, 0);
		Insert insert = new Insert(index, insertMove);
		assertEquals(expectedFitness, operator.getDeltaFitness(insert));
	}
	
	/*
	 * The list of car moves consist of two car moves, both moves are from a parking node to a parking node.
	 * A parking move is inserted. Fitness change should be 0.
	 */
	@DisplayName("Delta fitness test: Insertion of parking move")
	@ParameterizedTest(name = "Insert {0} should return fitness {1}")
	@CsvSource({
		"0, 0.0",
		"1, 0.0",
		"2, 0.0"
	})
	void insertDeltaParkingMove(int index, double expectedFitness) {
		CarMove c1 = new CarMove(pNode, pNode, null, 1, 0);
		CarMove c2 = new CarMove(pNode, pNode, null, 1, 0);
		operator.addCarMove(c1);
		operator.addCarMove(c2);
		
		CarMove insertMove = new CarMove(pNode, pNode, null, 2, 0);
		Insert insert = new Insert(index, insertMove);
		assertEquals(expectedFitness, operator.getDeltaFitness(insert));
	}
	
	/*
	 * The list of car moves consist of two car moves, the first move is a charging move, the second a 
	 * parking move. A charging move is inserted at indexes 0,1 and 2. Non of the insertions will push
	 * the existing charging move out of the list of moves that the operator is able to perform.
	 */
	@DisplayName("Delta fitness test: Insertion with other charging moves")
	@ParameterizedTest(name = "Insert {0} should return fitness {1}")
	@CsvSource({
		"0, -2.0",
		"1, -2.0",
		"2, -1.0"
	})
	void insertDeltaWithOtherChargingMoves(int index, double expectedFitness) {
		CarMove c1 = new CarMove(pNode, cNode, null, 1, 0);
		CarMove c2 = new CarMove(pNode, pNode, null, 1, 0);
		operator.addCarMove(c1);
		operator.addCarMove(c2);
		
		CarMove insertMove = new CarMove(pNode, cNode, null, 1, 0);
		Insert insert = new Insert(index, insertMove);
	}
}

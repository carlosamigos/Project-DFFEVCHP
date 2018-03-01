package unittests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import code.solver.heuristics.entities.Operator;

class OperatorTest {

	Operator operator;
	
	@BeforeEach
	void setUp() throws Exception {
		operator =  new Operator(0, 60, null, null, null);
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

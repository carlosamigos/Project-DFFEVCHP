package tests;

import constants.Constants;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InputTester {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void nodeTest() {
		assertTrue(Constants.HORIZONTAL_NODES * Constants.VERTICAL_NODES == Constants.P_NODES);
	}

}

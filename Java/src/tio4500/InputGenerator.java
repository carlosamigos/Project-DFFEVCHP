package tio4500;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import constants.Constants;

public class InputGenerator {

	public void generateGeneralInfo() {
		String info = "";
		info += "exampleFile : "+ Integer.toString(Constants.EXAMPLE_NUMBER) + "\n";
		info += "numNodes : " + Integer.toString(Constants.N_NODES) + "\n";
		info += "numPNodes : " + Integer.toString(Constants.P_NODES) + "\n";
		info += "numCNodes : " + Integer.toString(Constants.C_NODES) + "\n";
		info += "numVisits : " + Integer.toString(Constants.VISITS) + "\n";
		info += "numOperators : " + Integer.toString(Constants.N_OPERATORS) + "\n";
		info += "cToP : " + Constants.C_TO_P + "\n";
		info += "timeLimit : " + Double.toString(Constants.TIME_LIMIT_STEP) + "\n";
		info += "timeLimitLastVisit : " + Double.toString(Constants.TIME_LIMIT_LAST_VISIT) + "\n";
		info += "hNodes : " + Integer.toString(Constants.HORIZONTAL_NODES) + "\n";
		info += "wNodes : " + Integer.toString(Constants.VERTICAL_NODES) + "\n";
		
		String filePath = Constants.MOSEL_FOLDER + Constants.GENERAL_INFO_FILE;
		try {
			Files.write(Paths.get(filePath), info.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error writing " + filePath);
		}
		System.out.println("Successfully wrote " + filePath);
	}
}

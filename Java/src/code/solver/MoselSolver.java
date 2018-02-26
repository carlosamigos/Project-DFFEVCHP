package code.solver;

import code.problem.ProblemInstance;
import constants.Constants;
import constants.FileConstants;

public class MoselSolver extends Solver {

	private final String moselFileName;
	private final String bimFileName;
	private final Constants.SolverType solverType = Constants.SolverType.MOSEL;

	//private XPRM mosel;
	//private XPRMModel model;

	public MoselSolver(String moselFileName) {
		//this.mosel = new XPRM();
		this.moselFileName = moselFileName;
		this.bimFileName = moselFileName.substring(0, moselFileName.length() - 3) + "bim";
	}

	@Override
	public void solve(ProblemInstance problemInstance) {
		compile();
		fixParameters(problemInstance.getFileName());
		fixDataFile(problemInstance.getFilePath());

		System.out.print("Solving " + problemInstance.getFilePath() + " ");
		//this.model.run();
		//this.model.reset();
	}

	private void fixDataFile(String dataFile) {
		String parameters = "DataFile=" + dataFile + ".txt";
		//this.model.execParams += parameters;
	}

	private void fixParameters(String fileName) {
		String parameters = "printParams=" + Constants.PRINT_MOSEL_PARAMETERS + "," + "printResults="
				+ Constants.PRINT_MOSEL_RESULTS + "," + "MaxSolveTimeSeconds=" + Constants.MAX_SOLVE_TIME_MOSEL_SECONDS
				+ "," + "OutputPathArtificial=" + FileConstants.MOSEL_OUTPUT_ARTIFICIAL + fileName+ ".txt," + "OutputPathRegular=" + FileConstants.MOSEL_OUTPUT_REAL
				+ fileName + ".txt,";

		//model.execParams = parameters;
	}

	private void compile() {
		try {
			//this.mosel.compile(FileConstants.PROBLEM_FOLDER + this.moselFileName);
			//this.model = this.mosel.loadModel(FileConstants.PROBLEM_FOLDER + this.bimFileName);
		}

		catch (Exception e) {
			System.out.println("Could not compile mosel file");
		}

	}

	public Constants.SolverType getSolverType(){
		return this.solverType;
	}

	@Override
	public String getInfo() {
		return this.moselFileName;
	}

}

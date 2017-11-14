package tio4500.solvers;

import java.util.HashMap;

import constants.Constants;
import tio4500.StaticProblem;

public class MoselSolver extends Solver {
	
	private final String moselFileName;
	private final String bimFileName;
	/*
	private XPRM mosel;
	private XPRMModel model = null;
	*/
	
	public MoselSolver(String moselFileName) {
		this.moselFileName = moselFileName;
		this.bimFileName = moselFileName.substring(0, moselFileName.length() - 3) + "bim";
	}
	
	@Override
	public void solve(StaticProblem problem) {
		compile();
		System.out.println("Starting to solve   " + this.bimFileName);
        //try{
            //this.model = this.mosel.loadModel(Constants.PROBLEM_FOLDER + this.bimFileName);
            fixParameters(problem.getFilePath());
            //model.run();
            System.out.println("Done solving        " + this.bimFileName);
            setResults(problem.getFilePath());
        //}
        //catch (IOException e){
        //    System.out.println("Could not load mosel bim file"); }
    }
		
	
	private void fixParameters(String dataFilePath) {
		String parameters = "DataFile=" + dataFilePath + ".txt," +
		  "printParams=" + Constants.PRINT_MOSEL_PARAMETERS + "," +
		  "printResults=" + Constants.PRINT_MOSEL_RESULTS + "," +
		  "MaxSolveTimeSeconds=" + Constants.MAX_SOLVE_TIME_MOSEL_SECONDS + "," +
		  "OutputPathArtificial=" + Constants.MOSEL_OUTPUT + Constants.OUTPUT_ARTIFICIAL_SERVICE_PATHS 
		  + Constants.EXAMPLE_NUMBER + ".txt," +
		  "OutputPathRegular=" + Constants.MOSEL_OUTPUT + Constants.OUTPUT_REAL_SERVICE_PATHS 
		  + Constants.EXAMPLE_NUMBER + ".txt";
		
		//model.execParams = parameters;
	}
	
	private void compile() {
		System.out.println("Starting to compile " + this.moselFileName);

        //try{
        //    this.mosel.compile(Constants.PROBLEM_FOLDER + this.moselFileName);
            System.out.println("Done compiling      " + this.moselFileName);
        //}

        /*catch (XPRMCompileException e){
            System.out.println("Could not compile mosel file");}
            */
	}
	
	private void setResults(String dataFilePath) {
		this.results = new HashMap<String, String>();
		this.results.put("Name", dataFilePath);
		this.results.put("BestSolution", "" /*+ mosel.getMIPObjVal()*/);
		this.results.put("BestBound", "" /*+ mosel.getBestBound()*/);
		this.results.put("Gap", "");
		
	}

	@Override
	public HashMap<String,String> getResults() {
		return this.results;
	}
	
	@Override
	public String getInfo() {
		return this.moselFileName;
	}

}

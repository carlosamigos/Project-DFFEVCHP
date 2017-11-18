package tests;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.DynamicProblem;
import tio4500.KPITrackerDynamic;
import tio4500.ProblemInstance;
import tio4500.SimulationModel;
import tio4500.solvers.Solver;
import utils.StringUtils;

public class DynamicTestSuite extends TestSuite{
	private final int days;
	private HashMap<String, ArrayList<KPITrackerDynamic>> kpiTrackers;
	
	public DynamicTestSuite(SolverType solverType, int days) {
		super(solverType, Constants.TEST_DYNAMIC_INITIAL_FOLDER, Constants.DYNAMIC_TEST_SUITE_RESULTS_FILE);
		this.days = days;
		
	}

	
	
	public void runTestSuite() {
		System.out.println("Starting dynamic test suite...");
		System.out.println("Number of test files, days, and models: " + testFileNames.size() + ", " + this.days + ", " + this.solvers.size() + "\n");
		int runsLeft = testFileNames.size() * days * this.solvers.size();
		double timePerRun = calcTimePerRun();

		printEstimatedTimeLeft(timePerRun, runsLeft);

		for(String test : testFileNames) {
			
			System.out.println("Solving " + test);
			ProblemInstance cleanProblemInstance = new ProblemInstance(test);
			this.writeTestHeader(test);
			this.kpiTrackers =  new HashMap<>();
			
			for (int day = 0; day < days; day++) {
				
				// Simulate demand requests during day
				SimulationModel simModel = new SimulationModel(day, cleanProblemInstance);
				simModel.createNewDaySimulationModel(); // I konstruktøren til SimulationModel?
				simModel.saveDaySimulationModel();
				//Solve for each solver
				
				for(Solver solver : this.solvers) {
					System.out.println("Using " + solver.getInfo() + " on day " + day);
					// Read clean demand requests
					ProblemInstance problemInstance = new ProblemInstance(test);
					SimulationModel solverSimulationModel = new SimulationModel(day, problemInstance);
					solverSimulationModel.readSimulationModelFromFile(); // Burde vært i konstruktøren til SimulationModel?
					DynamicProblem problem = new DynamicProblem(problemInstance, solverSimulationModel, solver, test);
					problem.solve();
					
					// Results:
					KPITrackerDynamic tracker = problem.getKpiTrackerDyanmic();
					addKPITracker(solver.getInfo(), tracker);
					runsLeft--;
				}
				printEstimatedTimeLeft(timePerRun, runsLeft);

			}
			System.out.println("");
			writeKPIs();
		}
		
		System.out.println("\nDone with all tests. See the file " + Constants.DYNAMIC_TEST_SUITE_RESULTS_FILE + " for results.");
	}
	
	private void addKPITracker(String solverName, KPITrackerDynamic tracker) {
		if(!this.kpiTrackers.containsKey(solverName)) {
			this.kpiTrackers.put(solverName, new ArrayList<>());	
		}
		this.kpiTrackers.get(solverName).add(tracker);
	}
	
	private void writeKPIs() {
		String solv = StringUtils.center("Model", 60);
		String dns = StringUtils.center("DNS (customers)", 19);
		String abandoned = StringUtils.center("Abondoned (op)", 18);
		String charged = StringUtils.center("Charged (cars)", 18);
		String carDist = StringUtils.center("CarDist (min)", 17);
		String bikeDist = StringUtils.center("BikeDist (min)", 18);
		String elDist = StringUtils.center("EL-Used (%)", 15);
		String chargeWait = StringUtils.center("ChargeWait (min)", 19);
		String idleTime = StringUtils.center("IdleTime (min)", 17);
		String data = solv + "|" + dns + "|" + abandoned + "|" + charged +
		              "|" + carDist + "|" + bikeDist + "|" + elDist + "|" + chargeWait +
		              "|" + idleTime + "\n";
		
		DecimalFormat df = 	new DecimalFormat("#.##");
		for(String solver : this.kpiTrackers.keySet()) {
			ArrayList<KPITrackerDynamic> trackers = this.kpiTrackers.get(solver);
			String solverName = StringUtils.center(solver, 60);
			String dnsVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getDemandsNotServed().stream().collect(Collectors.summingInt(Integer::intValue)))
				.collect(Collectors.summingInt(Integer::intValue))/this.days), 19);
			
			String abandonedVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getNumberOfOperatorsAbandoned().stream().collect(Collectors.summingInt(Integer::intValue)))
				.collect(Collectors.summingInt(Integer::intValue))/this.days), 18);
			
			String chargedVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getNumberOfCarsSetToCharging().stream().collect(Collectors.summingInt(Integer::intValue)))
				.collect(Collectors.summingInt(Integer::intValue))/this.days), 18);
			
			String carDistVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getTotalCarTravelDoneByServiceOperators()).collect(Collectors.summingDouble(Double::doubleValue))/this.days)
				, 17);
			
			String bikeDistVal = StringUtils.center("" + df.format(trackers.stream().map(t ->
				t.getTotalBikeTravelDoneByServiceOperators()).collect(Collectors.summingDouble(Double::doubleValue))/this.days)
				, 18);
			
			String elDistVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getTotalBikeTravelDoneByServiceOperators()).collect(Collectors.summingDouble(Double::doubleValue))/this.days)
				, 15);
			
			String chargeWaitVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getWaitingTimeBeforeCarInNeedAreCharged().stream().collect(Collectors.summingDouble(Double::doubleValue)))
				.collect(Collectors.summingDouble(Double::doubleValue))/this.days), 19);
			
			String idleTimeVal = StringUtils.center("" + df.format(trackers.stream().map(t -> 
				t.getIdleTimeForServiceOperators().stream().collect(Collectors.summingDouble(Double::doubleValue)))
				.collect(Collectors.summingDouble(Double::doubleValue))/this.days), 17);
			
			data += solverName + "|" + dnsVal + "|" + abandonedVal + "|" + chargedVal + "|" + carDistVal
					+ "|" + bikeDistVal + "|" + elDistVal + "|" + chargeWaitVal + "|" + idleTimeVal + "\n";
		}
		fh.writeFile(data);
	}

	
	
	private void writeTestHeader(String testName) {
		String data = "\nTest " + testName + "\n";
		fh.writeFile(data);
	}
	
	@Override
	protected double calcTimePerRun() {
		double timePerRun = ((Constants.END_TIME - 2*Constants.TIME_LIMIT_STATIC_PROBLEM + Constants.TIME_INCREMENTS)
				- Constants.START_TIME)/60;
		timePerRun *= Constants.MAX_SOLVE_TIME_MOSEL_SECONDS;
		
		return timePerRun;
	}
	
	

}

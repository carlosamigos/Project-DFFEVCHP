package tests;

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
		super(solverType, Constants.TEST_FOLDER, Constants.DYNAMIC_TEST_SUITE_RESULTS_FILE);
		this.days = days;
		
	}
	
	public void runTestSuite() {
		System.out.println("Starting dynamic test suite...");
		System.out.println("Number of test files: " + testFileNames.size() + "\n");
		double estimatedTime = (Constants.END_TIME - Constants.START_TIME)/60 - 1;
		estimatedTime *= (60/Constants.TIME_INCREMENTS);
		estimatedTime *= (Constants.MAX_SOLVE_TIME_MOSEL_SECONDS/3600);
		estimatedTime *= this.testFileNames.size()*this.solvers.size();
		System.out.println("Estimated running time (hours): " + estimatedTime);
		
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
					DynamicProblem problem = new DynamicProblem(problemInstance, solverSimulationModel, solver);
					problem.solve();
					
					// Results:
					KPITrackerDynamic tracker = problem.getKpiTrackerDyanmic();
					addKPITracker(solver.getInfo(), tracker);
				}
				System.out.println("\n");
			}
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
		String solv = StringUtils.center("Model", 20);
		String dns = StringUtils.center("DNS (customers)", 15);
		String abandoned = StringUtils.center("Abondoned (op)", 15);
		String charged = StringUtils.center("Charged (cars)", 15);
		String carDist = StringUtils.center("CarDist (min)", 15);
		String bikeDist = StringUtils.center("BikeDist (min)", 15);
		String elDist = StringUtils.center("EL-Used (%)", 10);
		String chargeWait = StringUtils.center("ChargeWait (min)", 15);
		String idleTime = StringUtils.center("IdleTime (min)", 15);
		String data = solv + "|" + dns + "|" + abandoned + "|" + charged +
		              "|" + carDist + "|" + bikeDist + "|" + elDist + "|" + chargeWait +
		              "|" + idleTime + "\n";
		
		for(String solver : this.kpiTrackers.keySet()) {
			ArrayList<KPITrackerDynamic> trackers = this.kpiTrackers.get(solver);
			String solverName = StringUtils.center(solver, 20);
			String dnsVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getDemandsNotServed().stream().collect(Collectors.summingInt(Integer::intValue)))
				.collect(Collectors.summingInt(Integer::intValue))/this.days, 15);
			
			String abandonedVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getNumberOfOperatorsAbandoned().stream().collect(Collectors.summingInt(Integer::intValue)))
				.collect(Collectors.summingInt(Integer::intValue))/this.days, 15);
			
			String chargedVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getNumberOfCarsSetToCharging().stream().collect(Collectors.summingInt(Integer::intValue)))
				.collect(Collectors.summingInt(Integer::intValue))/this.days, 15);
			
			String carDistVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getTotalCarTravelDoneByServiceOperators()).collect(Collectors.summingDouble(Double::doubleValue))/this.days
				, 15);
			
			String bikeDistVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getTotalBikeTravelDoneByServiceOperators()).collect(Collectors.summingDouble(Double::doubleValue))/this.days
				, 15);
			
			String elDistVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getTotalBikeTravelDoneByServiceOperators()).collect(Collectors.summingDouble(Double::doubleValue))/this.days
				, 15);
			
			String chargeWaitVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getWaitingTimeBeforeCarInNeedAreCharged().stream().collect(Collectors.summingDouble(Double::doubleValue)))
				.collect(Collectors.summingDouble(Double::doubleValue))/this.days, 15);
			
			String idleTimeVal = StringUtils.center("" + trackers.stream().map(t -> 
				t.getIdleTimeForServiceOperators().stream().collect(Collectors.summingDouble(Double::doubleValue)))
				.collect(Collectors.summingDouble(Double::doubleValue))/this.days, 15);
			
			data += solverName + "|" + dnsVal + "|" + abandonedVal + "|" + chargedVal + "|" + carDistVal
					+ "|" + bikeDistVal + "|" + elDistVal + "|" + chargeWaitVal + "|" + idleTimeVal + "\n";
		}
		fh.writeFile(data);
	}

	
	
	private void writeDayHeader(int day) {
		String header = "Day " + day + "\n";
		fh.writeFile(header);
	}
	
	private void writeTestHeader(String testName) {
		String data = "Test " + testName + "\n";
		fh.writeFile(data);
	}
	
	private void writeTestResult(KPITrackerDynamic tracker) {
		System.out.println(tracker);
		/*
		String[] namePath = tracker.getName().split("/");
		String data = "\n" + StringUtils.center(namePath[namePath.length - 1], 30);
		data += "|";
		data += StringUtils.center(tracker.getTimeUsed() + "s.", 10);
		data += "|";
		data += StringUtils.center(tracker.getBestSolution(),10);
		data += "|";
		data += StringUtils.center(tracker.getGap() + "%", 10);
		
		fh.writeFile(data);
		*/
	}
}

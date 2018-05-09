package tests;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import code.DynamicProblem;
import code.kpitracker.KPITrackerDynamic;
import code.kpitracker.KPITrackerStatic;
import code.problem.ProblemInstance;
import code.simulation.SimulationModel;
import code.solver.Solver;
import constants.Constants;
import constants.Constants.SolverType;
import constants.FileConstants;
import constants.HeuristicsConstants;
import utils.FileHandler;
import utils.StringUtils;

public class DynamicTestSuite extends TestSuite{
	private final int days;
	private HashMap<Solver, ArrayList<KPITrackerDynamic>> kpiTrackers;
	private FileHandler staticKPIfh;
	
	public DynamicTestSuite(SolverType solverType, int days) {
		super(solverType, FileConstants.TEST_DYNAMIC_INITIAL_FOLDER, FileConstants.DYNAMIC_TEST_SUITE_RESULTS_FILE);
		this.days = days;
	}

	
	public void runTestSuite() {
		System.out.println("Starting dynamic test suite... (folder: " + FileConstants.TEST_DYNAMIC_INITIAL_FOLDER + ")");
		System.out.println("Number of test files, days, and models: " + testFileNames.size() + ", " + this.days + ", " + this.solvers.size() + "\n");
		int runsLeft = testFileNames.size() * days * this.solvers.size();
		double timePerRun = calcTimePerRun();
		writeTestHeader();
		for(String test : testFileNames) {
			
			this.staticKPIfh = new FileHandler(FileConstants.DYNAMIC_SINGLE_TEST_RESULTS_FILE + test + "_" + getTimestamp(), true, true);
			System.out.println("Solving " + test);
			printEstimatedTimeLeft(timePerRun, runsLeft, test);
			ProblemInstance cleanProblemInstance = new ProblemInstance(FileConstants.TEST_DYNAMIC_INITIAL_FOLDER + test);
			this.kpiTrackers =  new HashMap<>();
			
			for (int day = 0; day < days; day++) {
				
				// Simulate demand requests during day
				SimulationModel simModel = new SimulationModel(day, cleanProblemInstance);
				simModel.createNewDaySimulationModel();
				simModel.saveDaySimulationModel();
				//Solve for each solver
				
				for(Solver solver : this.solvers) {
					System.out.println("Using " + solver.getInfo() + " on day " + day);
					// Read clean demand requests
					ProblemInstance problemInstance = new ProblemInstance(FileConstants.TEST_DYNAMIC_INITIAL_FOLDER  + test);
					SimulationModel solverSimulationModel = new SimulationModel(day, problemInstance);
					solverSimulationModel.readSimulationModelFromFile();
					DynamicProblem problem = new DynamicProblem(problemInstance, solverSimulationModel, solver, test);
					problem.solve();
					
					// Results:
					KPITrackerDynamic tracker = problem.getKpiTrackerDyanmic();
					addKPITracker(solver, tracker);
					runsLeft--;
				}

			}
			System.out.println("");
			writeKPIs(test);
		}
		
		System.out.println("\nDone with all tests. See the file " + FileConstants.DYNAMIC_TEST_SUITE_RESULTS_FILE + this.timeStamp + " for results.");
	}
	
	private void addKPITracker(Solver solver, KPITrackerDynamic tracker) {
		if(!this.kpiTrackers.containsKey(solver)) {
			this.kpiTrackers.put(solver, new ArrayList<>());	
		}
		this.kpiTrackers.get(solver).add(tracker);
	}
	
	private void writeKPIs(String test) {
		String data = "";
		
		DecimalFormat df = 	new DecimalFormat("#.##");
		for(Solver solver : this.kpiTrackers.keySet()) {
			ArrayList<KPITrackerDynamic> trackers = this.kpiTrackers.get(solver);
			String testName = StringUtils.center(test, 80);
			Double expectedValueCalculated = trackers.stream().map(t ->
					t.calculateDemandServedFraction()*100)
					.collect(Collectors.summingDouble(Double::doubleValue))/this.days;
			String dsPercentage = StringUtils.center("" + df.format(expectedValueCalculated), 19);

			Double squaredSum = 0.0;
			for (KPITrackerDynamic tracker: trackers) {
				double demandServedFraction = tracker.calculateDemandServedFraction();
				squaredSum += Math.pow(demandServedFraction - expectedValueCalculated/100,2);
			}
			
			String dsVarianceVal;
			if(trackers.size()-1 == 0) {
				dsVarianceVal = StringUtils.center("NA", 19);
			} else {
				Double variance = squaredSum /(trackers.size() -1);
				dsVarianceVal = StringUtils.center("" + df.format(variance).toString(), 19);
			}

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
			
			data += testName + "|" + dsPercentage + "|" + dsVarianceVal +  "|" + abandonedVal + "|" + chargedVal + "|" + carDistVal
					+ "|" + bikeDistVal + "|" + elDistVal + "|" + chargeWaitVal + "|" + idleTimeVal + "\n";
		}
		fh.writeFile(data);
		
		writeStaticKPIs();
	}

	
	private void writeStaticKPIs() {
		for(Solver solver : kpiTrackers.keySet()) {
			ArrayList<KPITrackerDynamic> dynamicTrackers = kpiTrackers.get(solver);
			for(int day = 0; day < this.days; day++) {
				this.staticKPIfh.writeFile("Day " + day + "\n");
				writeTestHeaderStatic();
				for(KPITrackerStatic staticTracker : dynamicTrackers.get(day).getStaticKPITrackers()) {
					writeTestResult(staticTracker);
				}
			}
		}
	}
	
	
	private void writeTestHeader() {
		String solv = StringUtils.center("Test", 80);
		String ds = StringUtils.center("DS (%)", 19);
		String dsVariance = StringUtils.center("DS var", 19);
		String abandoned = StringUtils.center("Abondoned (op)", 18);
		String charged = StringUtils.center("Charged (cars)", 18);
		String carDist = StringUtils.center("CarDist (min)", 17);
		String bikeDist = StringUtils.center("BikeDist (min)", 18);
		String elDist = StringUtils.center("EL-Used (%)", 15);
		String chargeWait = StringUtils.center("ChargeWait (min)", 19);
		String idleTime = StringUtils.center("IdleTime (min)", 17);
		String data = solv + "|" + ds + "|" +dsVariance + "|" + abandoned + "|" + charged +
		              "|" + carDist + "|" + bikeDist + "|" + elDist + "|" + chargeWait +
		              "|" + idleTime + "\n";
		fh.writeFile(data);
	}
	
	private void writeTestResult(KPITrackerStatic tracker) {
		String[] namePath = tracker.getName().split("/");
		String data = "\n" + StringUtils.center(namePath[namePath.length - 1], 60);
		data += "|";
		data += StringUtils.center(tracker.getTimeUsed(), 10);
		data += "|";
		data += StringUtils.center(tracker.getBestSolution(),10);
		data += "|";
		data += StringUtils.center(tracker.getGap(), 10);
		data += "|";
		data += StringUtils.center(tracker.getRdev(), 10);
		data += "|";
		data += StringUtils.center(tracker.getCdev(), 10);
		
		staticKPIfh.writeFile(data);
	}
	
	private void writeTestHeaderStatic() {
		String name =  StringUtils.center("Test",  60);
		String time =  StringUtils.center("Time",  10);
		String value = StringUtils.center("Value", 10);
		String gap =   StringUtils.center("Gap",   10);
		String rdev =  StringUtils.center("rDev",  10);
		String cdev =  StringUtils.center("cDev",  10);
		String headerLine = name + "|" + time + "|" + value + "|" + gap + "|" + rdev + "|" + cdev;
		staticKPIfh.writeFile(headerLine);

	}
	
	@Override
	protected double calcTimePerRun() {
		int maxTime;
		switch(this.solverType) {
		case MOSEL:
			maxTime = Constants.MAX_SOLVE_TIME_MOSEL_SECONDS;
			break;
		case ALNS:
			maxTime = HeuristicsConstants.ALNS_MAX_TIME_SECONDS;
			break;
		default:
			maxTime = Constants.MAX_SOLVE_TIME_MOSEL_SECONDS;
		}
		
		double numberOfSubProblems = ((Constants.END_TIME - 2*Constants.TIME_LIMIT_STATIC_PROBLEM + Constants.TIME_INCREMENTS)
				- Constants.START_TIME)/Constants.TIME_INCREMENTS;
		double timePerRun = numberOfSubProblems * maxTime;
		return timePerRun;
	}
	
	

}

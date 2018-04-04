package code.kpitracker;

import java.util.ArrayList;
import code.solver.Solver;

public class KPITrackerStatic {
	
	
	private final String name;
	private String bestSolution;
	private String bestBound;
	private String gap;
	private String timeUsed;
	private String rdev;
	private String cdev;
	
    public KPITrackerStatic(String name) {
		this.name = name;
	}

	public void setResults(Solver solver){
        ArrayList<String> results = solver.getResults();
        this.bestSolution = results.get(0);
        this.bestBound = results.get(1);
        this.gap = results.get(2);
        this.timeUsed = results.get(3) + "s.";
        this.rdev = results.get(4);
        this.cdev = results.get(5);
    }
    
    public String getName() {
    		return name;
    }
    
    public String getBestSolution() {
		return bestSolution;
	}

	public String getBestBound() {
		return bestBound;
	}

	public String getGap() {
		return gap;
	}

	public String getTimeUsed() {
		return timeUsed;
	}

	public String getRdev() {
		return rdev;
	}

	public String getCdev() {
		return cdev;
	}
}

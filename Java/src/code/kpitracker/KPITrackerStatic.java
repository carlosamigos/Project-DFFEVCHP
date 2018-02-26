package code.kpitracker;

import constants.FileConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.stream.Stream;

public class KPITrackerStatic {

    private String name;
    private String bestSolution;
    private String bestBound;
    private String timeUsed;
    private String gap;
    private String rdev;
    private String cdev;

    public void setResults(String dataFilePath){
        ArrayList<String> currResults = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(Paths.get(FileConstants.STATIC_RUN_STATS))) {
            stream.forEach(result -> currResults.add(result));
        } catch (IOException e) {
            System.out.println("Could not find stats file from static run");
        }

        DecimalFormat df = 	new DecimalFormat("#.##");
        String gapString;
        String bestSolution;
        if(currResults.get(0).contains("e")) {
            gapString = "N/A";
            bestSolution = "N/A";
        } else {
            double gap = (Double.parseDouble(currResults.get(0)) - Double.parseDouble(currResults.get(1)))/
                    Double.parseDouble(currResults.get(0)) *100;
            gapString = df.format(gap);
            bestSolution = df.format(Double.parseDouble(currResults.get(0)));
        }

        String timeString = df.format(Double.parseDouble(currResults.get(2)));
        this.name = dataFilePath;
        this.bestSolution = bestSolution;
        this.bestBound = currResults.get(1);
        this.timeUsed = timeString;
        this.gap = gapString;
        //this.rdev = df.format(Double.parseDouble(currResults.get(3)));
        //this.cdev = df.format(Double.parseDouble(currResults.get(4)));
    }

    public String getName() {
        return name;
    }

    public String getBestSolution() {
        return bestSolution;
    }

    public String getTimeUsed() {
        return timeUsed;
    }

    public String getGap() {
        return gap;
    }
    
    public String getRDev() {
    	return this.rdev;
    }
    
    public String getCDev() {
    	return this.cdev;
    }
    
    public String getBestBound() {
    	return this.bestBound;
    }
}

package tio4500;

/**
 * TODO: UNCOMMENT ALL LINES WHEN RUNNING ON MACHINE WITH XPRESS INSTALLED
 */


import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import constants.Constants;
import com.dashoptimization.*;

import java.io.IOException;

public class StaticProblem {

    private XPRM mosel;
    private XPRMModel model = null;

    public StaticProblem() {
        this.mosel = new XPRM();
    }

    /**
     * Compiles the .mos file associated with this static subproblem to a .bim file
     * Documentation for the XPRM framework can be found here: http://homepages.ulb.ac.be/~bfortz/moselug.pdf
     */
    public void compile() {
        System.out.println("Starting to compile " + Constants.MOSEL_FILE);

        try{
            this.mosel.compile(Constants.PROBLEM_FOLDER + Constants.MOSEL_FILE);
            System.out.println("Done compiling      " + Constants.PROBLEM_FOLDER + Constants.MOSEL_FILE);
        }

        catch (XPRMCompileException e){
            System.out.println("Could not compile mosel file");}
    }

    /**
     * Solves this static subproblem
     */
    public void solve() {
        System.out.println("Starting to solve   " +Constants.MOSEL_BIM_FILE);
        try{
            this.model = this.mosel.loadModel(Constants.PROBLEM_FOLDER +Constants.MOSEL_BIM_FILE);
            fixParameters();
            model.run();
            System.out.println("Done solving        " + Constants.MOSEL_BIM_FILE);
        }
        catch (IOException e){
            System.out.println("Could not load mosel bim file"); }
    }

    public void fixParameters(){
        String divisionChar = ",";
        String setParametersString = "";
        setParametersString += "DataFile=" + Constants.STATE_FOLDER_FILE + Constants.EXAMPLE_NUMBER + ".txt" + divisionChar;
        setParametersString += "printParams=" + Constants.PRINT_MOSEL_RESULTS + divisionChar;
        setParametersString += "printResults=" + Constants.PRINT_MOSEL_RESULTS + divisionChar;
        setParametersString += "MaxSolveTimeSeconds="+ Constants.MAX_SOLVE_TIME_MOSEL_SECONDS + divisionChar;
        setParametersString += "OutputPathArtificial="+Constants.MOSEL_OUTPUT + Constants.OUTPUT_ARTIFICIAL_SERVICE_PATHS + Constants.EXAMPLE_NUMBER + ".txt" +divisionChar;
        setParametersString += "OutputPathRegular="+Constants.MOSEL_OUTPUT + Constants.OUTPUT_REAL_SERVICE_PATHS + Constants.EXAMPLE_NUMBER + ".txt";
        model.execParams = setParametersString;

    }

    public XPRM getMosel(){ return this.mosel; }

    public XPRMModel getModel(){ return this.model; }
}

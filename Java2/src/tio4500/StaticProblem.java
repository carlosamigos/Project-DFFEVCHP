package tio4500;

/**
 * TODO: UNCOMMENT ALL LINES WHEN RUNNING ON MACHINE WITH XPRESS INSTALLED
 */


import constants.Constants;
//import com.dashoptimization.*;

public class StaticProblem {

    //XPRM mosel;
    static String moselFile;
    static String bimFile;
    static int problemNo;
    static int subproblemNo;

    /**
     * Creates a static subproblem.
     * @param problemNo: Dynamic problem number
     * @param subproblemNo: Static subproblem number
     */
    public StaticProblem(int problemNo, int subproblemNo) {
        this.problemNo = problemNo;
        this.subproblemNo = subproblemNo;
        this.moselFile = Constants.PROBLEM_FOLDER +  Integer.toString(problemNo) + "/" + Integer.toString(subproblemNo) + ".mos";
    }

    /**
     * Compiles the .mos file associated with this static subproblem to a .bim file
     * Documentation for the XPRM framework can be found here: http://homepages.ulb.ac.be/~bfortz/moselug.pdf
     */
    public void compile() {
        System.out.println("Starting to compile " + this.moselFile);
        //this.mosel.compile(this.moselFile);
        System.out.println("Done compiling      " + this.moselFile);
        this.bimFile = Constants.PROBLEM_FOLDER + Integer.toString(this.problemNo) + "/" + Integer.toString(subproblemNo) + ".mos";
    }

    /**
     * Solves this static subproblem
     */
    public void solve() {
        System.out.println("Starting to solve   " + this.bimFile);
        //this.mosel.loadModel(this.bimFile);
        System.out.println("Done solving        " + this.bimFile);
    }
}

package tio4500;



public class Main {

    public static void main(String[] args) {
	    System.out.println("Hello, world!");
	    RollingHorizon rolling = new RollingHorizon();
	    rolling.run();
	    
	    // Generates general_info.txt in the Mosel folder based on information in Constants.java
	    InputGenerator input = new InputGenerator();
	    input.generateGeneralInfo();
    }
}

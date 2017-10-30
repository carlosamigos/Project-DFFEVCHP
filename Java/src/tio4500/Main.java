package tio4500;



public class Main {

    public static void main(String[] args) {
	    System.out.println("Hello, rolling horizon!");
	    
	    DynamicProblem dynamicProblem = new DynamicProblem(3);
	    dynamicProblem.solve();
    }
    
    public static void generateGeneralInfo() {
    	InputGenerator input = new InputGenerator();
	    input.generateGeneralInfo();
    }
}

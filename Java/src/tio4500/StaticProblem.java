package tio4500;

public class StaticProblem {

    private final String filePath;
    private final String fileName;


    public StaticProblem(String filePath) {
        this.filePath = filePath;
        String[] split = filePath.split("/");
        this.fileName = split[split.length-1];
    }

    public String getFileName(){
        return this.fileName;
    }

    public String getFilePath() {
    	return this.filePath;
    }

}

package code;

public class StaticProblemFile {

    private final String filePath;
    private final String fileName;


    public StaticProblemFile(String filePath) {
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

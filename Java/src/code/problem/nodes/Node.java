package code.problem.nodes;

public abstract class Node {

    private final int nodeId;
    private int xCord;

    public void setxCord(int xCord) {
        this.xCord = xCord;
    }

    public void setyCord(int yCord) {
        this.yCord = yCord;
    }

    private int yCord;

    public Node(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }


    public int getxCord() {
        return xCord;
    }

    public int getyCord() {
        return yCord;
    }
}

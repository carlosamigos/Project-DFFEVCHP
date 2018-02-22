package code.problem.nodes;

public abstract class Node {

    private final int nodeId;

    public Node(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

}

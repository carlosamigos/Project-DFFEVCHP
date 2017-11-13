package tio4500.simulations.Nodes;

public abstract class Node {

    private final int nodeId;

    public Node(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

}

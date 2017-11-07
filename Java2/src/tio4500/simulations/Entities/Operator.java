package tio4500.simulations.Entities;

import tio4500.simulations.Nodes.Node;

public class Operator {

    private final int id;
    private Node nextOrCurrentNode = null;
    private Node previousNode = null;
    private double timeRemainingToCurrentNextNode = 0;


    public Operator(int id) {
        this.id = id;
    }

    public void setNextOrCurrentNode(Node nextOrCurrentNode) {
        this.nextOrCurrentNode = nextOrCurrentNode;
        if(previousNode == null){
            previousNode = nextOrCurrentNode;
        }
    }

    public int getId() {
        return id;
    }

    public Node getNextOrCurrentNode() {
        return nextOrCurrentNode;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

    public double getTimeRemainingToCurrentNextNode() {
        return timeRemainingToCurrentNextNode;
    }

    public void setTimeRemainingToCurrentNextNode(double timeRemainingToCurrentNextNode) {
        this.timeRemainingToCurrentNextNode = timeRemainingToCurrentNextNode;
    }

    @Override
    public String toString() {
        return "Operator{" +
                "id=" + id +
                ", node=" + nextOrCurrentNode +
                '}';
    }
}

package tio4500.simulations.Entities;

import tio4500.simulations.Nodes.Node;

public class Operator {

    private final int id;
    private Node currentNode = null;

    public Operator(int id) {
        this.id = id;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public int getId() {
        return id;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    @Override
    public String toString() {
        return "Operator{" +
                "id=" + id +
                ", node=" + currentNode +
                '}';
    }
}

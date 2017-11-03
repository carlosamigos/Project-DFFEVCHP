package tio4500.simulations.DemandRequest;

import tio4500.simulations.Nodes.Node;
import tio4500.simulations.Nodes.ParkingNode;

public class DemandRequest {

    private ParkingNode node;
    private double time;

    public DemandRequest(ParkingNode node, double time) {
        this.node = node;
        this.time = time;
    }

    public ParkingNode getNode() {
        return node;
    }

    public double getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "DemandRequest{" +
                "node=" + node +
                ", time=" + time +
                '}';
    }
}

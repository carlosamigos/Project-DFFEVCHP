package tio4500.simulations;

import tio4500.simulations.Nodes.ParkingNode;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DemandRequest implements Comparable{

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
        NumberFormat formatter = new DecimalFormat("#0.00");
        return "DemandReq{" +
                "node=" + node +
                ", t=" + formatter.format(time) +
                '}';
    }


    @Override
    public int compareTo(Object o) {
        if (((DemandRequest)o).getTime() < this.time){
            return 1;
        } else if (((DemandRequest)o).getTime() > this.time){
            return -1;
        } else {
            return 0;
        }
    }
}

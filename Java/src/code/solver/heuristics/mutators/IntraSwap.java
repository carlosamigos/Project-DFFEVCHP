package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.Operator;

public class IntraSwap extends Mutation{

    public final static int id = 3;

    private final int index1;
    private final int index2;
    private final Operator operator1;
    private final int hashCode;

    public IntraSwap(int index1, int index2, Operator operator1) {
        this.index1 = index1;
        this.index2 = index2;
        this.operator1 = operator1;
        String hashString = id + operator1.id +  ((index1 <= index2) ? ("" + index1 + index2) : ("" + index2 + index1)) ;
        long conv = Long.valueOf(hashString);
        conv = conv % 105943;
        this.hashCode = (int) conv;

    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  IntraSwap){
            return ((IntraSwap) o).hashCode() == this.hashCode;
        } return false;
    }

    public int getIndex1() {
        return index1;
    }

    public int getIndex2() {
        return index2;
    }

    public Operator getOperator1() {
        return operator1;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "[(Operator 1: " + operator1 + ", Index: " + index1 + "), (Operator 1: " + operator1 + ", Index: " + index2 + ")]";
    }

}


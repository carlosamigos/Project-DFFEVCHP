package code.solver.heuristics.mutators;

import code.solver.heuristics.Individual;

public class Remove extends Mutation{

    public final static int id = 4;

    private final int index;
    private final int hashType = 4;
    private final int hashCode;

    public Remove(int index) {
        this.index = index;
        String hashString = hashType + "" + index;
        hashCode = Integer.parseInt(hashString);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  Remove){
            return o.hashCode() == this.hashCode;
        } return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }


}

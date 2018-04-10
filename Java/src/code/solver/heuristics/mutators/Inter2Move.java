package code.solver.heuristics.mutators;


import code.solver.heuristics.entities.Operator;


public class Inter2Move extends Mutation {

    public final static int id = 5;

    private final Operator operatorRemove;
    private final Operator operatorInsert;
    private final int removeIndex1;
    private final int removeIndex2;
    private final int insertIndex;
    private final int hashCode;


    public Inter2Move(Operator operatorRemove, int removeIndex1, int removeIndex2, Operator operatorInsert, int insertIndex) {
        this.operatorRemove = operatorRemove;
        this.operatorInsert = operatorInsert;
        this.removeIndex1 = removeIndex1;
        this.removeIndex2 = removeIndex2;
        this.insertIndex = insertIndex;
        String hashString = id + "" + ((operatorInsert.id < operatorRemove.id) ? (operatorInsert.id + "" + operatorRemove.id) : (operatorRemove.id+ "" + operatorInsert.id))
                +((removeIndex1 <= insertIndex) ? (removeIndex1 +"" + insertIndex) : ( insertIndex +"" + removeIndex1));
        this.hashCode = Integer.parseInt(hashString);
    }


    public Operator getOperatorRemove() {
        return operatorRemove;
    }

    public Operator getOperatorInsert() {
        return operatorInsert;
    }

    public int getRemoveIndex1() {
        return removeIndex1;
    }

    public int getRemoveIndex2() {
        return removeIndex2;
    }

    public int getInsertIndex() {
        return insertIndex;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof InterMove){
            return ((InterMove) o).hashCode() == this.hashCode;
        }return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "[(Remove Operator: " + operatorRemove.id + ", Index: " + removeIndex1 + "), "
                + "(Insert Operator: " + operatorInsert.id + ", Index: " + insertIndex + ")]";
    }

}
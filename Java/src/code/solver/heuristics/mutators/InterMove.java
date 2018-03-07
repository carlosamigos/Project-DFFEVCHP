package code.solver.heuristics.mutators;

import code.problem.nodes.Node;
import code.solver.heuristics.entities.Operator;

import java.util.HashMap;

public class InterMove extends Mutation {

    public final static int id = 2;

    private final Operator operatorRemove;
    private final Operator operatorInsert;
    private final int removeIndex;
    private final int insertIndex;
    private final int hashCode;


    public InterMove(Operator operatorRemove, int removeIndex, Operator operatorInsert, int insertIndex) {
        this.operatorRemove = operatorRemove;
        this.operatorInsert = operatorInsert;
        this.removeIndex = removeIndex;
        this.insertIndex = insertIndex;
        String hashString = id + "" + ((operatorInsert.id < operatorRemove.id) ? (operatorInsert.id + "" + operatorRemove) : (operatorRemove+ "" + operatorInsert.id))
                +((removeIndex <= insertIndex) ? (removeIndex +" " + insertIndex) : ( insertIndex +"" + removeIndex));
        this.hashCode = Integer.parseInt(hashString);
    }


    public Operator getOperatorRemove() {
        return operatorRemove;
    }

    public Operator getOperatorInsert() {
        return operatorInsert;
    }

    public int getRemoveIndex() {
        return removeIndex;
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

}

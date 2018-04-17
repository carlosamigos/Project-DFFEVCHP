package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;

public class EjectionReplaceMutation extends Mutation {

    public final static int id = 9;
    private final Operator operator;
    private final int carMoveIndex;
    private final CarMove carMoveReplace;
    private final int hashCode;



    public EjectionReplaceMutation(Operator op, int index, CarMove carMoveReplace, CarMove toBeReplaced){
        this.operator = op;
        this.carMoveIndex = index;
        this.carMoveReplace = carMoveReplace;
        // * A possible approach is to construct the hashcode to only include cars, to make sure the newly injected move
        // * Is not ejected.
        int smallestDest = Math.min(carMoveReplace.getToNode().getNodeId(), toBeReplaced.getToNode().getNodeId());
        int largestDest = Math.max(carMoveReplace.getToNode().getNodeId(), toBeReplaced.getToNode().getNodeId());
        String hashString = "-" + id + carMoveReplace.getCar().getCarId() + "" + smallestDest + "" + largestDest;
        this.hashCode = Integer.parseInt(hashString);
    }

    public Operator getOperator() {
        return operator;
    }

    public int getCarMoveIndex() {
        return carMoveIndex;
    }

    public CarMove getCarMoveReplace() {
        return carMoveReplace;
    }

    @Override

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  EjectionReplaceMutation){
            return o.hashCode() == this.hashCode;
        } return false;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}

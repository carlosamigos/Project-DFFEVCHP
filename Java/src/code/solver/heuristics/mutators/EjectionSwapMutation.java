package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;

public class EjectionSwapMutation extends Mutation {

    public final static int id = 8;
    private final Operator operator;
    private final int carMoveIndex;
    private final CarMove carMoveReplace;
    private final int hashCode;


    public EjectionSwapMutation(Operator op, int carMoveIndex, CarMove carMoveReplace){
        this.operator = op;
        this.carMoveReplace = carMoveReplace;
        this.carMoveIndex = carMoveIndex;
        // * A possible approach is to construct the hashcode to only include cars, to make sure the newly injected move
        // * Is not ejected.
        int carMoveReplaceId = carMoveReplace.getCar().getCarId();
        int carMoveCurrentId = op.getCarMove(carMoveIndex).getCar().getCarId();
        String hashString = "-" + id + op.id + ((carMoveCurrentId <= carMoveReplaceId) ? ("" + carMoveCurrentId + carMoveReplaceId) :
                ("" + carMoveReplaceId + carMoveCurrentId));
        long conv = Long.valueOf(hashString);
        conv = conv % 105943;
        this.hashCode = (int) conv;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  EjectionSwapMutation){
            return o.hashCode() == this.hashCode;
        } return false;

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
    public int hashCode() {
        return this.hashCode;
    }
}

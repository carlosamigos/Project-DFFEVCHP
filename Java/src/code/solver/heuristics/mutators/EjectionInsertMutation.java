package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;

public class EjectionInsertMutation extends Mutation {

    public final static int id = 8;
    private final Operator operator;
    private final int carMoveIndex;
    private final CarMove carMoveReplace;
    private final int hashCode;


    public EjectionInsertMutation(Operator op, int carMoveIndex, CarMove carMoveReplace){
        this.operator = op;
        this.carMoveReplace = carMoveReplace;
        this.carMoveIndex = carMoveIndex;
        // * A possible approach is to construct the hashcode to only include cars, to make sure the newly injected move
        // * Is not ejected.
        String hashString = "" + id + "" + carMoveReplace.getCar().getCarId();
        this.hashCode = Integer.parseInt(hashString);
    }


    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  EjectionInsertMutation){
            return o.hashCode() == this.hashCode;
        } return false;

    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}

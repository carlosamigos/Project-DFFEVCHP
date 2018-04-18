package code.solver.heuristics.mutators;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;

public class EjectionRemoveMutation extends Mutation{

    public final static int id = 7;
    private final Operator operator;
    private final int carRemoveIndex;
    private final int hashCode;


    public EjectionRemoveMutation(Operator op, int carMoveIndex, CarMove carMove){
        this.operator = op;
        this.carRemoveIndex = carMoveIndex;
        // * A possible approach is to construct the hashcode to only include cars, to make sure the newly injected move
        // * Is not ejected.
        String hashString = "-" + id + op.id + "" + carMove.getCar().getCarId() + carMoveIndex;
        this.hashCode = Integer.parseInt(hashString);
    }


    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  EjectionRemoveMutation){
            return o.hashCode() == this.hashCode;
        } return false;

    }

    public Operator getOperator() {
        return operator;
    }

    public int getCarMoveIndex() {
        return carRemoveIndex;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

}

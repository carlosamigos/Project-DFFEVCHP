package code.solver.heuristics.mutators;

import code.problem.entities.Car;
import code.solver.heuristics.entities.CarMove;

public class EjectionMutationRandom extends Mutation {

    public final static int id = 10;
    private int hashCode;


    public EjectionMutationRandom(HashMap<Car, ArrayList<CarMove>> carMoves, CarMove carMove)

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof  Insert){
            return o.hashCode() == this.hashCode;
        } return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}

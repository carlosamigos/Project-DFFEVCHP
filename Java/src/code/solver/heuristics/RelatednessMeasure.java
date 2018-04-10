package code.solver.heuristics;

import code.solver.heuristics.entities.CarMove;
import constants.HeuristicsConstants;

public class RelatednessMeasure {


    public static double relatedsessMeasure(CarMove carMove1, CarMove carMove2){
        double relatedNess = 0;
        relatedNess += HeuristicsConstants.FROM_NODE_WEIGHT
                * (carMove1.getFromNode().equals(carMove2.getFromNode()) ? 1 : 0);
        relatedNess += HeuristicsConstants.TO_NODE_WEIGHT
                * (carMove1.getToNode().equals(carMove2.getToNode()) ? 1 : 0);
        relatedNess += HeuristicsConstants.IS_CHARGING_WEIGHT
                * (carMove1.isToCharging() == carMove2.isToCharging() ? 1 : 0);
        relatedNess += HeuristicsConstants.TRAVEL_DISTANCE_WEIGHT
                * Math.abs(carMove1.getTravelTime() - carMove2.getTravelTime());
        relatedNess += HeuristicsConstants.EARLIEST_DEPARTURE_WEIGHT
                * Math.abs(carMove1.getEarliestDepartureTime() - carMove2.getEarliestDepartureTime());
        return relatedNess;
    }
}

package tio4500;

public class ProblemGenerator {

    public ProblemGenerator(){



    }

    //TODO: 1 Create new state for Solver given problemInstance

    //TODO 1.1 DEFINE CONSTANTS - should be read once by the ProblemInstance:

        // numPNodes
        // numCNodes
        // numROperators
        // nodeSubsetIndexes
        // travelTimeVehicle
        // travelTImeBike
        // handlingTimeP
        // handlingTimeC
        // mode

        // NEW:
        // maxTravelCharging - maximum time one can travel with cars that needs charging

    //TODO 1.1 DEFINE SEMI-CONSTANT - may calculate these, needs discussion

        // numVisits
        // costOfDeviation
        // costOfPostponedCharging
        // costOfExtraTime
        // costOfTravel
        // costOfTimeUse
        // costOfMaxTravel
        // timeLimitLastVisit
        // sequenceBigM


    //TODO 1.2 DEFINE VARIABLE CONSTANTS - needs to be calculated at each iteration

        // numAOperators
        // originNodeROperator
        // chargingNodeAOperator
        // parkingNodeAOperator
        // chargingSlotsAvailable
        // travelTimeTOOriginR
        // travelTimeToParkingA
        // initialHandling
        // initialRegularInP
        // initialInNeedP
        // finishedDuringC
        // idealStateP
        // deandP

}

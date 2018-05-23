import math
import random
import sys
import time
import copy
import os.path


# OLD (59.952483, 10.795069)
# NEW (59.954105, 10.819250)
### CONSTANTS ####

# EUCLEDIAN DISTANCE #
DISTANCESCALE = 3

# NUMBER OF EXAMPLES TO CREATE #
EXAMPLES = 1

#BOARD SIZE
XSIZE = 5
YSIZE = 10

#ALLOWED MOVES
MOVES = 25
CARSCHARGING = 20

#PARKING NODES USED
MAXNODES = XSIZE * YSIZE

#CHARGING NODES
NUMCHARGING = 24
PARKINGC = [2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48]
CAPACITY = [6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6]
TOTALCAPACITY = [6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6]

#OPERATORS
NUMOPERATORS = 12
STARTETIMEOP = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
HANDLINGOP = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
NUMTASKS = 7


# MAKING NODES - DON' CHANGE #
SPREAD = True
CLUSTER = True
WRITETOFILE = True
PRINT = True
BIGM = False

# FILES
CARFILE = "travelTimesCar.txt"
BIKEFILE = "travelTimesBike.txt"
SEGMENTS = 3
TOTALNODES = 225


# OUTPUT - the lists are run in a for loop #
# * First parameter: Does not matter in this iteration
# * Second parameter: Weight for not meeting ideal state
# * Thirds parameter: Weight for not setting vehicles to charging
# * Fourth parameter: Weight for traveling by service operator
# * Fifth parameter: Weight for handling by service operator

MODES_RUN2 = [[2, 10, 30, 0.05, 0.4]]

## CLASSES ##
class World:

    def __init__(self):

        # GENERAL CONSTANTS #
        self.VISITS = 0
        self.MODE = 0
        self.SBIGM = 0

        # COST CONSTANTS #
        self.COSTOFDEV = 0
        self.COSTOFPOS = 0
        self.COSTOFEXTRAT = 0
        self.COSTOFTRAVEL = 0
        self.COSTOFTRAVELH = 0

        # TIME CONSTANTS #
        self.HANDLINGTIMEP = 0
        self.HANDLINGTIMEC = 0
        self.TIMELIMIT = 0
        self.TIMELIMITLAST = 0
        self.MAXHTOC = 0

        # WORLD CONSTANTS #
        self.YCORD = 0
        self.XCORD = 0

        # COORDINATE CONSTANTS
        self.UPPERRIGHT = (0, 0)
        self.LOWERLEFT = (0, 0)

        # ENTETIES #
        self.operators = []
        self.fCCars = []
        self.nodes = []
        self.pNodes = []
        self.cNodes = []
        self.distancesB = []
        self.distancesC = []
        self.bigM = []
        self.visitList = []
        self.surp = []
        self.deficit = []
        self.charg = []
        self.cars = []

    def addDim(self, xCord, yCord):
        self.XCORD = xCord
        self.YCORD = yCord

    def addNodes(self, node):
        self.nodes.append(node)

    def addPNodes(self, pNode):
        self.pNodes.append(pNode)

    def addcNodes(self, cNode):
        self.cNodes.append(cNode)

    def addOperator(self, operator):
        self.operators.append(operator)

    def addfCCars(self, fCCar):
        self.fCCars.append(fCCar)

    def addCar(self, car):
        self.cars.append(car)


    ## CALCULATE DISTANCE ##
    def setDistancesFromFileCar(self):
        try:
            f = open(CARFILE, "r")
            for line in f:
                array = [float(format(float(x[0:len(x) - 1]), '.1f')) for x in line[1:len(line)-1].split(",")]
                self.distancesC.append(array)
        except IOError:
            return 0

    def setDistancesFromFileBike(self):
        try:
            f = open(BIKEFILE, "r")
            for line in f:
                array = [float(format(float(x[0:len(x) - 1]), '.1f')) for x in line[1:len(line)-1].split(",")]
                self.distancesB.append(array)
        except IOError:
            return 0

    def addChargingStationsToDistances(self, array, index):
        for i in range(len(self.cNodes)):
            if(self.cNodes[i].pNode - 1 == index):
                array.append(1.0)
            else:
                array.append(array[self.cNodes[i].pNode - 1])
        return array

    def addChargingStationsDistancesBike(self):
        for i in range(len(self.cNodes)):
            travelList = copy.deepcopy(self.distancesB[self.cNodes[i].pNode-1])
            self.distancesB.append(travelList)
            self.distancesB[len(self.distancesB) -1][self.cNodes[i].pNode-1] = 1.0
            #self.distancesB[len(self.distancesB) - 1][len(self.distancesB) - len(self.cNodes) + i] = 0.00

    def addChargingStationsDistancesCar(self):
        for i in range(len(self.cNodes)):
            travelList = copy.deepcopy(self.distancesC[self.cNodes[i].pNode - 1])
            self.distancesC.append(travelList)
            self.distancesC[len(self.distancesC) -1][self.cNodes[i].pNode-1] = 1.0
            #self.distancesC[len(self.distancesC) - 1][len(self.distancesC) - len(self.cNodes) + i] = 0

    def divideParkingNodes(self):
        k, m = divmod(MAXNODES, 3)
        segmentSizes = []
        segmentSizes.append(k + min(m,1))
        segmentSizes.append(k + min(max(m-1, 0), 1))
        segmentSizes.append(k)
        return segmentSizes

    def pickRandomNodes(self, segmentSizes):
        increment = 75
        samples = []
        counter = 0
        for number in segmentSizes:
            sampleSegment = random.sample(range(counter * increment, (counter + 1) * increment - 1), number)
            samples.extend(sampleSegment)
            counter += 1
        return samples

    def pickParkingNodesFromMatrix(self, samples, matrix):
        travelTime = []
        for number in samples:
            travelTimeForNode = []
            for element in samples:
                travelTimeForNode.append(matrix[number][element])
            travelTime.append(travelTimeForNode)
        return travelTime

    def addChargingNodes(self, travelTimes):
        counter = 0
        for array in travelTimes:
            self.addChargingStationsToDistances(array, counter)
            counter += 1
        return travelTimes


    def divideParkingNodesAtRandom(self):
        segmentSizes = self.divideParkingNodes()
        samples = self.pickRandomNodes(segmentSizes)
        travelTimesBike = self.pickParkingNodesFromMatrix(samples, self.distancesB)
        travelTimesCar = self.pickParkingNodesFromMatrix(samples, self.distancesC)
        self.distancesB = self.addChargingNodes(travelTimesBike)
        self.distancesC = self.addChargingNodes(travelTimesCar)
        self.addChargingStationsDistancesBike()
        self.addChargingStationsDistancesCar()

    def legacyTravelingMatrix(self, matrix):
        travelMatrix = []
        for distances in matrix:
            travelMatrix.extend(distances)
        return travelMatrix

    def inititateDistanceMatrix(self):
        self.setDistancesFromFileCar()
        self.setDistancesFromFileBike()
        self.divideParkingNodesAtRandom()
        self.distancesB = self.legacyTravelingMatrix(self.distancesB)
        self.distancesC = self.legacyTravelingMatrix(self.distancesC)
        print("Finished")



    # Big M for the model built on car moves
    def calculateBigM(self):
        for i in range(len(self.cars)):
            print(i)
            for j in range(len(self.cars[i].destinations)):
                maxDiff = 0
                for l in range(len(self.cars)):
                    for k in range(len(self.cars[l].destinations)):
                        for x in range(len(self.pNodes)):
                            if((self.pNodes[x].surplus > 0 or self.pNodes[x].cState > 0)):
                                distances1 = self.distancesB[(len(self.nodes) * (self.cars[i].destinations[j] -1)) + x]
                                distances2 = self.distancesB[(len(self.nodes) * (self.cars[l].destinations[k] - 1)) + x]
                                handlingTime2 = self.distancesC[len(self.nodes) * (self.cars[l].parkingNode - 1) + self.cars[l].destinations[k] - 1]
                                diff = distances1 - (distances2 + handlingTime2)
                                if(diff > maxDiff):
                                    maxDiff = diff
                bigMdiff = maxDiff
                bigM = float(format(bigMdiff, '.1f'))
                self.bigM.append(bigM)

    ## CALCULATE VISITS ##

    def calculateInitialAdd(self):
        initial_theta = [0 for i in range(len(self.nodes))]
        initial_handling = [0 for i in range(len(self.nodes))]
        initial_lambda = [0 for i in range(len(self.nodes))]
        initial_service = [0 for i in range(len(self.nodes))]
        for j in range(len(self.fCCars)):
            initial_theta[self.fCCars[j].parkingNode - 1] += 1
            initial_service[self.fCCars[j].parkingNode - 1] += 1
            initial_lambda[self.fCCars[j].parkingNode - 1] += 1
        for j in range(len(self.operators)):
            initial_theta[self.operators[j].startNode - 1] += 1
            if(self.operators[j].handling):
                initial_lambda[self.operators[j].startNode - 1] += 1
                initial_handling[self.operators[j].startNode - 1] += 1
        return initial_theta, initial_handling, initial_lambda, initial_service

    def calculateSpecificVisitParking(self, initial_theta, initial_lambda, initial_service, i):
        initial_omega = initial_lambda[i]
        if(self.pNodes[i].pState - (self.pNodes[i].iState + self.pNodes[i].demand) < initial_theta[i] - initial_lambda[i]):
            initial_omega = initial_theta[i]

        first_term = (self.pNodes[i].iState + self.pNodes[i].demand) - (self.pNodes[i].pState + initial_lambda[i]) + initial_theta[i]
        second_term = (self.pNodes[i].pState + initial_service[i]) - (self.pNodes[i].iState + self.pNodes[i].demand) + initial_omega
        last_term = initial_theta[i]

        visit = max(first_term, max(second_term, last_term))
        visit += self.pNodes[i].cState

        return visit

    def calculateVisitList(self):
        numCharging = 0
        initial_theta, initial_handling, initial_lambda, initial_service = self.calculateInitialAdd()
        for i in range(len(self.pNodes)):
            visit = self.calculateSpecificVisitParking(initial_theta, initial_lambda, initial_service, i)
            self.visitList.append(max(visit, 2))
            numCharging += self.pNodes[i].cState
        for i in range(len(self.cNodes)):
            visit = min(numCharging, self.cNodes[i].totalCapacity + self.cNodes[i].finishes - initial_handling[len(self.pNodes) + i])
            visit += self.cNodes[i].finishes
            visit += initial_theta[len(self.pNodes) + i]
            self.visitList.append(max(visit,2))
        for i in range(len(self.operators)):
            self.visitList.append(1)
            self.visitList.append(1)

    ## SCALE IDEAL STATE ##

    def createRealIdeal(self):
        initialAdd = [0 for i in range(len(self.nodes))]
        for j in range(len(self.fCCars)):
            initialAdd[self.fCCars[j].parkingNode - 1] += 1
        for j in range(len(self.operators)):
            if (self.operators[j].handling):
                initialAdd[self.operators[j].startNode - 1] += 1
        sumIState = 0
        sumPState = 0
        for i in range(len(self.pNodes)):
            sumIState += self.pNodes[i].iState
            sumPState += self.pNodes[i].pState - self.pNodes[i].demand + initialAdd[i]

        sumIStateAfter = 0
        for j in range(len(self.pNodes)):
            self.pNodes[j].iState = int(round(float(sumPState) * (float(self.pNodes[j].iState) / sumIState)))
            sumIStateAfter += self.pNodes[j].iState
        while(sumIStateAfter != sumPState):
            if(sumIStateAfter < sumPState):
                r = random.randint(0, len(self.pNodes) -1)
                self.pNodes[r].iState += 1
                sumIStateAfter += 1
            else:
                r = random.randint(0, len(self.pNodes) - 1)
                if(self.pNodes[r].iState > 0):
                    self.pNodes[r].iState -= 1
                    sumIStateAfter -= 1

    def calculateMovesToIDeal(self):
        moves = 0
        initial_theta, initial_handling, initial_lambda, initial_service = self.calculateInitialAdd()
        for i in range(len(self.pNodes)):
            deficit = (self.pNodes[i].iState + self.pNodes[i].demand) - (self.pNodes[i].pState + initial_lambda[i])
            moves += max(deficit,0)
            #moves += self.pNodes[i].cState

        return moves

    def calculateMovesListToIdeal(self):
        movesDef = []
        movesSurp = []
        movesCharg = []
        initial_theta, initial_handling, initial_lambda, initial_service = self.calculateInitialAdd()
        for i in range(len(self.pNodes)):
            deficit = (self.pNodes[i].iState + self.pNodes[i].demand) - (self.pNodes[i].pState + initial_lambda[i])
            surplus = (self.pNodes[i].pState + initial_lambda[i]) - (self.pNodes[i].iState + self.pNodes[i].demand)
            movesDef.append(max(deficit, 0))
            movesSurp.append(max(surplus, 0))
            movesCharg.append(self.pNodes[i].cState)
        for i in range(len(self.cNodes) + 2* len(self.operators)):
            movesDef.append(1)
            movesSurp.append(1)
            movesCharg.append(1)

        self.deficit = movesDef
        self.surp = movesSurp
        self.charg = movesCharg

    def checkSurplusNode(self, i):
        initial_theta, initial_handling, initial_lambda, initial_service = self.calculateInitialAdd()
        if ((self.pNodes[i].iState + self.pNodes[i].demand) - (self.pNodes[i].pState + initial_lambda[i]) > 0):
            return False
        return True

    def checkdeficitNode(self, i):
        initial_theta, initial_handling, initial_lambda, initial_service = self.calculateInitialAdd()
        if((self.pNodes[i].iState + self.pNodes[i].demand) - (self.pNodes[i].pState + initial_lambda[i]) < 0):
            return False
        return True

    def shuffleIdealState(self):
        moves = self.calculateMovesToIDeal()
        iStateList = []
        cStateList = []
        numCMoves = 0
        for i in range(len(self.pNodes)):
            iStateList.append(self.pNodes[i].iState)
            cStateList.append(self.pNodes[i].cState)
            numCMoves += self.pNodes[i].cState


        while (float(numCMoves) > CARSCHARGING or float(numCMoves) < CARSCHARGING):
            r = random.randint(0, len(self.pNodes) - 1)
            if(float(numCMoves) > CARSCHARGING):
                if (cStateList[r] > 0):
                    cStateList[r] -= 1
                    numCMoves -= 1
            else:
                cStateList[r] += 1
                numCMoves += 1
        for i in range(len(self.pNodes)):
            self.pNodes[i].cState = cStateList[i]

        while(moves > MOVES or moves < MOVES):
            moves = self.calculateMovesToIDeal()
            if(moves > MOVES):
                r1 = random.randint(0, len(self.pNodes) - 1)
                r2 = random.randint(0, len(self.pNodes) - 1)
                while (r1 == r2 or iStateList[r1] == 0 or self.checkSurplusNode(r1) or self.checkdeficitNode(r2)):
                    r1 = random.randint(0, len(self.pNodes) - 1)
                    r2 = random.randint(0, len(self.pNodes) - 1)
                iStateList[r1] -= 1
                iStateList[r2] += 1

            elif(moves < MOVES):
                r1 = random.randint(0, len(self.pNodes) - 1)
                r2 = random.randint(0, len(self.pNodes) - 1)
                while (r1 == r2 or iStateList[r1] == 0 or self.checkSurplusNode(r2) or self.checkdeficitNode(r1)):
                    r1 = random.randint(0, len(self.pNodes) - 1)
                    r2 = random.randint(0, len(self.pNodes) -1)
                iStateList[r1] -= 1
                iStateList[r2] += 1

            for i in range(len(self.pNodes)):
                self.pNodes[i].iState = iStateList[i]

            moves = self.calculateMovesToIDeal()

    def calculateNodeDiff(self):
        initial_theta, initial_handling, initial_lambda, initial_service = self.calculateInitialAdd()
        for i in range(len(self.pNodes)):
            self.pNodes[i].surplus = (self.pNodes[i].pState + initial_lambda[i]) - (self.pNodes[i].iState + self.pNodes[i].demand)



    ### SET CONSTANTS ###

    def setConstants(self, visits, mode, sBigM):
        self.VISITS = visits
        self.MODE = mode
        self.SBIGM = sBigM

    def setCostConstants(self, costOfDev, costOfPos, costOfExtraT, costOfTravel, costOfTravelH):
        self.COSTOFDEV = costOfDev
        self.COSTOFPOS = costOfPos
        self.COSTOFEXTRAT = costOfExtraT
        self.COSTOFTRAVEL = costOfTravel
        self.COSTOFTRAVELH = costOfTravelH

    def setTimeConstants(self, handlingTimeP, handlingTimeC, timeLimit, timeLimitLast, maxHToC):
        self.HANDLINGTIMEP = handlingTimeP
        self.HANDLINGTIMEC = handlingTimeC
        self.TIMELIMIT = timeLimit
        self.TIMELIMITLAST = timeLimitLast
        self.MAXHTOC = maxHToC

    def setCordConstants(self, upperRight, lowerLeft):
        self.UPPERRIGHT = upperRight
        self.LOWERLEFT = lowerLeft


    ## FILE HANDLER ##

    def writeToFile(self, example):
        fileName = "../../Testing/Input/Static/ModelTesting/" + str(example) + "_a.txt"
        if (os.path.exists(fileName)):
            fileName = "../../Testing/Input/Static/ModelTesting/" + str(example) + "_b.txt"
            if (os.path.exists(fileName)):
                fileName = "../../Testing/Input/Static/ModelTesting/" + str(example) + "_c.txt"
        f = open(fileName, 'w')
        string = ""
        string += "numVisits: " + str(self.VISITS) + "\n"
        string += "numPNodes: " + str(len(self.pNodes)) + "\n"
        string += "numCNodes: " + str(len(self.cNodes)) + "\n"
        string += "numROperators: " + str(len(self.operators)) + "\n"
        string += "numAOperators: " + str(len(self.fCCars)) + "\n"
        string += "\n"
        string += "hNodes : " + str(self.YCORD) + "\n"
        string += "wNodes : " + str(self.XCORD) + "\n"
        string += "nodeSubsetIndexes: ["
        for i in range(len(self.pNodes)):
            string += str(i +1)
            if(i < len(self.pNodes) -1):
                string+= " "
            else:
                string += "] \n"
        string += "\n"
        string += "startNodeROperator: ["
        for i in range(len(self.operators)):
            string += str(self.operators[i].startNode)
            if (i < len(self.operators) - 1):
                string += " "
        string += "] \n"
        string += "originNodeROperator: ["
        for i in range(len(self.operators)):
            string += str(i + len(self.nodes) +1)
            if (i < len(self.operators) - 1):
                string += " "
            else:
                string += "] \n"
        string += "destinationNodeROperator: ["
        for i in range(len(self.operators)):
            string += str(i + len(self.nodes) + len(self.operators) + 1)
            if (i < len(self.operators) - 1):
                string += " "
        string += "] \n"
        string += "cToP: ["
        for i in range(len(self.cNodes)):
            string += str(self.cNodes[i].pNode)
            if (i < len(self.cNodes) - 1):
                string += " "
        string += "] \n"
        string += "parkingNodeAOperator: ["
        for i in range(len(self.fCCars)):
            string += str(self.fCCars[i].parkingNode)
            if (i < len(self.fCCars) - 1):
                string += " "
        string += "] \n"
        string += "chargingNodeAOperator: ["
        for i in range(len(self.fCCars)):
            string += str(self.fCCars[i].startNode)
            if (i < len(self.fCCars) - 1):
                string += " "
        string += "] \n"
        string += "\n"
        string += "chargingSlotsAvailable: ["
        for i in range(len(self.cNodes)):
            string += str(self.cNodes[i].capacity)
            if (i < len(self.cNodes) - 1):
                string += " "
        string += "] \n"
        string += "totalNumberOfChargingSlots: ["
        for i in range(len(self.cNodes)):
            string += str(self.cNodes[i].totalCapacity)
            if (i < len(self.cNodes) - 1):
                string += " "
        string += "] \n"
        string += "\n"
        string += "costOfDeviation : " + str(self.COSTOFDEV) + "\n"
        string += "costOfPostponedCharging : " + str(self.COSTOFPOS) + "\n"
        string += "costOfExtraTime : " + str(self.COSTOFEXTRAT) + "\n"
        string += "costOfTravel: " + str(self.COSTOFTRAVEL) + "\n"
        string += "costOfTravelH: " + str(self.COSTOFTRAVELH) + "\n"
        string += "\n"
        string += "travelTimeVehicle: ["
        for i in range(len(self.nodes)):
            for j in range(len(self.nodes)):
                string += str(self.distancesC[i*len(self.nodes) + j]) + " "
            if(i < len(self.nodes) -1):
                string += "\n"
                string+= "\t" + "\t"
        string+="]" + "\n"
        string += "\n"
        string += "travelTimeBike: ["
        for i in range(len(self.nodes)):
            for j in range(len(self.nodes)):
                string += str(self.distancesB[i * len(self.nodes) + j]) + " "
            if (i < len(self.nodes) - 1):
                string += "\n"
                string += "\t" + "\t"
        string += "]" + "\n"
        string += "\n"
        string += "handlingTimeP: " + str(self.HANDLINGTIMEP) + "\n"
        string += "handlingTimeC: " + str(self.HANDLINGTIMEC) + "\n"
        string += "travelTimeToOriginR: ["
        for i in range(len(self.operators)):
            string += str(self.operators[i].startTime)
            if (i < len(self.operators) - 1):
                string += " "
            else:
                string += "] \n"
        string += "travelTimeToParkingA: ["
        for i in range(len(self.fCCars)):
            string += str(self.fCCars[i].remainingTime)
            if (i < len(self.fCCars) - 1):
                string += " "

        string += "] \n"
        string += "timeLimit: " + str(self.TIMELIMIT) + "\n"
        string += "timeLimitLastVisit: " + str(self.TIMELIMITLAST) + "\n"
        string += "maxTravelHToC: " + str(self.MAXHTOC) + "\n"
        string += "\n"
        string += "initialHandling: ["
        for i in range(len(self.operators)):
            string += str(1 if self.operators[i].handling else 0)
            if (i < len(self.operators) - 1):
                string += " "
            else:
                string += "] \n"
        string += "initialRegularInP: ["
        for i in range(len(self.pNodes)):
            string += str(self.pNodes[i].pState)
            if (i < len(self.pNodes) - 1):
                string += " "
            else:
                string += "] \n"
        string += "initialInNeedP: ["
        for i in range(len(self.pNodes)):
            string += str(self.pNodes[i].cState)
            if (i < len(self.pNodes) - 1):
                string += " "
            else:
                string += "] \n"
        string += "finishedDuringC: ["
        for i in range(len(self.cNodes)):
            string += str(self.cNodes[i].finishes)
            if (i < len(self.cNodes) - 1):
                string += " "
            else:
                string += "] \n"
        string += "idealStateP: ["
        for i in range(len(self.pNodes)):
            string += str(self.pNodes[i].iState)
            if (i < len(self.pNodes) - 1):
                string += " "
            else:
                string += "] \n"
        string += "\n"
        string += "demandP: ["
        for i in range(len(self.pNodes)):
            string += str(self.pNodes[i].demand)
            if (i < len(self.pNodes) - 1):
                string += " "
            else:
                string += "] \n"
        string += "\n"
        string += "mode: " + str(self.MODE) + "\n"
        string += "sequenceBigM: " + str(self.SBIGM) + "\n"
        string += "visitList: ["
        for i in range(len(self.visitList)):
            string += str(self.visitList[i])
            if (i < len(self.visitList) - 1):
                string += " "
        string += "] \n"
        string += "\n"
        count = 0
        for i in range(len(self.cars)):
            if not(self.cars[i].charging):
                for j in range(len(self.cars[i].destinations)):
                    count += 1
        string += "numCarMovesP : " + str(count) + "\n"
        count = 0
        for i in range(len(self.cars)):
            if(self.cars[i].charging):
                for j in range(len(self.cars[i].destinations)):
                    count += 1
        string += "numCarMovesC : " + str(count) + "\n"
        string += "numCars : " + str(len(self.cars)) + "\n"
        string += "numTasks : " + str(NUMTASKS) + "\n"
        count = 0
        for i in range(len(self.pNodes)):
            if(self.pNodes[i].surplus < 0):
                count += 1
        string += "numDeficitNodes : " + str(count) + "\n"
        string += "\n"
        deficitNodes = []
        for i in range(len(self.pNodes)):
            if (self.pNodes[i].surplus < 0):
                deficitNodes.append(i)
        string += "deficitTranslate : ["
        for i in range(len(deficitNodes)):
            string += str(deficitNodes[i]+1)
            if (i < len(deficitNodes) - 1):
                string += " "
        string += "] \n"
        string += "deficitCarsInNode : ["
        for i in range(len(deficitNodes)):
            string += str(-self.pNodes[deficitNodes[i]].surplus)
            if (i < len(deficitNodes) - 1):
                string += " "
        string += "] \n"
        string += "carMoveCars : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                string += str(i+1)
                if (i < len(self.cars) - 1):
                    string += " "
                else:
                    if(j < len(self.cars[i].destinations) -1):
                        string += " "
        string += "] \n"
        string += "carMoveOrigin : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                string += str(self.cars[i].parkingNode)
                if (i < len(self.cars) - 1):
                    string += " "
                else:
                    if(j < len(self.cars[i].destinations) -1):
                        string += " "
        string += "] \n"
        string += "carMoveDestination : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                string += str(self.cars[i].destinations[j])
                if (i < len(self.cars) - 1):
                    string += " "
                else:
                    if(j < len(self.cars[i].destinations) -1):
                        string += " "
        string += "] \n"
        string += "carMoveHandlingTime : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                if(self.cars[i].destinations[j] > MAXNODES):
                    string += str(self.distancesC[(self.cars[i].parkingNode -1)*len(self.nodes) + self.cars[i].destinations[j] -1] + self.HANDLINGTIMEC)
                else:
                    string += str(self.distancesC[(self.cars[i].parkingNode - 1) * len(self.nodes) + self.cars[i].destinations[j] - 1] + self.HANDLINGTIMEP)
                if (i < len(self.cars) - 1):
                    string += " "
                else:
                    if(j < len(self.cars[i].destinations) -1):
                        string += " "
        string += "] \n"
        string += "carMoveStartingTime : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                string += str(self.cars[i].startTime)
                if (i < len(self.cars) - 1):
                    string += " "
                else:
                    if (j < len(self.cars[i].destinations) - 1):
                        string += " "
        string += "] \n"
        count = 0
        for i in range(len(self.pNodes)):
            if(self.pNodes[i].cState > 0):
                count += 1
        string += "numCarsInCNeedNodes : " + str(count)
        string += "\n"
        carsInNeedCNodes = []
        for i in range(len(self.pNodes)):
            if (self.pNodes[i].cState > 0):
                carsInNeedCNodes.append(i)
        string += "carsInNeedCTranslate : ["
        for i in range(len(carsInNeedCNodes)):
            string += str(carsInNeedCNodes[i] + 1)
            if (i < len(carsInNeedCNodes) - 1):
                string += " "
        string += "]\n"
        string += "carsInNeedNodes : ["
        for i in range(len(carsInNeedCNodes)):
            string += str(self.pNodes[carsInNeedCNodes[i]].cState)
            if (i < len(carsInNeedCNodes) - 1):
                string += " "
        string += "]\n"
        if(BIGM):
            string += "bigMCars : ["
            for i in range(len(self.bigM)):
                string += str(self.bigM[i])
                if(i < len(self.bigM) -1):
                    string += " "
            string += "]"
            string += "\n"
        string += "\n"
        string += "originCarMoveOperator : ["
        count = 0
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                count +=1
        for i in range(len(self.operators)):
            string += str(count + i + 1)
            if(i < len(self.operators) -1):
                string += " "
        string += "] \n"
        string += "destinationCarMoveOperator : ["
        for i in range(len(self.operators)):
            string += str(len(self.operators) + count + 1 + i)
            if(i < len(self.operators) -1):
                string += " "
        string += "] \n"
        string += "numCarMovesA : " + str(len(self.operators) * 2) + "\n"
        string += "carMoveOriginA : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                string += str(self.cars[i].parkingNode)
                if (i <= len(self.cars) - 1):
                    string += " "
                else:
                    if(j <= len(self.cars[i].destinations) -1):
                        string += " "
        for i in range(len(self.operators)):
            string += str(self.operators[i].startNode)
            if (i < len(self.operators) - 1):
                string += " "
        string += " "
        for i in range(len(self.operators)):
            string += str(i + len(self.nodes) + len(self.operators) + 1)
            if (i < len(self.operators) - 1):
                string += " "
        string += "] \n"
        string += "carMoveDestinationA : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                string += str(self.cars[i].destinations[j])
                if (i <= len(self.cars) - 1):
                    string += " "
                else:
                    if (j <= len(self.cars[i].destinations) - 1):
                        string += " "
        for i in range(len(self.operators)):
            string += str(self.operators[i].startNode)
            if (i < len(self.operators) - 1):
                string += " "
        string += " "
        for i in range(len(self.operators)):
            string += str(i + len(self.nodes) + len(self.operators) + 1)
            if (i < len(self.operators) - 1):
                    string += " "
        string += "] \n"
        string += "carMoveHandlingTimeA : ["
        for i in range(len(self.cars)):
            for j in range(len(self.cars[i].destinations)):
                if(self.cars[i].destinations[j] > MAXNODES):
                    string += str(self.distancesC[
                                          (self.cars[i].parkingNode - 1) * len(self.nodes) + self.cars[i].destinations[
                                              j] - 1] + self.HANDLINGTIMEC)
                else:
                    string += str(self.distancesC[
                                      (self.cars[i].parkingNode - 1) * len(self.nodes) + self.cars[i].destinations[
                                          j] - 1] + self.HANDLINGTIMEP)
                if (i < len(self.cars) - 1):
                    string += " "
                else:
                    if (j <= len(self.cars[i].destinations) - 1):
                        string += " "
        for i in range(len(self.operators)):
            string += str(0)
            if(i <= len(self.operators) - 1):
                string += " "
        for i in range(len(self.operators)):
            string += str(0)
            if(i < len(self.operators) - 1):
                string += " "
        string += "] \n"
        f.write(string)
        if(PRINT):
            print(string)

class pNode:

    def __init__(self, xCord, yCord, pState, cState, iState, demand):
        self.xCord = xCord
        self.yCord = yCord
        self.charging = False
        self.pState = pState
        self.cState = cState
        self.iState = iState
        self.demand = demand
        self.surplus = 0

class cNode:

    def __init__(self, xCord, yCord, capacity, finishes, totalCapacity, pNode):
        self.xCord = xCord
        self.yCord = yCord
        self.capacity = capacity
        self.finishes = finishes
        self.totalCapacity = totalCapacity
        self.pNode = pNode

class operator:

    def __init__(self, startNode, startTime, handling):
        self.startNode = startNode
        self.startTime = startTime
        self.handling = False
        if(handling):
            self.handling = True

class fCCars:

    def __init__(self, startNode, parkingNode, remainingTime):
        self.startNode = startNode
        self.parkingNode = parkingNode
        self.remainingTime = remainingTime

class car:
    def __init__(self, startTime, parkingNode, charging):
        self.startTime = startTime
        self.parkingNode = parkingNode
        self.destinations = []
        self.charging = charging

    def calculateDestinations(self, world):
        pass

## - CREATORS - ##

# PARKING NODES
def createNodes(world):
    for i in range(YSIZE):
        xCord = i
        for j in range(XSIZE):
            pState = random.randint(0, 3)
            cState = random.randint(0, 1)
            iState = random.randint(0, 4)
            demand = 0
            yCord = j
            node = pNode(xCord, yCord, pState, cState, iState, demand)
            world.addNodes(node)
            world.addPNodes(node)
    world.addDim(XSIZE, YSIZE)

# ARTIFICIAL OPERATORS
def createFCCars(world, time, cNode, pNode):
    fc = fCCars(cNode, pNode, time)
    world.addfCCars(fc)

# CHARGING NODES
def createCNodes(world):
    numCNodes = NUMCHARGING
    for i in range(numCNodes):
        print("Creating charging node: ", i+1)
        pNodeNum = PARKINGC[i]
        pNode = world.pNodes[pNodeNum-1]
        capacity = CAPACITY[i]
        totalCapacity = TOTALCAPACITY[i]
        fCCars = totalCapacity - capacity
        for j in range(fCCars):
            time = random.randint(0, 10)
            createFCCars(world, time, i + len(world.pNodes) + 1, pNodeNum)
        cN = cNode(pNode.xCord, pNode.yCord, capacity, fCCars, totalCapacity, pNodeNum)
        world.addcNodes(cN)
        world.addNodes(cN)

# OPERATORS
def createOperators(world):
    numOperators = NUMOPERATORS
    for i in range(numOperators):
        print("Creating operator", i+1)
        startNode = random.randint(1, len(world.pNodes))
        time = STARTETIMEOP[i]
        handling = HANDLINGOP[i]
        if(handling == 1):
            op = operator(startNode, time, True)
            world.addOperator(op)

        else:
            op = operator(startNode, time, False)
            world.addOperator(op)

# CARS
def createCars(world):
    initial_theta, initial_handling, initial_lambda, initial_service = world.calculateInitialAdd()
    movesDef = []
    movesSurp = []
    movesCharg = []
    for i in range(len(world.pNodes)):
        deficit = (world.pNodes[i].iState + world.pNodes[i].demand) - (world.pNodes[i].pState + initial_lambda[i])
        surplus = (world.pNodes[i].pState) - (world.pNodes[i].iState + world.pNodes[i].demand)
        movesDef.append(max(deficit, 0))
        movesSurp.append(max(surplus, 0))
        movesCharg.append(world.pNodes[i].cState)
    for i in range(len(world.cNodes) + 2 * len(world.operators)):
        movesDef.append(1)
        movesSurp.append(1)
        movesCharg.append(1)
    for i in range(len(world.pNodes)):
        for j in range(movesSurp[i]):
            newCar = car(0, i+1, False)
            for x in range(len(world.pNodes)):
                if(movesDef[x] > 0):
                    newCar.destinations.append(x + 1)
            world.addCar(newCar)
        if(initial_lambda[i] > 0 and (world.pNodes[i].pState + initial_lambda[i]) - (world.pNodes[i].iState + world.pNodes[i].demand) > 0):
            count = 0
            for j in range(len(world.operators)):
                if(world.operators[j].handling and world.operators[j].startNode - 1 == i):
                    if(count <= initial_lambda[i] and count <= (world.pNodes[i].pState + initial_lambda[i]) - (world.pNodes[i].iState + world.pNodes[i].demand)):
                        newCar = car(world.operators[j].startTime, i+1, False)
                        count += 1
                        for x in range(len(world.pNodes)):
                            if (movesDef[x] > 0 and x != i):
                                newCar.destinations.append(x + 1)
                        world.addCar(newCar)
            for j in range(len(world.fCCars)):
                if(world.fCCars[j].parkingNode -1 == i):
                    if (count <= initial_lambda[i] and count <= (world.pNodes[i].pState + initial_lambda[i]) - (
                        world.pNodes[i].iState + world.pNodes[i].demand)):
                        newCar = car(world.fCCars[j].remainingTime, i + 1, False)
                        count += 1
                        for x in range(len(world.pNodes)):
                            if (movesDef[x] > 0 and x != i):
                                newCar.destinations.append(x + 1)
                        world.addCar(newCar)
    for i in range(len(world.pNodes)):
        for j in range(movesCharg[i]):
            newCar = car(0, i + 1, True)
            for x in range(len(CAPACITY)):
                if(CAPACITY[x] > 0):
                    newCar.destinations.append(len(world.pNodes) + x +1)
            world.addCar(newCar)


# WORLD AND ENTITIES
def initiateWorld(world):
    world.setCordConstants((59.954105, 10.819250), (59.904574, 10.681527))
    createNodes(world)
    createCNodes(world)
    createOperators(world)



    ## - BUILDER -- ##

def buildWorld():
    world = World()
    initiateWorld(world)
    world.inititateDistanceMatrix()
    world.createRealIdeal()
    world.shuffleIdealState()
    world.setTimeConstants(4, 5, 60, 10, 30)
    world.calculateVisitList()
    world.calculateMovesListToIdeal()
    maxVisit = max(world.visitList)
    createCars(world)
    world.calculateNodeDiff()
    if(BIGM):
        world.calculateBigM()
    for i in range(len(MODES_RUN2)):
        world.setConstants(maxVisit, MODES_RUN2[i][0], 10)
        world.setCostConstants(MODES_RUN2[i][1], MODES_RUN2[i][2], 0.5, MODES_RUN2[i][3], MODES_RUN2[i][4])
        moves = world.calculateMovesToIDeal()
        totalMoves = 0
        for j in range(len(world.cars)):
            totalMoves += len(world.cars[j].destinations)
        filepath = "test_" + str(len(world.pNodes)) + "nodes_" + str(len(world.operators)) + "so_" + str(len(world.cNodes)) + "c_" + str(moves) + "mov_" + str(CARSCHARGING) + "charging_" + str(len(world.fCCars)) + "finishes_" + str(totalMoves) + "CM"
        print(filepath)
        if(WRITETOFILE):
            world.writeToFile(filepath)



def main():
    print("\n WELCOME TO THE EXAMPLE CREATOR \n")
    for i in range(EXAMPLES):
        print("Creating instance: ", i)
        buildWorld()

main()

list = [1, 2, 3]






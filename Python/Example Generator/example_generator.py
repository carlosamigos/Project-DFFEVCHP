import math
import random
import sys
import time
import copy
import os.path

sys.path.append('../')
from Data_Retrieval import googleTrafficInformationRetriever as gI



# CONSTANTS
DISTANCESCALE = 3
CARSCHARGING = 3
MOVES = 4
MAXNODES = 9
SPREAD = True
CLUSTER = True

MODES_RUN_1 = [[2, 10, 30, 0.05, 0.4], [1, 10, 30, 0.05, 0.4], [2, 30, 30, 0.05, 0.4], [1, 30, 30, 0.05, 0.4], [2, 30, 30, 0.4, 0.4], [1, 30, 30, 0.4, 0.4], [4, 10, 30, 0.05, 0.4], [4, 30, 30, 0.05, 0.4], [4, 30, 30, 0.4, 0.4]]
MODES_RUN_2 = [[2, 10, 30, 0.05, 0.4],  [2, 30, 30, 0.05, 0.4],  [2, 30, 30, 0.4, 0.4], [4, 10, 30, 0.05, 0.4], [4, 30, 30, 0.05, 0.4]]
MODES_RUN2 = [[2, 10, 30, 0.05, 0.4] , [4, 10, 30, 0.05, 0.4]]

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
        self.visitList = []
        self.surp = []
        self.deficit = []
        self.charg = []

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

    ## CALCULATE DISTANCE ##

    def calculateDistances(self):
        self.distancesC = []
        self.distancesB = []
        maxDistance = math.sqrt(math.pow(self.pNodes[0].xCord - self.pNodes[len(self.pNodes) - 1].xCord, 2) + math.pow(self.pNodes[0].yCord - self.pNodes[len(self.pNodes) - 1].yCord, 2))
        scale = float(format((self.TIMELIMIT + self.TIMELIMITLAST - 1)/(maxDistance*DISTANCESCALE), '.1f'))
        for x in range(len(self.nodes)):
            for y in range(len(self.nodes)):
                distance = math.pow(self.nodes[x].xCord - self.nodes[y].xCord, 2) + math.pow(self.nodes[x].yCord - self.nodes[y].yCord, 2)
                distanceSq = float(format(math.sqrt(distance)*scale, '.1f'))
                distanceB = float(format(distanceSq * 2, '.1f'))

                # Creating some distance between charging and parking nodes
                if (int(distance) == 0 and x != y):
                    distanceSq = 1.0
                    distanceB = 1.0

                self.distancesC.append(distanceSq)
                self.distancesB.append(distanceB)

    def giveRealCoordinatesSpread(self):
        stepX = (self.UPPERRIGHT[1] - self.LOWERLEFT[1]) / self.XCORD
        stepY = (self.UPPERRIGHT[0] - self.LOWERLEFT[0]) / self.YCORD
        startX = self.LOWERLEFT[1] + 0.5 * stepX
        startY = self.UPPERRIGHT[0] - 0.5 * stepY
        cords = []
        for i in range(self.YCORD):
            for j in range(self.XCORD):
                cordX = startX + j * stepX
                cordY = startY - i * stepY
                cord = (cordY, cordX)
                cords.append(cord)

        for i in range(len(cords) - MAXNODES):
            r = random.randint(0, len(cords) - 1)
            cords.pop(r)
            self.pNodes.pop((r))
            self.nodes.pop((r))
        return cords


    def giveRealCoordinatesCluster(self):
        pass

    def calculateRealDistances(self, cords):
        if(len(cords) == 0):
            stepX = (self.UPPERRIGHT[1] - self.LOWERLEFT[1])/self.XCORD
            stepY = (self.UPPERRIGHT[0] - self.LOWERLEFT[0])/self.YCORD
            startX = self.LOWERLEFT[1] + 0.5*stepX
            startY = self.UPPERRIGHT[0] - 0.5*stepY
            cords = []
            for i in range(self.YCORD):
                for j in range(self.XCORD):
                    cordX = startX + j*stepX
                    cordY = startY - i*stepY
                    cord = (cordY, cordX)
                    cords.append(cord)


        travelMatrixCar = gI.run(cords, "driving", False)
        time.sleep(2)
        travelMatrixBicycle = gI.run(cords, "bicycling", False)
        time.sleep(2)
        travelMatrixTransit = gI.run(cords, "transit", False)

        for i in range(len(travelMatrixBicycle)):
            for j in range(len(self.cNodes)):
                if(self.cNodes[j].pNode-1 == i):
                    travelMatrixBicycle[i].append(60)
                    travelMatrixTransit[i].append(60)
                    travelMatrixCar[i].append(60)
                else:
                    travelMatrixBicycle[i].append(travelMatrixBicycle[i][self.cNodes[j].pNode - 1])
                    travelMatrixTransit[i].append(travelMatrixTransit[i][self.cNodes[j].pNode - 1])
                    travelMatrixCar[i].append(travelMatrixCar[i][self.cNodes[j].pNode - 1])
        for i in range(len(self.cNodes)):
            travelMatrixBicycle.append(copy.deepcopy(travelMatrixBicycle[self.cNodes[i].pNode-1]))
            travelMatrixTransit.append(copy.deepcopy(travelMatrixTransit[self.cNodes[i].pNode-1]))
            travelMatrixCar.append(copy.deepcopy(travelMatrixCar[self.cNodes[i].pNode-1]))
            travelMatrixBicycle[len(travelMatrixBicycle) -1][self.cNodes[i].pNode-1] = 60
            travelMatrixTransit[len(travelMatrixTransit) - 1][self.cNodes[i].pNode - 1] = 60
            travelMatrixCar[len(travelMatrixCar) - 1][self.cNodes[i].pNode - 1] = 60
            travelMatrixBicycle[len(travelMatrixBicycle) - 1][len(self.pNodes) + i] = 0
            travelMatrixTransit[len(travelMatrixTransit) - 1][len(self.pNodes) + i] = 0
            travelMatrixCar[len(travelMatrixCar) - 1][len(self.pNodes) + i] = 0

        travelMatrixNotHandling = []
        travelMatrixHandling = []
        for i in range(len(travelMatrixBicycle)):
            for j in range(len(travelMatrixBicycle[i])):
                travelMatrixNotHandling.append(float(format(min(travelMatrixBicycle[i][j], travelMatrixTransit[i][j])/60, '.1f')))
                travelMatrixHandling.append(float(format(travelMatrixCar[i][j]/60, '.1f')))
        self.distancesC = travelMatrixHandling
        self.distancesB = travelMatrixNotHandling

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

        for j in range(len(self.pNodes)):
            self.pNodes[j].iState = int(round(float(sumPState) * (float(self.pNodes[j].iState) / sumIState)))

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
                    r2 = random.randint(0, len(self.pNodes) - 1)
                iStateList[r1] -= 1
                iStateList[r2] += 1

            for i in range(len(self.pNodes)):
                self.pNodes[i].iState = iStateList[i]

            moves = self.calculateMovesToIDeal()





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
        fileName = "../../Mosel/tests/" + str(example) + "_a.txt"
        if (os.path.exists(fileName)):
            fileName = "../../Mosel/tests/" + str(example) + "_b.txt"
            if (os.path.exists(fileName)):
                fileName = "../../Mosel/tests/" + str(example) + "_c.txt"
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
        f.write(string)
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


# PNODES
def createNodes(world):
    xRange = int(input("How many nodes on the X-axis: "))
    yRange = int(input("How many nodes on the Y-axis: "))
    for i in range(xRange):
        xCord = i
        for j in range(yRange):
            pState = random.randint(0, 3)
            cState = random.randint(0, 1)
            iState = random.randint(0, 4)
            demand = random.randint(-1, 1)
            yCord = j
            node = pNode(xCord, yCord, pState, cState, iState, demand)
            world.addNodes(node)
            world.addPNodes(node)
    world.addDim(xRange, yRange)

# ARTIFICIAL OPERATORS
def createFCCars(world, time, cNode, pNode):
    fc = fCCars(cNode, pNode, time)
    world.addfCCars(fc)

# CNODES
def createCNodes(world):
    string = "\n You can create a charging node in parking node 1 to: " + str(len(world.pNodes))
    print(string)
    numCNodes = int(input("How many do yoy want to create: "))
    for i in range(numCNodes):
        print("\n")
        print("Creating charging node: ", i+1)
        pNodeNum = int(input("Which parking node should it be located in: "))
        pNode = world.pNodes[pNodeNum-1]
        capacity = int(input("How many available charging slots right now: "))
        totalCapacity = int(input("What is the total capacity: "))
        fCCars = totalCapacity - capacity
        for j in range(fCCars):
            time = random.randint(0, 10)
            createFCCars(world, time, i + len(world.pNodes) + 1, pNodeNum)
        cN = cNode(pNode.xCord, pNode.yCord, capacity, fCCars, totalCapacity, pNodeNum)
        world.addcNodes(cN)
        world.addNodes(cN)

# OPERATORS
def createOperators(world):
    numOperators = int(input("\n What is the number of operators "))
    for i in range(numOperators):
        print("\n")
        print("Creating operator", i+1)
        startNode = random.randint(1, len(world.pNodes))
        time = int(input("What is the starting time: "))
        handling = int(input("Are they handling, 1 if yes: "))
        if(handling == 1):
            op = operator(startNode, time, True)
            world.addOperator(op)

        else:
            op = operator(startNode, time, False)
            world.addOperator(op)




def main():
    print("\n WELCOME TO THE EXAMPLE CREATOR \n")
    world = World()
    #world.setCordConstants((59.956751, 10.861843), (59.908674, 10.670612))
    world.setCordConstants((59.952483, 10.795069), (59.904574, 10.681527))
    createNodes(world)
    cords = []
    if(SPREAD):
        cords = world.giveRealCoordinatesSpread()
    createCNodes(world)
    createOperators(world)
    world.createRealIdeal()
    world.shuffleIdealState()
    print("DONE")
    world.setTimeConstants(4, 5, 60, 10, 30)
    if(len(world.pNodes) > 10):
        world.calculateDistances()
    else:
        world.calculateRealDistances(cords)
    world.calculateVisitList()
    world.calculateMovesListToIdeal()
    maxVisit = max(world.visitList)
    for i in range(len(MODES_RUN2)):
        world.setConstants(maxVisit, MODES_RUN2[i][0], 10)
        world.setCostConstants(MODES_RUN2[i][1], MODES_RUN2[i][2], 0.5, MODES_RUN2[i][3], MODES_RUN2[i][4])
        moves = world.calculateMovesToIDeal()
        #filepath = "test_" + str(world.YCORD) + "x" + str(world.XCORD) + "_" + str(len(world.operators)) + "so_" + str(len(world.cNodes)) + "c_" + str(moves) + "mov_" + str(i) + "MODE"
        filepath = "test_" + str(len(world.pNodes)) + "nodes_" + str(len(world.operators)) + "so_" + str(len(world.cNodes)) + "c_" + str(moves) + "mov_" + str(CARSCHARGING) + "charging_" + str(len(world.fCCars)) + "finishes_" + str(i) + "MODE"
        world.writeToFile(filepath)

main()

"""
string += "surplusList: ["
for i in range(len(self.surp)):
    string += str(self.surp[i])
    if (i < len(self.surp) - 1):
        string += " "
string += "] \n"
string += "deficitList: ["
for i in range(len(self.deficit)):
    string += str(self.deficit[i])
    if (i < len(self.deficit) - 1):
        string += " "
string += "] \n"
string += "allList: ["
for i in range(len(self.deficit)):
    string += str(self.deficit[i] + self.surp[i] + self.charg[i])
    if (i < len(self.deficit) - 1):
        string += " "
string += "] \n"
"""
# TODO: Create a new location template, so that zobnes scale equally
# TODO: More crontol paramters for generating boards with an equal amount of: Cars to be handled, cars to be charged, cars that finish charging


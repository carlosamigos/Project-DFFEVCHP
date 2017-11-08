import math
import random
import sys
import time

sys.path.append('../')
from Data_Retrieval import googleTrafficInformationRetriever as gI

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

        # TIME CONSTANTS #
        self.HANDLINGTIMEP = 0
        self.HANDLINGTIMEC = 0
        self.TIMELIMIT = 0
        self.TIMELIMITLAST = 0

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

    def calculateDistances(self):
        self.distancesC = []
        self.distancesB = []
        maxDistance = math.sqrt(math.pow(self.pNodes[0].xCord - self.pNodes[len(self.pNodes) - 1].xCord, 2) + math.pow(self.pNodes[0].yCord - self.pNodes[len(self.pNodes) - 1].yCord, 2))
        scale = float(format((self.TIMELIMIT + self.TIMELIMITLAST - 1)/(maxDistance*3), '.1f'))
        for x in range(len(self.nodes)):
            for y in range(len(self.nodes)):
                distance = math.pow(self.nodes[x].xCord - self.nodes[y].xCord, 2) + math.pow(self.nodes[x].yCord - self.nodes[y].yCord, 2)
                distanceSq = float(format(math.sqrt(distance)*scale, '.1f'))
                distanceB = float(format(distanceSq * 2, '.1f'))

                self.distancesC.append(distanceSq)
                self.distancesB.append(distanceB)

    def calculateRealDistances(self):
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

        for i in range(len(self.cNodes)):
            cords.append(cords[self.cNodes[i].pNode - 1])

        travelMatrixCar = gI.run(cords, "driving", False)
        time.sleep(2)
        travelMatrixBicycle = gI.run(cords, "bicycling", False)
        time.sleep(2)
        travelMatrixTransit = gI.run(cords, "transit", False)

        #print(travelMatrixTransit)
        #print(travelMatrixBicycle)
        #print(travelMatrixCar)

        travelMatrixNotHandling = []
        travelMatrixHandling = []
        for i in range(len(travelMatrixBicycle)):
            for j in range(len(travelMatrixBicycle[i])):
                travelMatrixNotHandling.append(float(format(min(travelMatrixBicycle[i][j], travelMatrixTransit[i][j])/60, '.1f')))
                travelMatrixHandling.append(float(format(travelMatrixCar[i][j]/60, '.1f')))
        self.distancesC = travelMatrixHandling
        self.distancesB = travelMatrixNotHandling



    def setConstants(self, visits, mode, sBigM):
        self.VISITS = visits
        self.MODE = mode
        self.SBIGM = sBigM

    def setCostConstants(self, costOfDev, costOfPos, costOfExtraT, costOfTravel):
        self.COSTOFDEV = costOfDev
        self.COSTOFPOS = costOfPos
        self.COSTOFEXTRAT = costOfExtraT
        self.COSTOFTRAVEL = costOfTravel

    def setTimeConstants(self, handlingTimeP, handlingTimeC, timeLimit, timeLimitLast):
        self.HANDLINGTIMEP = handlingTimeP
        self.HANDLINGTIMEC = handlingTimeC
        self.TIMELIMIT = timeLimit
        self.TIMELIMITLAST = timeLimitLast

    def setCordConstants(self, upperRight, lowerLeft):
        self.UPPERRIGHT = upperRight
        self.LOWERLEFT = lowerLeft


    def writeToFile(self, example):
        fileName = "../../Mosel/states/initialExample" + str(example) + ".txt"
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
        fCCars = int(input("How many cars are charging there now, that will finish during the planning period: "))
        for j in range(fCCars):
            time = random.randint(0, 59)
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
    createNodes(world)
    createCNodes(world)
    createOperators(world)
    world.setCordConstants((59.956751, 10.861843), (59.908674, 10.670612))
    world.setConstants(5, 1, 10)
    world.setCostConstants(20, 20, 1, 1)
    world.setTimeConstants(4, 5, 60, 10)
    world.calculateDistances()
    world.calculateRealDistances()
    world.writeToFile(1)

main()





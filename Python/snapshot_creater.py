#snapshot creator from mosel outputs
import copy

def readPaths(filename):
    fil = open(filename,"r")
    paths = {}
    for line in fil:
        line = line.split(": ")
        operatorId = int(line[0]) - 1
        path = []
        for visit in line[1].split("),("):
            visit = visit.strip()
            visit = visit.strip("(")
            visit = visit.strip(")")
            visit = stringListToIntAndFloatList(visit.split(","))
            visitDict = {}
            visitDict["node"] = visit[0]-1
            visitDict["visit"] = visit[1]
            visitDict["handling"] = visit[2]
            visitDict["time"] = visit[3]
            path.append(visitDict)
        paths[operatorId] = path
    fil.close()
    return paths

def readExampleFile(filename):
    inputProblem = {}
    fil = open(filename,"r")
    vehicleCounter = 0
    checkingVehicle = False
    bikeCounter = 0
    checkingBike = False
    totalNumberOfRealNodes = 0
    travelTimeVehicleMatrix = []
    travelTimeBikeMatrix = []
    for line in fil:
        if(vehicleCounter == totalNumberOfRealNodes):
            checkingVehicle = False
            vehicleCounter = 0
            inputProblem["travelTimeVehicle"] = travelTimeVehicleMatrix
        if(bikeCounter == totalNumberOfRealNodes):
            checkingBike = False
            vehicleCounter = 0
            inputProblem["travelTimeBike"] = travelTimeBikeMatrix
        if(checkingVehicle):
            vehicleCounter += 1
            array = makeLineListStringToArray(line)
            travelTimeVehicleMatrix.append(array)
        elif( checkingBike ): 
            bikeCounter += 1
            array = makeLineListStringToArray(line)
            travelTimeBikeMatrix.append(array)
        if "numROperators" in line:
            line = line.strip("numROperators : ")
            inputProblem["numROperators"] = int(line)
        elif "numAOperators" in line:
            line = line.strip("numAOperators : ")
            inputProblem["numAOperators"] = int(line)
        elif "numPNodes" in line:
            line = line.strip("numPNodes : ")
            inputProblem["numPNodes"] = int(line)
        elif "numCNodes" in line:
            line = line.strip("numCNodes : ")
            inputProblem["numCNodes"] = int(line)
        elif "travelTimeVehicle" in line:
            totalNumberOfRealNodes = inputProblem["numCNodes"] + inputProblem["numPNodes"]
            checkingVehicle = True
            array = makeLineListStringToArray(line)
            travelTimeVehicleMatrix.append(array)
            vehicleCounter+=1
        elif "travelTimeBike" in line:
            totalNumberOfRealNodes = inputProblem["numCNodes"] + inputProblem["numPNodes"]
            checkingBike = True
            array = makeLineListStringToArray(line)
            travelTimeBikeMatrix.append(array)
            bikeCounter+=1
        elif "initialRegularInP" in line:
            line = line.strip("initialRegularInP : [")
            line = line.strip()
            line = line.strip("]")
            array = line.split()
            inputProblem["initialRegularInP"] = list(map(int, array))
        elif "initialInNeedP" in line:
            line = line.strip("initialInNeedP : [")
            line = line.strip()
            line = line.strip("]")
            array = line.split()
            inputProblem["initialInNeedP"] = list(map(int, array))
        elif "finishedDuringC" in line:
            line = line.strip("finishedDuringC : [")
            line = line.strip()
            line = line.strip("]")
            array = line.split()
            inputProblem["finishedDuringC"] = list(map(int, array))
        elif "travelTimeToOriginR" in line:
            line = line.strip("travelTimeToOriginR : [")
            line = line.strip()
            line = line.strip("]")
            array = line.split()
            inputProblem["travelTimeToOriginR"] = list(map(float, array))
        elif "travelTimeToParkingA" in line:
            line = line.strip("travelTimeToParkingA : [")
            line = line.strip()
            line = line.strip("]")
            array = line.split()
            inputProblem["travelTimeToParkingA"] = list(map(float, array))
    fil.close()
    
    rOperators = inputProblem["numROperators"]
    originNodes = list(range(totalNumberOfRealNodes,totalNumberOfRealNodes+rOperators))
    inputProblem["originNodes"] = originNodes
    destinationNodes = list(range(totalNumberOfRealNodes+rOperators,totalNumberOfRealNodes+rOperators*2))
    inputProblem["destinationNodes"] = destinationNodes


    return inputProblem

def makeLineListStringToArray(string):
    string = string.strip("travelTimeVehicle : [")
    string = string.strip("travelTimeBike    : [")
    string = string.strip()
    string = string.strip("]")
    array = string.split(" ")
    return list(map(float, array))

def stringListToIntList(array):
    newList = []
    for i in range(len(array)):
        element = array[i]
        newList.append(int(element))
    return newList

def stringListToIntAndFloatList(array):
    newList = []
    for i in range(len(array)):
        element = array[i]
        if(i == len(array)-1):
            newList.append(float(element))  
        else:
            newList.append(int(element))
    return newList

def stringListToFloatList(array):
    newList = []
    for i in range(len(array)):
        element = array[i]
        newList.append(float(element))
    return newList

def addNodesZeroIndexed(inputProblem):
    numPNodes = inputProblem["numPNodes"]
    numCNodes = inputProblem["numCNodes"]
    nodes = list(range(numCNodes+numPNodes))
    pNodes = list(range(numPNodes))
    cNodes = list(range(numPNodes+1,numPNodes+1 + numCNodes))
    inputProblem["nodes"] = nodes
    inputProblem["pNodes"] = pNodes
    inputProblem["cNodes"] = cNodes
    return inputProblem

def findOperatorStates(time,inputProblem,realOperatorsPaths):
    operators = []
    for operator in range(inputProblem["numROperators"]):
        path = realOperatorsPaths[operator]
        prevNode = path[0]
        operatorSnapshot = {}
        didFindRoomForOperator = False
        for nodeIndex in range(1,len(path)-1):
            node = path[nodeIndex]
            if(prevNode["time"] <= time and time <= node["time"]):
                handling = node["handling"]
                travelTime = findTravelTimeBetweenNodeAandB(prevNode,node,operator,handling,inputProblem)
                prevTime = prevNode["time"]
                thisTime = node["time"]
                toNode = None
                fromNode = prevNode["node"]
                remainingTime = 0
                covered = 0
                if(thisTime == time):
                    fromNode = node["node"]
                    toNode = node["node"]
                elif(thisTime - travelTime <= time): 
                    #oeprator is travelling
                    toNode = node["node"]
                    remainingTime = thisTime - time
                    if travelTime != 0:
                        covered = 1.0-float(remainingTime) / travelTime
                    else:
                        covered = 1.0
                else:
                    toNode = fromNode
                operatorSnapshot["id"] = operator
                operatorSnapshot["to"] = toNode
                operatorSnapshot["from"] = fromNode
                operatorSnapshot["covered"] = covered
                operatorSnapshot["handling"] = node["handling"]
                operatorSnapshot["remaining_time"] = remainingTime
                didFindRoomForOperator = True
                break
            prevNode = node

        if not didFindRoomForOperator:
            secondLastNode = path[-2]
            nodeId = secondLastNode["node"]
            operatorSnapshot["id"] = operator
            operatorSnapshot["to"] = nodeId
            operatorSnapshot["from"] = nodeId
            operatorSnapshot["covered"] = 0
            operatorSnapshot["handling"] = 0
            operatorSnapshot["remaining_time"] = 0
        operators.append(operatorSnapshot)
    return operators


def addStates(time,inputProblem,realOperatorsPaths,artificialPaths):
    carsParked = copy.deepcopy(inputProblem["initialRegularInP"])
    carsInNeed = copy.deepcopy(inputProblem["initialInNeedP"])
    carsCharging = copy.deepcopy(inputProblem["finishedDuringC"])
    numberOfParkingNodes = len(carsParked)
    parkingNodes = inputProblem["pNodes"]
    chargingNodes = inputProblem["cNodes"]
    for operator in range(inputProblem["numROperators"]):
        path = realOperatorsPaths[operator]
        for pathIndex in range(1,len(path)-1):
            node = path[pathIndex]
            isParkingNode = node["node"] in parkingNodes
            isHandling = node["handling"]

            if time < node["time"]:
                if isHandling:
                    prevNode = path[pathIndex-1]  
                    if pathIndex!=1:
                        isTravelling = isOperatorTravellingFromNodeToNode(prevNode,node,time,inputProblem["travelTimeVehicle"][prevNode["node"]][node["node"]])
                        if isTravelling and isParkingNode:
                            #print(operator,prevNode["node"],node["node"],time,isHandling,isTravelling)
                            #None
                            carsParked[prevNode["node"]] -=1
                        if isTravelling and not isParkingNode:
                            carsInNeed[prevNode["node"]] -=1
                break
            else:
                #print(node,isHandling)
                if isHandling and pathIndex == 1:
                    
                    if isParkingNode:
                        carsParked[node["node"]] +=1
                    else:
                        carsCharging[node["node"]-numberOfParkingNodes] +=1
                elif isHandling:
                    prevNode = path[pathIndex-1]
                    isTravelling = isOperatorTravellingFromNodeToNode(prevNode,node,time,inputProblem["travelTimeVehicle"][prevNode["node"]][node["node"]])
                    if isParkingNode:
                        #print(isTravelling)
                        if(not isTravelling):
                            carsParked[node["node"]] +=1
                        carsParked[prevNode["node"]] -=1
                    else:
                        
                        carsCharging[node["node"]-numberOfParkingNodes] +=1
                        carsInNeed[prevNode["node"]] -=1

    for aOperator in range(inputProblem["numAOperators"]):
        path = artificialPaths[aOperator]
        chargingNode = path[0]
        parkingNode = path[1]
        if time >= parkingNode["time"]:
            carsCharging[chargingNode["node"]-numberOfParkingNodes] -=1
            carsParked[parkingNode["node"]] +=1
    return carsParked, carsInNeed, carsCharging


def isOperatorTravellingFromNodeToNode(fromNode,toNode,time, travelTime):
    return (toNode["time"] - travelTime < time) and time > fromNode["time"] and time < toNode["time"]

def createSnapshotFromTime(time,inputProblem,realOperatorsPaths,artificialPaths):
    snapshot = {}
    carsParked,carsInNeed, carsCharging = addStates(time,inputProblem,realOperatorsPaths,artificialPaths)
    snapshot["cars_parked"] = carsParked
    snapshot["cars_need"] = carsInNeed
    snapshot["charging"] = carsCharging
    operators = findOperatorStates(time,inputProblem,realOperatorsPaths)
    snapshot["operators"] = operators
    return snapshot

def findTravelTimeBetweenNodeAandB(From,To,operator,handling,inputProblem):
    realNodes = inputProblem["nodes"]
    fromIndex = From["node"]
    toIndex = To["node"]
    travelTime = 0
    if(fromIndex in realNodes and toIndex in realNodes):
        if handling:
            travelTime = inputProblem["travelTimeVehicle"][fromIndex][toIndex]
        else:
            travelTime = inputProblem["travelTimeBike"][fromIndex][toIndex]
    elif(fromIndex in inputProblem["originNodes"]):
        travelTime = inputProblem["travelTimeToOriginR"][operator]
    return travelTime

def main():
    pathFileName = "../Mosel/outputServiceOperatorsPath.txt"
    artificialFilename = "../Mosel/outputArtificialServiceOperators.txt"
    exampleFileName = "../Mosel/examples/example1.txt"
    inputProblem = addNodesZeroIndexed(readExampleFile(exampleFileName))
    realOperatorsPaths = readPaths(pathFileName)
    artificialPaths = readPaths(artificialFilename)
    maxTime = 5
    steps = 30
    snapshots = []
    stepLength = float(maxTime) / steps
    for step in range(0,steps+1):
        t = step*stepLength
        snapshot = createSnapshotFromTime(t,inputProblem,realOperatorsPaths,artificialPaths)
        snapshots.append(snapshot)
        #print(step,t,snapshot,"\n")
    return snapshots
#main()


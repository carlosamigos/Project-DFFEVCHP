#snapshot creator from mosel outputs

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

def createSnapshotFromTime(time,inputProblem,realOperatorsPaths,artificialPaths):
    snapshot = {}
    snapshot["cars_parked"] = inputProblem["initialRegularInP"]
    snapshot["cars_need"] = inputProblem["initialInNeedP"]
    snapshot["charging"] = inputProblem["finishedDuringC"]
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

    snapshots = []
    maxTime = 5
    steps = 10
    stepLength = float(maxTime) / steps
    for step in range(0,steps+1):
        t = step*stepLength
        snapshot = {}
        snapshot["cars_parked"] = [1,0,1,0,0,1]
        snapshot["cars_need"] = [0,1,0,0,1,0]
        snapshot["charging"] = [1,0]
        operators = createSnapshotFromTime(t,inputProblem,realOperatorsPaths,artificialPaths)
        snapshot["operators"] = operators
        snapshots.append(snapshot)
    return snapshots


main()

# -*- coding: utf-8 -*-
import simplejson, urllib.request as urlReq

def calculateTravelTimeMatrixFromCoordVector(coordVector, transportType, apikey, writeToFile):
    # max 10 coordinates
    # transport by default is car
    maxCoor = 10
    numberOfCoordinates = len(coordVector)
    secondMatrix = [[0 for i in range(numberOfCoordinates)] for j in range(numberOfCoordinates)]
    numberOf10Sets = int(numberOfCoordinates / maxCoor)
    originLists = []
    destinationLists = []
    coordinatesUsed = [[0 for i in range(numberOfCoordinates)] for j in range(numberOfCoordinates)]

    for i in range(0, numberOfCoordinates, maxCoor):
        originLists.append(coordVector[i:i + maxCoor])
        destinationLists.append(coordVector[i:i + maxCoor])

    for o in range(len(originLists)):
        for d in range(len(destinationLists)):
            origins = originLists[o]
            destinations = destinationLists[d]
            originString = makeStringListFromCoordinateVector(origins)
            destinationString = makeStringListFromCoordinateVector(destinations)

            url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + originString + "&destinations=" + destinationString + "&key=" + apikey + "&mode=" + transportType
            response = urlReq.urlopen(url)
            data = simplejson.load(response)
            if ("error_message" in data.keys()):
                print(data["error_message"])
                return secondMatrix

            # print(data.encode("ascii"))
            for i in range(len(data["rows"])):
                # print(data["rows"][i])
                dataDictList = data["rows"][i]["elements"]
                for j in range(len(dataDictList)):
                    seconds = dataDictList[j]["duration"]["value"]
                    firstCoor = maxCoor * o + i
                    secondCoor = maxCoor * d + j
                    secondMatrix[firstCoor][secondCoor] = seconds
                    coordinatesUsed[firstCoor][secondCoor] += 1
    if (writeToFile):
        writeMatrixToFile(secondMatrix, transportType)
    return secondMatrix


def writeMatrixToFile(matrix, transportType):
    if transportType == "":
        transportType = "car"
    fil = open("travelTimes_" + transportType + ".txt", "w")
    writeString = ""
    for row in matrix:
        for elem in row:
            writeString += str(elem) + " "
        writeString = writeString[:-1] + "\n"
    fil.write(writeString[:-1])
    fil.close()


def makeStringListFromCoordinateVector(coordVector):
    ret = ""
    for coor in coordVector:
        ret += str(coor[0]) + "," + str(coor[1]) + "|"
    ret = ret[:-1]
    return ret


def test():
    coordVector = [(63.427057, 10.3925251), (63.4222027, 10.3955179), (63.4367, 10.3988199), (63.4188848, 10.4044),
                   (63.4225, 10.431944)]
    print("number of coordinates", len(coordVector))
    transportType = "transit"
    apikey = "AIzaSyBMQAmCiWBwO1VznaTzEiNAEyoAUr2xzGM"
    matrix = calculateTravelTimeMatrixFromCoordVector(coordVector, transportType, apikey, True)
    print(matrix)

def run(coordVector, transportType, writeToFile):
    apikey = "AIzaSyBK2KN_jQS6ygFfU1UaMjG8CA8vhjeV10k"
    return calculateTravelTimeMatrixFromCoordVector(coordVector, transportType, apikey, writeToFile)


#test()

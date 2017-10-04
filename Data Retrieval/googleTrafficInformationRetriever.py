#-*- coding: utf-8 -*-
import simplejson, urllib.request as urlReq



def calculateTravelTimeMatrixFromCoordVector(coordVector, transportType = "",apikey):
	# max 10 coordinates
	# transport by default is car
	maxCoor = 10
	numberOfCoordinates = len(coordVector)
	secondMatrix = [[0 for i in range(numberOfCoordinates)] for j in range(numberOfCoordinates)]
	numberOf10Sets = int(numberOfCoordinates / maxCoor)
	originLists = []
	destinationLists = []
	coordinatesUsed = [[0 for i in range(numberOfCoordinates)] for j in range(numberOfCoordinates)]

	for i in range(0,numberOfCoordinates,maxCoor):
		originLists.append(coordVector[i:i+maxCoor])
		destinationLists.append(coordVector[i:i+maxCoor])
	
	for o in range(len(originLists)):
		for d in range(len(destinationLists)):
			origins = originLists[o]
			destinations = destinationLists[d]
			originString = makeStringListFromCoordinateVector(origins)
			destinationString = makeStringListFromCoordinateVector(destinations)			
			
			url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+originString+"&destinations="+destinationString+"&key="+apikey+"&mode="+transportType
			response = urlReq.urlopen(url)
			data = simplejson.load(response)
			if("error_message" in data.keys()):
				print(data["error_message"])
				return secondMatrix

			#print(data.encode("ascii"))
			for i in range(len(data["rows"])):
				#print(data["rows"][i])
				dataDictList = data["rows"][i]["elements"]
				for j in range(len(dataDictList)):
					seconds = dataDictList[j]["duration"]["value"]
					firstCoor = maxCoor*o + i
					secondCoor = maxCoor*d + j
					secondMatrix[firstCoor][secondCoor] = seconds
					coordinatesUsed[firstCoor][secondCoor]+=1
	return secondMatrix


def makeStringListFromCoordinateVector(coordVector):
	ret = ""
	for coor in coordVector:
		ret+= str(coor[0])+","+str(coor[1])+"|"
	ret = ret[:-2]
	return ret


def test():

	coordVector = [(63.427057, 10.3925251),(63.4222027, 10.3955179),(63.4367, 10.3988199),(63.4188848, 10.4044),(63.4225, 10.431944)]
	print("number of coordinates",len(coordVector))
	transportType = "bicycling"
	apikey = "AIzaSyBMQAmCiWBwO1VznaTzEiNAEyoAUr2xzGM"
	matrix = calculateTravelTimeMatrixFromCoordVector(coordVector,transportType,apikey)
	print(matrix)




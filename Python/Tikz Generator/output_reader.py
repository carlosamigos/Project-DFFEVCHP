from setup import input_file
general_info = {}

with open(input_file, 'r') as f: 
    for line in f:
        if len(line.strip()) == 0 or ":" not in line:
            continue
        info = line.split(":")
        key = info[0].strip()
        data = info[1].strip()

        if "[" in data and "]" in data and len(data) == 2:
            data = []

        elif ("[" in data and data[-1] != "]") :
            continue
        elif "[" in data:
            data = data.replace("[", "").replace("]", "")
            data = data.split()
            if "." in data[0]:
                data = list(map(float, data))
            else:
                data = list(map(int, data))

        elif "." in data:
            data = float(data)
        else:
            data = int(data)

        general_info[key] = data

general_info["numNodes"] = general_info["numPNodes"] + general_info["numCNodes"]
general_info["numOperators"] = general_info["numROperators"]
general_info["idealState"] = general_info["idealStateP"]

general_info["cToP"] = list(map(lambda x: x - 1, general_info["cToP"]))

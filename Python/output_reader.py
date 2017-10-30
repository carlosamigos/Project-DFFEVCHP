general_info = {}

with open("../Mosel/general_info.txt", 'r') as f: 
    for line in f:
        info = line.split(":")
        key = info[0].strip()
        data = info[1].strip()

        if "[" in data:
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

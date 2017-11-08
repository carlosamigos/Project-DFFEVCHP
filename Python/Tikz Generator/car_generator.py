from output_reader import general_info
from helpers import *
from setup import size_factor

step = 10*size_factor
columns = general_info["wNodes"]
rows = general_info["hNodes"]
width = columns*step
height = rows*step
car_height = size_factor*(step / 14)
man_height = size_factor*(step / 10)
car_dist_y = car_height / 6
car_dist_x = 3*car_dist_y
car_height_string = "{:.2f}".format(car_height) + "cm"
car_dist_y_string = "{:.2f}".format(car_dist_y) + "cm"
man_height_string = "{:.2f}".format(man_height) + "cm"

c_to_p = general_info["cToP"]
n_nodes = general_info["numNodes"]
c_nodes = general_info["numCNodes"]

def filler(id_string, color, number):
    s = ""
    for car_number in range(1, number):
        s += draw_object(color, id_string + str(car_number), 0, ["above", id_string + str(car_number-1)])

    return s

def fill_parking(node, cars_parked, cars_need):
    s = ""
    x_above = x_right_from_node(node, step, columns) - car_dist_x*3
    y = y_bottom_from_node(node, step, columns, rows) + 4*car_dist_y

    if cars_parked > 0:
        above_string = str(node) + "_above_"
        s += draw_object("car_green", above_string + "0", [x_above, y])
        if cars_parked > 1:
            s += filler(above_string, "car_green", cars_parked)

    if cars_parked != 0:
        x_below = x_above - car_dist_x*6
    else:
        x_below = x_above

    
    if cars_need > 0:
        below_string = str(node) + "_below_"
        s += draw_object("car_red", below_string + "0", [x_below, y])
        if cars_need > 1:
            s += filler(below_string, "car_red", cars_need)

    return s

def fill_charging(c_node, cars_charging):
    s = ""
    if cars_charging > 0:
        charging_string = str(c_node) + "_charging_"
        p_node = c_to_p[c_node]
        x = x_left_from_node(p_node, step, columns) + 3*car_dist_x
        y = y_bottom_from_node(p_node, step, columns, rows) + 4*car_dist_y
        s += draw_object("car_orange", charging_string + "0", [x,y])
        if cars_charging > 1:
            s += filler(charging_string, "car_orange", cars_charging)

    return s

def fill_origin_list(operator_info):
    s = ""
    counter = 0

    while counter < len(operator_info):
        time = operator_info[counter]["remaining_time"]

        if time <= 0:
            counter += 1
            continue
        operator_id = operator_info[counter]["id"]
        handling = operator_info[counter]["handling"]
        to_node = operator_info[counter]["to"]
        if to_node >= n_nodes:
            to_node = c_to_p[to_node - n_nodes]
        if handling == 1:
            obj = "car_black"
            text_dist = "0.2cm"
        else:
            obj = "bike"
            text_dist = "0.55cm"
        relative_string = "[below = 1.2cm of sidebar_time_to_header.west, anchor=west]"
        s += "    \\node" + relative_string + " (operator_" + str(operator_id) + ") {\\includegraphics[height=" + car_height_string + "]{\"tex/img/" + obj + "\".png}};\n"
        #s += "    \\node[above left = -0.2cm of operator_" + str(operator_id) + "] {" + str(operator_id) + "};\n"
        s += "    \\node[right =" + text_dist + " of operator_" + str(operator_id) + "] (operator_string_" + str(operator_id) +\
                ") {" + "{:.2f}".format(time) + " time units to node " + str(to_node) + "};\n"

        counter += 1
        prev_operator = operator_id
        if len(operator_info) - counter >= 1:
            for i in range(counter, len(operator_info)):
                operator = operator_info[i]
                operator_id = operator["id"]
                handling = operator["handling"]
                time = operator_info[i]["remaining_time"]
                to_node = operator_info[i]["to"]
                if to_node >= n_nodes:
                    to_node = c_to_p[to_node - n_nodes]
                if handling == 1:
                    obj = "car_black"
                else:
                    obj = "bike"

                relative_string = "[below =1.2cm of operator_" + str(prev_operator) + ".west, anchor=west]"
                s += "    \\node" + relative_string + " (operator_" + str(operator_id) + ")"+\
                        "{\\includegraphics[height=" + car_height_string + "]{\"tex/img/" + obj + "\".png}};\n"
                #s += "    \\node[above left = -0.35cm of operator_" + str(operator_id) + "] {" + str(operator_id) + "};\n"
                s += "    \\node at (operator_string_" + str(prev_operator) + " |- operator_" + str(operator_id) + ")"+\
                        "{" + "{:.2f}".format(time) + " time units to node " + str(to_node) + "};\n"
                prev_operator = operator_id
        break
    return s


def draw_object(obj, identifier, position=0, relative=""):
    if position != 0:
        s = "    \\node at (" + "{:.2f}".format(position[0]) + "," + "{:.2f}".format(position[1]) + ") (" + identifier + ") \
                {\\includegraphics[height=" + car_height_string + "]{\"tex/img/" + obj + "\".png}};\n"
    else:
        relative_string = "[" + relative[0] + " = " + car_dist_y_string + " of " + relative[1] + "]"
        s = "    \\node" + relative_string + " (" + identifier + ") {\\includegraphics[height=" + car_height_string + "]{\"tex/img/" + obj + "\".png}};\n"

    return s

def fill_node_with_operators(node, operators_in_node):
    s = ""
    obj = "man"
    x = "{:.2f}".format(x_mid_from_node(node, step, columns))
    y = "{:.2f}".format(y_mid_from_node(node, step, columns, rows))
    relative = [("left", "1.2cm"), ("right", "1.2cm"), ("above", "1.2cm"), ("below", "1.2cm")]

    if len(operators_in_node) > 0:
        operator_id = operators_in_node[0]["id"]
        s += "    \\node at (" + x + "," + y + ") (operator_" + str(operator_id) + ")"+\
                " {\\includegraphics[height=" + man_height_string + "]{\"tex/img/" + obj + "\".png}};\n"
        s += "    \\node[above left = -0.35cm of operator_" + str(operator_id) + "] {" + str(operator_id) + "};\n"
        prev_operator = operator_id

        if len(operators_in_node) > 1:
            rel_counter = 0
            for i in range(1, len(operators_in_node)):
                operator = operators_in_node[i]
                operator_id = operator["id"]
                relative_string = "[" + relative[rel_counter][0] + " = " + relative[rel_counter][1] +\
                        " of operator_" + str(prev_operator) + "]"
                s += "    \\node" + relative_string + " (operator_" + str(operator_id) + ")"+\
                        "{\\includegraphics[height=" + man_height_string + "]{\"tex/img/" + obj + "\".png}};\n"
                s += "    \\node[above left = -0.35cm of operator_" + str(operator_id) + "] {" + str(operator_id) + "};\n"
                rel_counter += 1
    return s


def draw_moving_object(from_node, to_node, covered, operator, obj):
    if to_node >= n_nodes:
        to_node = c_to_p[to_node - n_nodes]

    from_x = x_mid_from_node(from_node, step, columns)
    from_y = y_mid_from_node(from_node, step, columns, rows)


    height = car_height_string

    to_x = x_mid_from_node(to_node, step, columns)
    to_y = y_mid_from_node(to_node, step, columns, rows)

    delta_x = from_x - to_x
    delta_y = from_y - to_y

    new_x = from_x - delta_x * covered
    new_y = from_y - delta_y * covered

    from_x_string = "{:.2f}".format(from_x)
    from_y_string = "{:.2f}".format(from_y)
    to_x_string = "{:.2f}".format(to_x)
    to_y_string = "{:.2f}".format(to_y)
    x_string = "{:.2f}".format(new_x)
    y_string = "{:.2f}".format(new_y)

    s = "    \\node at (" + x_string + "," + y_string + ") (operator_" + str(operator) + ") \
            {\\includegraphics[height=" + height + "]{\"tex/img/" + obj + "\".png}};\n"
    s += "    \\draw[->, ultra thick] (operator_" + str(operator) + ".center) -- (" + to_x_string + "," + to_y_string + ");\n"
    s += "    \\draw[ultra thick, dashed] (" + from_x_string + "," + from_y_string + ") -- (operator_" + str(operator) + ".center);\n"
    s += "    \\node[above left = -0.35cm of operator_" + str(operator) + "] {" + str(operator) + "};\n"
    return s

        

def draw_object_below(obj, x, identifier):
    if obj == "bike" or "car" in obj:
        height = car_height_string
    else:
        height = man_height_string

    relative_string = "[below = " + man_height_string + " of " + x + ".west, anchor=west]"
    s = "    \\node" + relative_string + " (" + str(identifier) + ")"+\
            "{\\includegraphics[height=" + height + "]{\"tex/img/" + obj + "\".png}};\n"
    return s

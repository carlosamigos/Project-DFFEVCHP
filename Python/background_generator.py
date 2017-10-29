from output_reader import general_info
from car_generator import draw_object
from helpers import x_left_from_node, y_top_from_node, row_from_node

step = 10
columns = general_info["w_nodes"]
rows = general_info["h_nodes"]
width = columns*step
height = rows*step

sign_width = step / 7
sign_dist = step / 10
path_options = "ultra thin"

def draw_parking():
    width_string = "{:.2f}".format(width)
    height_string = "{:.2f}".format(height)
    s = "   \\draw[step=" + str(step) + ", thick] (0,0) grid (" + width_string + "," + height_string + ");\n"

    for node in range(general_info["n_nodes"]):
        x = x_left_from_node(node, step, columns) + sign_dist
        y = y_top_from_node(node, step, columns) - sign_dist
        s += "    \\node at (" + "{:.2f}".format(x) + "," + "{:.2f}".format(y) + ") (p_sign_" + str(node) + ") {\\includegraphics[width=" +\
                "{:.2f}".format(sign_width) + "cm]{\"tex/img/psign\".pdf}};"

    return s


def draw_charging():
    s = ""

    for node in range(general_info["n_c_nodes"]):
        p_node = general_info["c_to_p"][node]
        s += "    \\node[right = " + "{:.2f}".format(sign_dist) + "pt of p_sign_" +  str(p_node) + "] {\\includegraphics[width=" +\
                "{:.2f}".format(sign_width) + "cm]{\"tex/img/csign\".pdf}};"

    return s

def draw_sidebar():
    s = ""
    x = - step/1.9
    y = height - 1

    s += "    \\node at (" + "{:.2f}".format(x) + "," + "{:.2f}".format(y) + ") (sidebar_header) {\\Huge Timestep: };\n"
    s += "    \\node[below = 1.5cm of sidebar_header.west, anchor=west] (sidebar_time_to_header) {\\Huge\\underline{Time to origin}};\n"

    return s


def draw_legend():
    s = ""

    legend_width = width/3.6
    legend_height = legend_width/1.8

    left_x = width - legend_width
    right_x = width
    top_y = -.5
    bottom_y = - (legend_height - .5)
    
    right_x_string = "{:.2f}".format(right_x)
    left_x_string = "{:.2f}".format(left_x)
    top_y_string = "{:.2f}".format(top_y)
    bottom_y_string = "{:.2f}".format(bottom_y)

    s = "   \\path[" + path_options + "] (" + left_x_string + "," + top_y_string + ") edge (" +\
            left_x_string + "," + bottom_y_string + ")\n       (" + left_x_string + "," + bottom_y_string + ") edge ("+\
            right_x_string + "," + bottom_y_string +")\n       (" + right_x_string + "," + bottom_y_string + ") edge ("+\
            right_x_string + "," + top_y_string + ")\n       (" + right_x_string + "," + top_y_string + ") edge ("+\
            left_x_string + "," + top_y_string + ");\n"

    s += draw_object("car_green", "legend_green", [left_x + 1.2, top_y - .8])
    s += draw_object("car_orange", "legend_orange", 0, ["below", "legend_green"])
    s += draw_object("car_red", "legend_red", 0, ["below", "legend_orange"])


    s += "    \\node[right = .2cm of legend_green] {Cars above battery threshold};\n"
    s += "    \\node[right = .2cm of legend_orange] {Cars currently charging};\n" 
    s += "    \\node[right = .2cm of legend_red] {Cars below battery threshold};\n"

    return s




def draw_nodes():
    s = ""
    s += draw_parking()
    s += draw_charging()
    s += draw_legend()
    s += draw_sidebar()
    return s


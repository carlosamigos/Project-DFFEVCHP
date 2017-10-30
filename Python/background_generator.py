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
                "{:.2f}".format(sign_width) + "cm]{\"tex/img/psign\".pdf}};\n"

    return s


def draw_charging():
    s = ""

    for node in range(general_info["n_c_nodes"]):
        p_node = general_info["c_to_p"][node]
        s += "    \\node[right = " + "{:.2f}".format(sign_dist) + "pt of p_sign_" +  str(p_node) + "] {\\includegraphics[width=" +\
                "{:.2f}".format(sign_width) + "cm]{\"tex/img/csign\".pdf}};\n"

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

    legend_width = width/2
    legend_height = width/5

    left_x = "{:.2f}".format(0)
    right_x = "{:.2f}".format(legend_width)
    top_y = "{:.2f}".format(-0.5)
    bottom_y = "{:.2f}".format(- (legend_height - .5))
    
    s += "    \\node at (" + left_x + "," + top_y + ") (legend_left_top) {};\n"
    s += "    \\node at (" + left_x + "," + bottom_y + ") (legend_left_bottom) {};\n"
    s += "    \\node at (" + right_x + "," + bottom_y + ") (legend_right_bottom) {};\n"
    s += "    \\node at (" + right_x + "," + top_y + ") (legend_right_top) {};\n"
    s += "    \\path[" + path_options + "] (legend_left_top.center) edge (legend_left_bottom.center);\n"
    s += "    \\path[" + path_options + "] (legend_left_bottom.center) edge (legend_right_bottom.center);\n"
    s += "    \\path[" + path_options + "] (legend_right_bottom.center) edge (legend_right_top.center);\n"
    s += "    \\path[" + path_options + "] (legend_right_top.center) edge (legend_left_top.center);\n"

    s += draw_object("car_green", "legend_green", 0, ["below right", "legend_left_top"])
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


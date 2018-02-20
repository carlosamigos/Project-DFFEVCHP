from output_reader import general_info
from car_generator import draw_object, draw_object_below, car_height_string, car_height
from helpers import x_left_from_node, y_top_from_node, row_from_node, x_right_from_node, y_bottom_from_node
from setup import size_factor

step = 10*size_factor
columns = general_info["wNodes"]
rows = general_info["hNodes"]
width = columns*step
height = rows*step

sign_width = "{:.2f}".format(size_factor*(step / 10))
sign_dist = step / 10
corner_triangle_dist = step / 9
path_options = "thick"

def draw_parking(ideal_state):
    width_string = "{:.2f}".format(width)
    height_string = "{:.2f}".format(height)
    s = "   \\draw[step=" + str(step) + ", thick] (0,0) grid (" + width_string + "," + height_string + ");\n"

    for node in range(general_info["numPNodes"]):
        x = x_left_from_node(node, step, columns) + sign_dist
        y = y_top_from_node(node, step, columns, rows) - sign_dist
        s += "    \\node at (" + "{:.2f}".format(x) + "," + "{:.2f}".format(y) + ") (p_sign_" + str(node) + ") {\\includegraphics[width=" +\
                sign_width + "cm]{\"tex/img/psign\".pdf}};\n"

        x2 = x_right_from_node(node, step, columns)
        y2 = y_top_from_node(node, step, columns, rows)
        s += "    \\node at (" + "{:.2f}".format(x2) + "," + "{:.2f}".format(y2) + ") (right_corner_" + str(node) + ") {};\n"
        #s += "    \\node[below left = 1mm of right_corner_" + str(node) + "] (ideal_png_" + str(node) + ") {\\includegraphics[height=" +\
        #        car_height_string + "]{\"tex/img/car_green\".png}};\n"
        s += "    \\node[below left = 1mm of right_corner_" + str(node) + "] (ideal_text_" + str(node) + ") {Ideal: " + str(ideal_state[node]) + "};\n"

        y3 = y_bottom_from_node(node, step, columns, rows)
        x -= sign_dist
        x3 = x + corner_triangle_dist
        y4 = y3 + corner_triangle_dist

        x4 = "{:.2f}".format(x + corner_triangle_dist/2.0)
        y5 = "{:.2f}".format(y3 + corner_triangle_dist/2.0)
        s += "    \\draw[thick] (" + "{:.2f}".format(x) + "," + "{:.2f}".format(y4) + ") -- (" + "{:.2f}".format(x3) + "," + "{:.2f}".format(y4) + ");\n"
        s += "    \\draw[thick] (" + "{:.2f}".format(x3) + "," + "{:.2f}".format(y4) + ") -- (" + "{:.2f}".format(x3) + "," + "{:.2f}".format(y3) + ");\n"
        s += "    \\node at (" + x4 + "," + y5 + ") {" + str(node+1) + "};\n"
    return s


def draw_charging(capacity):
    s = ""

    for node in range(general_info["numCNodes"]):
        p_node = general_info["cToP"][node]
        s += "    \\node[right = " + "{:.2f}".format(sign_dist) + "pt of p_sign_" +  str(p_node) + "] {\\includegraphics[width=" +\
                sign_width + "cm]{\"tex/img/csign\".pdf}};\n"
        #s += "    \\node[below = 1mm of ideal_png_" + str(p_node) + "] (capacity_png_" + str(p_node) + ") {\\includegraphics[height=" +\
        #        car_height_string + "]{\"tex/img/car_orange\".png}};\n"
        s += "    \\node[below = 5mm of ideal_text_" + str(p_node) + ".east, anchor=north east] (capacity_text_" + str(p_node) + ") {Capacity: " + str(capacity[node]) + "};\n"

    return s

def draw_sidebar():
    s = ""
    x = width/2.0
    y = height + 1

    s += "    \\node[anchor=east] at (" + "{:.2f}".format(x) + "," + "{:.2f}".format(y) + ") (sidebar_header) {\\Huge \\textbf{Timestep: }};\n"
    #s += "    \\node[below = 1.5cm of sidebar_header.west, anchor=west] (sidebar_time_to_header) {\\Huge\\underline{Time to origin}};\n"

    return s


def draw_legend():
    s = ""

    legend_width = 33 + 6*car_height
    legend_height = 3 + car_height*3

    left_x = "{:.2f}".format((width - legend_width)/2)
    right_x = "{:.2f}".format(width - (width - legend_width)/2)
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
    s += draw_object_below("car_orange", "legend_green", "legend_orange")
    s += draw_object_below("car_red", "legend_orange", "legend_red")


    s += "    \\node[right = .2cm of legend_green] (legend_green_text) {Cars above battery threshold};\n"
    s += "    \\node[right = .2cm of legend_orange] (legend_orange_text) {Cars currently charging};\n" 
    s += "    \\node[right = .2cm of legend_red] (legend_red_text) {Cars below battery threshold};\n"

    s += draw_object("car_black", "legend_black", 0, ["right", "legend_green_text"])
    s += draw_object_below("bike", "legend_black", "legend_bike")
    s += draw_object_below("man", "legend_bike", "legend_man")

    s += "    \\node[right = .2cm of legend_black] (legend_black_text) {Operator in rental vehicle};\n"
    s += "    \\node[anchor=west] at (legend_black_text.west |- legend_bike) (legend_bike_text) {Operator on bike/public transport};\n"
    s += "    \\node[anchor=west] at (legend_bike_text.west |- legend_man) (legend_man_text) {Idle operator};\n"

    s += "    \\node[anchor=west] at (legend_bike_text.east |- legend_black) (legend_psign) {\\includegraphics[width=" + sign_width + " cm]{\"tex/img/psign\".pdf}};\n"
    s += "    \\node at ([yshift=-" + sign_width + "cm,xshift=1mm]legend_psign.south) (legend_csign) {\\includegraphics[width=" + sign_width + " cm]{\"tex/img/csign\".pdf}};\n"

    s += "    \\node[right = .2cm of legend_psign] (legend_psign_text) {Parking node};\n"
    s += "    \\node[anchor=west] at (legend_psign_text.west |- legend_csign) (legend_csign_text) {Charging node};\n"

    return s




def draw_nodes(ideal_state, capacity):
    s = ""
    s += draw_parking(ideal_state)
    s += draw_charging(capacity)
    s += draw_legend()
    s += draw_sidebar()
    return s


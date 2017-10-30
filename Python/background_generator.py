from output_reader import general_info
from car_generator import draw_object, draw_object_below, car_height_string
from helpers import x_left_from_node, y_top_from_node, row_from_node, x_right_from_node

step = 10
columns = general_info["w_nodes"]
rows = general_info["h_nodes"]
ideal_state = general_info["ideal_state"]
capacity = general_info["capacity"]
width = columns*step
height = rows*step

sign_width = "{:.2f}".format(step / 8)
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
                sign_width + "cm]{\"tex/img/psign\".pdf}};\n"

        x2 = x_right_from_node(node, step, columns)
        y2 = y_top_from_node(node, step, columns)
        s += "    \\node at (" + "{:.2f}".format(x2) + "," + "{:.2f}".format(y2) + ") (right_corner_" + str(node) + ") {};\n"
        s += "    \\node[below left = 1mm of right_corner_" + str(node) + "] (ideal_png_" + str(node) + ") {\\includegraphics[height=" +\
                car_height_string + "]{\"tex/img/car_green\".png}};\n"
        s += "    \\node[left = 1mm of ideal_png_" + str(node) + "] (ideal_text_" + str(node) + ") {Ideal: " + str(ideal_state[node]) + " x};\n"

    return s


def draw_charging():
    s = ""

    for node in range(general_info["n_c_nodes"]):
        p_node = general_info["c_to_p"][node]
        s += "    \\node[right = " + "{:.2f}".format(sign_dist) + "pt of p_sign_" +  str(p_node) + "] {\\includegraphics[width=" +\
                sign_width + "cm]{\"tex/img/csign\".pdf}};\n"
        s += "    \\node[below = 1mm of ideal_png_" + str(p_node) + "] (capacity_png_" + str(p_node) + ") {\\includegraphics[height=" +\
                car_height_string + "]{\"tex/img/car_orange\".png}};\n"
        s += "    \\node[left = 1mm of capacity_png_" + str(p_node) + "] (capacity_text_" + str(p_node) + ") {Capacity: " + str(capacity[node]) + " x};\n"

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

    legend_width = 2*width/3
    legend_height = width/6

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
    #dd:s += "    \\node[right = .2cm of legend_green_text.east] (legend_black) {\\includegraphics[height
    s += draw_object_below("bike", "legend_black", "legend_bike")
    s += draw_object_below("man", "legend_bike", "legend_man")

    s += "    \\node[right = .2cm of legend_black] (legend_black_text) {Operator in rental vehicle};\n"
    s += "    \\node[anchor=west] at (legend_black_text.west |- legend_bike) (legend_bike_text) {Operator on bike/public transport};\n"
    s += "    \\node[anchor=west] at (legend_bike_text.west |- legend_man) (legend_man_text) {Idle operator};\n"

    s += "    \\node at ([yshift=-4mm,xshift=2.5cm]legend_black_text.east) (legend_psign) {\\includegraphics[width=" + sign_width + " cm]{\"tex/img/psign\".pdf}};\n"
    s += "    \\node at ([yshift=-1cm,xshift=1mm]legend_psign.south) (legend_csign) {\\includegraphics[width=" + sign_width + " cm]{\"tex/img/csign\".pdf}};\n"

    s += "    \\node[right = .2cm of legend_psign] (legend_psign_text) {Parking node};\n"
    s += "    \\node[anchor=west] at (legend_psign_text.west |- legend_csign) (legend_csign_text) {Charging node};\n"

    return s




def draw_nodes():
    s = ""
    s += draw_parking()
    s += draw_charging()
    s += draw_legend()
    s += draw_sidebar()
    return s


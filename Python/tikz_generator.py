data = {"n_nodes": 6, "w_nodes": 3, "h_nodes": 2, "n_c_nodes": 2, "c_to_p": [3,5], "cars_parked": [1, 0, 1, 0, 0, 1], "cars_need": [0, 1, 0, 0, 1, 0], "charging": [1,0]}

edge = "edge[dotted]"
start = "\\documentclass[margin=10pt]{standalone}\n" +\
        "\\usepackage{tikz}\n" +\
        "\\begin{document}\n\\begin{tikzpicture}\n"

nodes = ""

end = "\\end{tikzpicture}\n\\end{document}"

def write_file(tex_string, n):
    with open('tex/snapshot_' + str(n) + '.tex', 'w') as f:
        f.write(start + tex_string + end)

def draw_nodes():
    step = 6/data["w_nodes"]
    height = step*data["h_nodes"]
    step_string = "{:.1f}".format(step)
    height_string = "{:.1f}".format(height)
    s = "   \\draw[step=" + step_string + "cm, color=black] (0,0) grid (6," + height_string + ");\n"

    c_step = step / 3
    for c in range(data["n_c_nodes"]):
        p = data["c_to_p"][c]
        row = p//data["w_nodes"]
        col = p - data["w_nodes"]*row
        left_y = "{:.1f}".format(row*step - c_step)
        left_x = "{:.1f}".format(col*step)
        top_y =  "{:.1f}".format(row*step)
        top_x =  "{:.1f}".format(col*step + c_step)
        box_string = "   \\draw (" + left_x + "," + left_y + ") " + edge + " (" +\
                top_x + "," + left_y + ");\n   \\draw (" + top_x + "," + left_y + ") " + edge + " (" + top_x + "," + top_y + ");\n"
        
        s += box_string
    return s

test_string1 = "\\draw[step=0.5cm,color=black] (-1,-1) grid (1,1);" +\
    	      	"\\node[fill=green] at (-0.75,+0.75) {};" +\
    		"\\node[fill=green] at (-0.25,+0.75) {};" +\
    		"\\node[fill=orange] at (+0.25,+0.75) {};" +\
    		"\\node[fill=orange] at (+0.75,+0.75) {};" +\
    		"\\node[fill=purple!70] at (-0.75,+0.25) {};"
test_string2 = "\\draw[step=0.5cm,color=black] (-1,-1) grid (1,1);" +\
    	      	"\\node[fill=red] at (-0.75,+0.75) {};" +\
    		"\\node[fill=red] at (-0.25,+0.75) {};" +\
    		"\\node[fill=orange] at (+0.25,+0.75) {};" +\
    		"\\node[fill=orange] at (+0.75,+0.75) {};" +\
    		"\\node[fill=purple!70] at (-0.75,+0.25) {};"
test_string3 = "\\draw[step=0.5cm,color=black] (-1,-1) grid (1,1);" +\
    	      	"\\node[fill=gray] at (-0.75,+0.75) {};" +\
    		"\\node[fill=gray] at (-0.25,+0.75) {};" +\
    		"\\node[fill=orange] at (+0.25,+0.75) {};" +\
    		"\\node[fill=orange] at (+0.75,+0.75) {};" +\
    		"\\node[fill=purple!70] at (-0.75,+0.25) {};"

#test_strings = [test_string1, test_string2, test_string3]

foo = draw_nodes()
test_strings = [foo]


for i in range(len(test_strings)):
    write_file(test_strings[i], i)

import background_generator
import car_generator
import scenario_generator

edge = "edge[dotted]"
start = "\\documentclass[png]{standalone}\n" +\
        "\\usepackage{tikz}\n" +\
        "\\usepackage{graphicx}\n" +\
        "\\usetikzlibrary{positioning,calc}\n" +\
        "\\begin{document}\n\\begin{tikzpicture}\n"

nodes = ""

end = "\\end{tikzpicture}\n\\end{document}\n"

def write_file(tex_string, i, n):
    with open('tex/snapshot_' + get_file_ending(i, n) + '.tex', 'w') as f:
        f.write(start + tex_string + end)



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

def draw():
    all_snapshots = scenario_generator.draw_all_snapshots()
    n = len(all_snapshots)
    for i in range(n):
        snapshot = all_snapshots[i]
        write_file(snapshot, i, n)


def get_file_ending(i, n):
    n_string = str(n)
    i_string = str(i)

    zeros = len(n_string) - len(i_string)
    return zeros*"0" + str(i)
    
draw()

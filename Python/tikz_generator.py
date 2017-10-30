import scenario_generator
import snapshot_creater

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


def draw():
    snapshots = snapshot_creater.generate_snapshots(3)
    snapshot_strings = scenario_generator.draw_all_snapshots(snapshots)
    n = len(snapshot_strings)
    for i in range(n):
        snapshot_string = snapshot_strings[i]
        write_file(snapshot_string, i, n)


def get_file_ending(i, n):
    n_string = str(n)
    i_string = str(i)

    zeros = len(n_string) - len(i_string)
    return zeros*"0" + str(i)
    
draw()

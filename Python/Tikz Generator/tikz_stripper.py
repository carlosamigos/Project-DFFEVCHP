import sys

new_file = ""
with open(sys.argv[1]) as f:
    line_number = 0
    for line in f:
        if line_number < 5:
            line_number += 1
            continue
        if "\\end{document}" in line:
            line = line.replace("\\end{document}", "")

        new_file += line

    
    new_file.replace("\\end{document}", "")

new_file_path = sys.argv[1][:-3] + "tikz"
with open(new_file_path, "w+") as f:
    f.write(new_file)

print(sys.argv[1] + ": Done creating tikz from tex")

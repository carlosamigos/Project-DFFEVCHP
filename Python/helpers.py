def x_from_col(col, step):
    return col*step

def y_from_row(row, step, n_rows):
    return (n_rows - 1 - row) * step

def row_from_node(node, width):
    return node // width

def col_from_node(node, width):
    return node % width

def x_left_from_node(node, step, width):
    return x_from_col(col_from_node(node, width), step)

def x_right_from_node(node, step, width):
    return x_left_from_node(node, step, width) + step

def y_top_from_node(node, step, width):
    return y_from_row(row_from_node(node, width), step, width)

def y_bottom_from_node(node, step, width):
    return y_top_from_node(node, step, width) - step

def x_mid_from_node(node, step, width):
    return x_left_from_node(node, step, width) + step/2

def y_mid_from_node(node, step, width):
    return y_top_from_node(node, step, width) - step/2

def x_for_origin(node, step, width):
    return x_mid_from_node(node, step, width) - step/3

def y_for_origin(node, step, width):
    return y_mid_from_node(node, step, width)

from output_reader import general_info
import snapshot_creater
from car_generator import fill_parking, fill_charging, draw_moving_object, fill_origin_list, fill_node_with_operators
from background_generator import draw_nodes

n_nodes = general_info["numPNodes"]
c_nodes = general_info["numCNodes"]
n_operators = general_info["numOperators"]
max_time = general_info["timeLimit"]

def draw_operators(operator_info):
    s = ""
    origin_list = []
    operator_in_node = [[] for _ in range(n_nodes)]
    for operator in range(n_operators):
        current_operator_info = operator_info[operator]
        to_node = current_operator_info["to"]
        from_node = current_operator_info["from"]
        covered = current_operator_info["covered"]
        handling = current_operator_info["handling"]
        operator_id = current_operator_info["id"]

        if from_node >= n_nodes + c_nodes:
            origin_list.append(current_operator_info)
        else:
            if to_node != from_node:
                if from_node >= n_nodes:
                    from_node = general_info["cToP"][from_node - n_nodes]
                if handling:
                    obj = "car_black"
                else:
                    obj = "bike"
                if to_node >= n_nodes and to_node < n_nodes + c_nodes:
                    to_node = general_info["cToP"][to_node - n_nodes]
                s += draw_moving_object(from_node, to_node, covered, operator_id, obj)
            else:
                if to_node >= n_nodes and to_node < n_nodes + c_nodes:
                    to_node = general_info["cToP"][to_node - n_nodes]
                operator_in_node[to_node].append(current_operator_info)

    for node in range(n_nodes):
        if len(operator_in_node[node]) > 0:
            s += fill_node_with_operators(node, operator_in_node[node])
    s += fill_origin_list(origin_list)
    return s

def draw_snapshot(snapshot, i, time_step):
    s = draw_nodes(snapshot["ideal_state"], snapshot["capacity"])
    time_elapsed = "{:.1f}".format(i*time_step)
    s += "    \\node[right = 0cm of sidebar_header] {\\Huge " + time_elapsed + "/" + "{:.1f}".format(max_time) + "};\n"
    for parking_node in range(n_nodes):
        s += fill_parking(parking_node, snapshot["cars_parked"][parking_node], snapshot["cars_need"][parking_node])
    
    for charging_node in range(c_nodes):
        s += fill_charging(charging_node, snapshot["charging"][charging_node])

    operator_info = snapshot["operators"]
    s += draw_operators(operator_info)

    return s


def draw_all_snapshots(snapshots):
    result = []
    time_step = max_time / len(snapshots)
    for snapshot_no in range(len(snapshots)):
        result.append(draw_snapshot(snapshots[snapshot_no], snapshot_no, time_step))

    return result

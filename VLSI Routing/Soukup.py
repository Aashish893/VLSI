import random
import numpy as np
import random
import numpy as np
import matplotlib.pyplot as plt
from collections import deque


# Cell Class is used to store the cells
class Cell:
    def __init__(self, name, cell_type):
        self.name = name
        self.cell_type = cell_type

# Cell Class is used to store the nets
class Interconnect:
    def __init__(self, net_name, cells):
        self.net_name = net_name
        self.cells = cells

# Cell Class is used to store the Obstacles
class Obstacle:
    def __init__(self):
        self.name = "obstacle"


# This is where out main function starts
def main():
    # We define this globally since they are used in Most functions of the code
    global neighbors,grid,path 

    # Next we decide how many cells of each tipe we will need
    num_standard_cells = 6
    num_pad_cells = 2

    # Here we decide the dimensions of our grid

    grid_width, grid_height = 6,6
    
    # This is to initialize the Cell object with normal cells
    cells = []
    for i in range(num_standard_cells):
        cell_name = f"a{i}"
        cell = Cell(cell_name, 'standard')
        cells.append(cell)

    # This is to initialize the Cell object with pad cells
    for i in range(num_pad_cells):
        cell_name = f"p{i}"
        cell = Cell(cell_name, 'pad')
        cells.append(cell)


    # Generate a fixed number of random Nets 
    num_nets = 2
    nets = []
    for i in range(num_nets):
        net_name = f"net{i}"
        # Randomly select a number of cells between 2 and a specified maximum
        max_cells_in_net = 4  # This can be changed based on out preference
        num_cells_in_net = random.randint(2, max_cells_in_net)
        # Randomly select the cells to be in the net
        connected_cells = random.sample(cells, num_cells_in_net)
        net = Interconnect(net_name, connected_cells)
        nets.append(net)

    # Initialize The grid
    grid = initialize_grid(grid_height, grid_width)

    # Create Co-ordinate for Standard Cells on the grid
    placements, occupied_positions = place_standard_cells(cells, grid_height, grid_width)

    # Place pad cells on the edges
    for cell in cells:
        if cell.cell_type == "pad":
            y, x = generate_edge_placement(grid_height, grid_width)
            while (y, x) in occupied_positions:
                y, x = generate_edge_placement(grid_height, grid_width) #This gives a random coordinate on the edge of the grid
            placements[cell.name] = (y, x)
            occupied_positions.append((y, x))

    # Place cells on the grid
    for cell in cells:
        y, x = placements[cell.name]
        grid[y][x] = cell
        cell.location = (y,x)

    # Here we Define the Number of Obstacles we want to use.
    num_obstacles = 10
    obstacles, occupied_positions = generate_random_obstacles(num_obstacles, grid_height, grid_width, cells,placements)

    # Place obstacles on the grid
    for obstacle, position in zip(obstacles, occupied_positions[-num_obstacles:]):
        y, x = position
        grid[y][x] = obstacle

    # Here we get all the neighbors of the non-obstacle objects
    neighbors = Create_Neighbors()

    path={} # A Map to store the path mapped to their source and target
    for net in nets:
        source = net.cells[0].location # Get the SOURCE
        target = net.cells[-1].location # Get the TARGET
        print(source , target)
        path[(source,target)] = SoukupAlgorithm(source,target) # Get the path from Soukup's Algorithm
    
    # Visualizes all the paths from source to target
    visualize_path(path)

# END of Main Function

# This function Initializes the Grid with the dimensions specified above
def initialize_grid(height, width):
    return np.empty((height, width), dtype=object)


# This generates the Y,X coordinates for the standard cells
def place_standard_cells(cells, grid_height, grid_width):
    occupied_positions = []
    placements = {}
    for cell in cells:
        if cell.cell_type == "standard":
            y, x = random.randint(1, grid_width - 2), random.randint(1, grid_height - 2)
            while (y, x) in occupied_positions: # Find a free position
                x, y = random.randint(1, grid_width - 2), random.randint(1, grid_height - 2)
            placements[cell.name] = (y, x)
            occupied_positions.append((y, x))
    return placements, occupied_positions

# This generates the Y,X coordinates for the pad cells
def generate_edge_placement(grid_height, grid_width):
    edge = random.choice(["top", "bottom", "left", "right"])
    if edge == "top":
        return random.randint(0, grid_width - 1), 0
    elif edge == "bottom":
        return random.randint(0, grid_width - 1), grid_height - 1
    elif edge == "left":
        return 0, random.randint(0, grid_height - 1)
    else:
        return grid_width - 1, random.randint(0, grid_height - 1)

# This generates the Y,X coordinates for the obstacles
def generate_random_obstacles(num_obstacles, grid_height, grid_width, cells,placements):
    occupied_positions = [placements[cell.name] for cell in cells]
    obstacles = []
    for i in range(num_obstacles):
        y, x = random.randint(0, grid_width - 1), random.randint(0, grid_height - 1)
        while (y, x) in occupied_positions: # Find a free position 
            y, x = random.randint(0, grid_width - 1), random.randint(0, grid_height - 1)
        occupied_positions.append((y, x))
        obstacles.append(Obstacle())
    return obstacles, occupied_positions


# This function Finds the Neighbors
def Create_Neighbors():
    neighbors = {}
    for y in range(len(grid)):
        for x in range(len(grid[y])):
            if not isinstance(grid[y][x], Obstacle) :
                # Check for neighbors
                if x + 1 < len(grid[0]) and not isinstance(grid[y][x+1], Obstacle):
                    neighbors.setdefault((y, x), []).append((y, x + 1))
                if x - 1 >= 0 and not isinstance(grid[y][x-1], Obstacle):
                    neighbors.setdefault((y, x), []).append((y, x - 1))
                if y + 1 < len(grid) and not isinstance(grid[y+1][x], Obstacle):
                    neighbors.setdefault((y, x), []).append((y + 1, x))
                if y - 1 >= 0 and not isinstance(grid[y-1][x], Obstacle):
                    neighbors.setdefault((y, x), []).append((y - 1, x))
    return neighbors


# This is the Function that performs Soukup's algorithm
def SoukupAlgorithm(s,t):
    plist = [] # Propogation List
    nList = [] # Neighbor List for bfs
    plist.append(s)
    temp = 1 # This term is used to backtrack from target to source
    L ={}
    L[s] = 0
    Visited = [] # We use this to avoid the infinite loops in bfs step
    path_exists = False
    try:
        while plist:
            for i in plist:
                for j in neighbors.get(i):
                    Visited =[i]
                    if j[0] == t[0] and j[1] == t[1]:
                        L[j] = temp
                        
                        path_exists = True
                        raise BreakNestedLoops
                    if isinstance(grid[j[0],[j[1]]], Obstacle) == False:
                        if DIR(i,j,t): 
                            L[j] = temp
                            temp = temp + 1
                            plist.append(j)
                        # Here we start the dfs once we have found the neighbor in correct direction
                        while(isinstance(grid[j[0],[j[1]]], Obstacle) == False and not Visited.__contains__(j)): 
                                Visited.append(j)
                                for k in neighbors.get(j):
                                    if DIR(j,k,t) and not Visited.__contains__(k): 
                                        j = k
                                        L[j] = temp
                                        temp = temp + 1
                                        plist.append(j)
                                        break  
                    else: # We Continue bfs if no unblocked neighbor is fount
                        L[j] = temp
                        temp+=1
                        nList.append(j)
            plist = nList
            nList = []
    except BreakNestedLoops:
        pass
        if path_exists:
            # This calls the retrace function
            L = retrace(s,t,L)
            p = L[::-1] # Simple logic to reverse the list in python
            return p
        else:
            return 

# This function checks whether the search is in the direction or not
def DIR( i, j, t):
    return abs(i[0] - t[0]) + abs(i[1] - t[1]) > abs(j[0] - t[0]) + abs(j[1] - t[1])

# This function is used to retrace the path from the target to the source
def retrace(s, t, L):
    visited = set()
    queue = deque([(t, [t])])
    while queue:
        node, path = queue.popleft()
        if node not in visited:
            visited.add(node)
            if node == s:
                return path

            for neighbor in neighbors.get(node):
                if neighbor not in visited and neighbor in L:
                    queue.append((neighbor, path + [neighbor]))
    
    return None

# We need this class in python to break out of nested loop
class BreakNestedLoops(Exception):
    pass


# This function is to visualize the paths which are found between the source and targets
def visualize_path(paths):
    fig, ax = plt.subplots(figsize=(12, 12))
    path_colors = ['red', 'green', 'blue', 'purple', 'orange', 'magenta', 'cyan']
    for y in range(grid.shape[0]):
        for x in range(grid.shape[1]):
            elem = grid[y][x]
            if elem is not None:
                if isinstance(elem, Cell):
                    color = 'blue' if elem.cell_type == 'standard' else 'cyan'
                elif isinstance(elem, Obstacle):
                    color = 'black'
                else:
                    continue
                ax.scatter(x, y, c=color, marker='s', edgecolors='black', linewidths=0.5, s=100)

    for color_idx, path in enumerate(paths.values()):
        path_color = path_colors[color_idx % len(path_colors)]
        path_x = [p[0] for p in path]
        path_y = [p[1] for p in path]
        plt.plot(path_y, path_x, color=path_color, linewidth=2, marker='o', markersize=5, markerfacecolor='yellow', markeredgecolor='black')
        # Denote the first starting point and the last ending point
        plt.scatter(path_y[0], path_x[0], color='purple', marker='o', s=100, edgecolor='black', zorder=10)
        plt.scatter(path_y[-1], path_x[-1], color='orange', marker='o', s=100, edgecolor='black', zorder=10)


    
    ax.set_xticks(np.arange(-0.5, grid.shape[1], 1), minor=True)
    ax.set_yticks(np.arange(-0.5, grid.shape[0], 1), minor=True)
    ax.grid(which='minor', color='black', linewidth=0.5)
    ax.set_xlim(-0.5, grid.shape[1] - 0.5)
    ax.set_ylim(-0.5, grid.shape[0] - 0.5)

    # Add horizontal and vertical lines
    for y in range(grid.shape[0]):
        ax.axhline(y, color='black', linewidth=0.5)
    for x in range(grid.shape[1]):
        ax.axvline(x, color='black', linewidth=0.5)

# Set x and y ticks
    ax.set_xticks(np.arange(0, grid.shape[1], 1))
    ax.set_yticks(np.arange(0, grid.shape[0], 1))
    # plt.gca().invert_yaxis()
    plt.show()
    

# This is where the main function
if __name__ == "__main__":
    main()

# END OF CODE
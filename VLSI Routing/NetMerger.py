import matplotlib.pyplot as plt
import numpy as np
import math
import random
def main():
    # Define the inputs for terminals of the channel
    TOP = [0,1,4,5,1,6,7,0,4,9,10,10]
    BOT = [2,3,5,3,5,2,6,8,9,8,7,9]

    global S,T
    global counter
    global Tracks
    global Hs
    global endTracer
    Tracks = []
    # We create a hashset to store values
    Hs = set()
    for i in TOP:
        Hs.add(i)
    for j in BOT:
        Hs.add(j)
    # Since there is no use of 0 value on the tracks we do not consider it
    Hs.remove(0)
    counter = max(Hs) + 1
    # This Map stores which nets are in which column
    Map = {}
    endTracer = []
    for i in Hs:
        curr = []
        for j in range(0,len(BOT)):
            if BOT[j] == i:
                curr.append(j+1)
        for j in range(0,len(TOP)):
            if TOP[j] == i:
                curr.append(j+1)
        minval = np.min(curr)
        maxval = np.max(curr)
        Map[i] = [v for v in range(minval,maxval+1)]
    for i in range(len(BOT)):
        if(BOT[i]) != 0 and BOT[i] not in endTracer: 
            endTracer.append(BOT[i])
        if(TOP[i]) != 0 and TOP[i] not in endTracer: 
            endTracer.append(TOP[i])
    # We creata Map of Columns and nets to store the values in order
    ColumnMap = {new_list: [] for new_list in range(1,len(TOP) + 1)}
    for i in Map:
        cols = Map.get(i)
        for j in cols:
            (ColumnMap.get(j)).append(i) 
    # To create Maximal Sets we remove the subsets from the Column map
    temp = remove_subsets(ColumnMap)
    # This will get the Maximal Set of our input
    MaximalSet = GetMaximalSet(temp)
    VCG = CreateVCG(TOP, BOT, Hs)
    # Create random S node and T node 
    S = -100
    T = 100
    # Connect sinks to T
    for i in VCG:
        if VCG[i] == []:
            VCG[i] = [T]
    #  Connect S to all sources
    VCG[S] = []
    VCG[T] = []
    for i in Hs:
        found = False
        for j in VCG.values():
            if i in j:
                found = True
                break
        if not found:
            VCG[S].append(i)

    StartingZones, EndingZones = GetNetSpans(MaximalSet)
    output = NetMerge(VCG,MaximalSet,StartingZones, EndingZones, counter, endTracer)
    draw_arrays(TOP, BOT, output)



# We use this function to store the merged dictionary in order to help create the zones.
def GetMaximalSet(d):
    new_dict = {}
    new_key = 1
    for k, v in d.items():
        new_dict[new_key] = v
        new_key += 1
    return new_dict

# Function to check whether set s1 is subset of set s2
def is_subset(s1, s2):
    return s1.issubset(s2)

# This Function removes the subsets from the map and returns the merged dictionary
def remove_subsets(d):
    keys_to_remove = set()
    unique_lists = set()
    for k1, v1 in d.items():
        sorted_v1 = tuple(sorted(v1))

        if sorted_v1 in unique_lists:
            keys_to_remove.add(k1)
            continue

        unique_lists.add(sorted_v1)
        for k2, v2 in d.items():
            if k1 == k2 or sorted_v1 == tuple(sorted(v2)):
                continue
            if is_subset(set(v1), set(v2)):
                keys_to_remove.add(k1)
                break

    for key in keys_to_remove:
        del d[key]

    return d




# This function creates the Vertical Constraint Graph as an Adjacency Map
def CreateVCG(TOP, BOT, Hs):
    VCG = {}
    for i,j in enumerate(Hs):
        VCG[j] = []

    for i in range(len(TOP)):
        if TOP[i]!=0 and BOT[i]!=0:
            VCG[TOP[i]].append(BOT[i])
    print(VCG)
    return VCG

# Here We calculate the which notes starts where and ends where
def GetNetSpans(MaximalSet):
    TrackSet = set(MaximalSet.get(1))
    StartingZone = [None]*len(MaximalSet)
    StartingZone[0] = MaximalSet.get(1)
    EndingZone = [None]*len(MaximalSet)
    EndingZone[len(MaximalSet) -1 ] = MaximalSet.get(len(MaximalSet))
    for i in range(1,len(MaximalSet)):
        StartingZone[i] = list(set(MaximalSet.get(i+1)) - TrackSet  )
        TrackSet.update(StartingZone[i])
    TrackSet = set() # Empty The Set to Reuse for ending zones
    TrackSet = set(MaximalSet.get(len(MaximalSet)))
    for i in range(len(MaximalSet)-1,0,-1):
        EndingZone[i-1] = list(set(MaximalSet.get(i)) - TrackSet)
        TrackSet.update(EndingZone[i-1])
    return StartingZone, EndingZone


def NetMerge(VCG,MaximalSet,StartingZones, EndingZones, counter, endTracer):
    L = set()
    for i in range(0,len(MaximalSet)):
        Zi = set(EndingZones[i])
        if(i+1 == len(MaximalSet)):
            Zi1 = StartingZones[i]
        else:
            Zi1 = set(StartingZones[i+1])
        L = L.union(Zi)
        Lprime = Merge(VCG,L,Zi1,counter)
        removal(Lprime, EndingZones, StartingZones)
        counter+= 1
        L = L - Lprime
        if(i+1 < len(MaximalSet)):
            for val in L:
                EndingZones[i+1].append(val)
    fetchValue(Tracks)
    VCG = cleanup(VCG)

    display = {}
    for i in range(len(VCG)):
        display[i] = []
    numOfTracks = len(VCG)

    sorted_list = []
    for i in endTracer: 
        for node in VCG: 
                if isinstance(node, int): 
                    if node not in sorted_list:
                        if node == i : 
                            sorted_list.append(node)
                            break
                else:
                    if list(node) not in sorted_list: 
                        for k in node: 
                            if (k == i):
                                sorted_list.append(list(node))
                                break
    while(sorted_list):
        for i in sorted_list: 
            if isinstance(i, int):
                if (VCG[i] == []): 
                    display[numOfTracks-1] = i
                    numOfTracks-=1
                    delFromGraph(VCG, i)
                    sorted_list.remove(i)
            elif (VCG[tuple(i)] == []):
                display[numOfTracks-1] = i
                delFromGraph(VCG, i)
                numOfTracks-=1
                sorted_list.remove(i)

    return display



def delFromGraph(VCG, node):
    for i in VCG: 
        for j in VCG[i]: 
            if j == node: 
                VCG[i].remove(node)
    if isinstance(node, int):
        del VCG[node]
    else: 
        del VCG[tuple(node)]
    return VCG

def removal(Lprime, EndingZones, StartingZones):
    for l in Lprime:
        for j in StartingZones:
            if l in j:
                j.remove(l)
                j.append(counter)
        for k in EndingZones:
            if l in k:
                k.remove(l)
                k.append(counter)


def cleanup(VCG):
        if S in VCG:
            del VCG[S]
        if T in VCG:
            del VCG[T]
        for i in VCG.values():
            if S in i:
                i.remove(S)
            if T in i:
                i.remove(T)
        
        Graph = {}
        for key in VCG:
            if key > max(Hs):
                values = VCG[key]
                key = tuple(Tracks[key - max(Hs) -1])
                Graph[key] = values
            else:
                Graph[key] = VCG[key]
        
        for i in Graph:
            Graph[i] = list(set(Graph[i]))
        for i in Graph:
            for k in Graph[i]:
                if isinstance(k, int):
                    if k > max(Hs):
                        Graph[i].remove(k)
                        Graph[i].insert(0,Tracks[k-max(Hs) -1])
                else:
                    toadd = []
                    for val in k:
                        if val > max(Hs):
                            Graph[i][k].remove(val)
                            toadd.append(Tracks[val-max(Hs) -1])
                            Graph[i][k].append(toadd)
                    # for t in Tracks[k - max(Hs) -1]:
                    #     Graph[i].append(t)
        return Graph
        #if the value is greater than hs.max - tracks[value - tracks.size()]
        

def fetchValue(Tracks):
    for i in Tracks:
        for v in i : 
            if (v > max(Hs)):
                i.remove(v)
                for k in Tracks[v - max(Hs) -1]:
                    i.append(k)

def Merge(VCG,L,R, counter):
    U,D,F,G, H = {},{},{},{},{}
    for i in L.union(R):
        Distance1 = longest_path(VCG,S,i)
        Distance2 = longest_path(VCG,i,T)
        if Distance1 == float('-inf') or Distance2 == float('-inf'):
            continue
        U[i] = Distance1
        D[i] = Distance2
    maxVal = float('-inf')
    m = 0
    for i in R:
        F[i] = 100*(U[i] + D[i]) + max(U[i], D[i])
        if F[i] > maxVal:
            maxVal = F[i]
            m = i
    minVal = float('inf')
    n = 0
    for i in L :
        if longest_path(VCG,i,m) != float('-inf') or (i not in D.keys() or i not in U.keys()):
            continue
        H[(i,m)] = max(U[i], U[m]) + max(D[i], D[m]) + max((U[i]+D[i]), (U[m]+D[m]))
        G[(i,m)] = 100* H[(i,m)]  - (math.sqrt(U[i]*U[m])+ math.sqrt(D[i]*D[m]))
        if G[(i,m)] <= minVal:
            minVal = G[(i,m)]
            n = i
    
    Tracks.append([n,m])
    VCG = merge_nodes(VCG, n,m, counter)
    
    return {n,m}


def longest_path(graph, source, target, memo=None):
    if memo is None:
        memo = {}

    if source == target:
        return 0

    if source in memo:
        return memo[source]

    if source not in graph:
        return float('-inf')

    max_length = float('-inf')
    for neighbor in graph[source]:
        length = longest_path(graph, neighbor, target, memo)
        max_length = max(max_length, length + 1)

    memo[source] = max_length
    return max_length


def merge_nodes(graph, node1, node2, new_node):
    if node1 not in graph or node2 not in graph:
        raise ValueError("One or both nodes not in graph")

    # Create a new node with the combined outgoing edges of node1 and node2
    graph[new_node] = list(set(graph[node1]) | set(graph[node2]))

    # Remove self-loops if present
    if new_node in graph[new_node]:
        graph[new_node].remove(new_node)

    # Update incoming edges of node1 and node2 to point to the new node
    for node, edges in graph.items():
        if node == new_node:
            continue
        if node1 in edges:
            edges.remove(node1)
            edges.append(new_node)
        if node2 in edges:
            edges.remove(node2)
            edges.append(new_node)

    # Remove original nodes
    del graph[node1]
    del graph[node2]

    return graph



# This function is used to Visualize the output by creating appropriate tracks 
# and connecting the routed nets

def draw_arrays(TOP, BOT, output):
    fig, ax = plt.subplots()
    ax.plot([0, len(TOP)], [0.5, 0.5], color='gray', linestyle='-', linewidth=2)
    ax.plot([0, len(BOT)], [-0.5, -0.5], color='gray', linestyle='-', linewidth=2)
    
    color_array = ['red', 'blue', 'green', 'yellow', 'purple', 'orange', 'pink', 'brown', 'gray', 'black']

    # Calculate the number of tracks and their positions
    num_tracks = len(output)
    track_positions = np.linspace(-0.5, 0.5, num_tracks+2)[1:-1]  # Excluding the first and last values
    
    # Draw tracks
    for i, val in enumerate(track_positions):
        ax.plot([0, len(TOP)], [val, val], color='gray', linestyle='-', linewidth=1)

    for i, (top, bot) in enumerate(zip(TOP, BOT)):
        ax.text(i, 0.5, str(top), ha='center', va='center', fontsize=9, bbox=dict(facecolor='blue', alpha=0.5, edgecolor='blue'))
        ax.text(i, -0.5, str(bot), ha='center', va='center', fontsize=9, bbox=dict(facecolor='red', alpha=0.5, edgecolor='red'))

    Map = getLimits(TOP, BOT)

    for i in Map:
        color = random.choice(color_array)  
        xleft = Map[i][0]-1
        xRight = Map[i][1]-1
        ycord = 0
        for trk in range(len(output)):
            if isinstance(output[trk], int):
                if(output[trk] == i): 
                    ycord = track_positions[len(track_positions) -trk-1]
                    break
            else: 
                if i in output[trk]:
                    ycord = track_positions[len(track_positions) -trk-1]
                    break
        ax.plot([xleft,xRight], [ycord, ycord], color=color, linestyle='-', linewidth=3)
        
        if i in BOT:
            if BOT.index(i) == xleft: 
                ax.plot([xleft, xleft], [-0.5,ycord], color=color, linestyle='-', linewidth=3)
            if(len(BOT) - BOT[::-1].index(i) - 1 == xRight):
                ax.plot([xRight, xRight], [-0.5,ycord], color=color, linestyle='-', linewidth=3)
        if i in TOP: 
            if TOP.index(i) == xleft: 
                ax.plot([xleft, xleft], [0.5,ycord], color=color, linestyle='-', linewidth=3)
            if(len(TOP) - TOP[::-1].index(i) - 1 == xRight):
                ax.plot([xRight, xRight], [0.5,ycord], color=color, linestyle='-', linewidth=3)
        


    ax.set_xticks(range(len(TOP)))
    ax.set_xticklabels([f"C({i+1})" for i in range(len(TOP))])

    ax.set_yticks([-0.5, 0.5] + [(-0.5 + (i + 1) * 1 / (len(output) + 1)) for i in range(len(output))])
    ax.set_yticklabels(['BOT', 'TOP'] + [f"Track {i+1}" for i in range(len(output))])

    plt.title("TOP and BOT Arrays with Output Tracks")
    plt.xlabel("Columns")
    plt.grid(False)
    plt.show()

def getLimits(TOP, BOT):
    Hs = set()
    Map = {}
    for i in range(len(TOP)):
        Hs.add(TOP[i])
        Hs.add(BOT[i])
    Hs.remove(0)
    for i in Hs:
        curr = []
        for j in range(0,len(BOT)):
            if BOT[j] == i:
                curr.append(j+1)
        for j in range(0,len(TOP)):
            if TOP[j] == i:
                curr.append(j+1)
        
        minval = np.min(curr)
        maxval = np.max(curr)
        Map[i] = (minval, maxval)
    return Map


if __name__ == "__main__":
    main()
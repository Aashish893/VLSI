import java.io.IOException;
import java.util.*;

public class Main {
    private static final int Temperature = 30000; // This is the initial temperature for the simulated annealing process

    public static Map<String, Node> CircuitNodes; // This variable stores information of all the nodes used in the
                                                  // process.
    public static Map<Integer, ArrayList<String>> CircuitNets; // This stores information about the interconnects

    public static double coolingRate = 0.95; // This is the cooling rate for the simulated annealing

    public static ArrayList<Row> standardrows = new ArrayList<>(); // This is a list of Row object to store the Standard
                                                                   // Cells

    public static ArrayList<Row> macroRows = new ArrayList<>(); // This is a list of Row object to store the Macro Cells

    public static int initialChipWidth; // This stores the initial chip width once the placement is done

    public static int verticalSpacing = 1; // This defines the vertical spacing between rows

    public static int horizontalSpacing = 0; // This defines horizontal spacing between cells in each row

    public static double TotalChipArea = 0; // This stores total area for the chip

    public static double terminationTemperature = 0.1; // This is the Termination Temperatue

    public static int maxMovesPerTemperature = 500; // This defines the total number of moves for each temperature

    public static int initialWirelength; // This stores the initial wirelength generated.

    public static int initialchipArea; // This stores the initial Chip Area

    public static double initialCost; // This stores the initial Cost of placement

    public static int RowWidth; // This stores the total allowed width for a row

    public static final Random random = new Random();

    public static void main(String[] args) throws IOException { // Main Function
        String nodesFile = "ibm09.nodes"; // This is the nodes file

        String netsFile = "ibm09.nets"; // This is the Nets file

        String plFile = "ibm09.pl"; // This is the pl file.

        parser.NodeParser(nodesFile); // Calls the node parser function from the parser file to get height and width
                                      // of nodes

        parser.NetsParser(netsFile); // Calls the nets parser function from the parser file to get interconnects of
                                     // nodes

        parser.PlParser(plFile); // Calls the pl parser function from the parser file to intialize palcement of
                                 // nodes to (0,0)

        CircuitNodes = parser.CircuitNodes; // Globally stores the parsed information of nodes

        CircuitNets = parser.CircuitNets; // Globally stores the parsed information of interconnects

        int maxwidth = 0; // This block calculates the maximum width of a node
        for (Node n : CircuitNodes.values()) {
            if (maxwidth < n.width) {
                maxwidth = n.width;
            }
        }

        RowWidth = maxwidth + 50; // This Determines the total Allowed width of the rows

        long startTime = System.currentTimeMillis(); // Start Time for TimberWolf

        PerformTW(Temperature); // Function to start the TimberWolf Algorithm

        long endTime = System.currentTimeMillis(); // End Time for TimberWolf

        System.out.println("That took " + (endTime - startTime) + " milliseconds"); // Total Time of Execution

        int finalWirelength = calculateTotalWireLength(); // Calculating the final wirelength.
        System.out.println(" Wire Length after SA : " + finalWirelength);
        int finalChiparea = calculateTotalChipArea(); // Calculating the final Chip Area.

        double finalCost = calculateCombinedCost(finalWirelength, finalChiparea); // Calculating the final Cost of
                                                                                  // Placement.
        System.out
                .println(" Percent Improvement in Wire Length: "
                        + (((double) initialWirelength - (double) finalWirelength) / (double) initialWirelength) * 100);
        System.out.println(" Percent Improvement in Total Cost : " + ((initialCost - finalCost) / initialCost) * 100);
    }

    // Function to Perform TimberWolf
    public static void PerformTW(int NumberofMoves) {

        CreateInitialPlacement(RowWidth); // Initialize The Placement

        hasOverlaps(); // Check For Overlaps

        initialWirelength = calculateTotalWireLength(); // Get The initial WireLength
        System.out.println(" Wire Before  SA : " + initialWirelength);
        initialchipArea = calculateTotalChipArea(); // Get the inital Chip Area

        double currentCost = calculateCombinedCost(initialWirelength, initialchipArea); // Calculate the inital Cost

        double currentTemp = Temperature; // Define the starting Temperature

        initialCost = currentCost; // Store initial Cost

        while (currentTemp > terminationTemperature) { // Start the outer loop until it reaches termination temperature

            int inner = 0; // Variable to keep track of inner loop

            while (inner < maxMovesPerTemperature) { // Terminate inner loop if max number of iterations reached

                Object[] perturbation = perturb(); // Calls the perturb function defined earlier to move a cell.

                Node selectedCell = (Node) perturbation[0]; // Stores which Node is being moved

                int originalX = (int) perturbation[1]; // The original Xcoordinate of node(before move)

                int originalY = (int) perturbation[2]; // The original Ycoordinate of node(before move)

                double newCost = calculateCombinedCost(calculateTotalWireLength(),
                        calculateTotalChipArea()); // Calculates the new cost after the move

                double deltaCost = newCost - currentCost; // Differnece of New Cost and Previous Cost

                if (newCost < currentCost) { // If new Cost is less We accept it

                    currentCost = newCost;

                } else {
                    // If the new solution is worse, calculate an acceptance probability
                    double acceptanceProbability = accept(deltaCost, currentTemp);
                    if (Math.random() < acceptanceProbability) { // If the acceptance probability is less than the
                        currentCost = newCost; // random number generated, we reject it
                    } else {
                        // Revert the swap
                        selectedCell.xCoordinate = originalX; // If the move is rejected, Reset place the node to its
                        selectedCell.yCoordinate = originalY; // previous place
                    }
                }
                inner++; // Increment the inner loop counter
            }
            currentTemp *= coolingRate; // Update the temperature
        }

    }

    // Function to Create An Initial Placement
    public static void CreateInitialPlacement(int rowWidth) {
        // Separate macro cells, standard cells and pads (For placing the cells in a
        // better way)
        ArrayList<Node> macroCells = new ArrayList<>();
        ArrayList<Node> standardCells = new ArrayList<>();
        ArrayList<Node> padCells = new ArrayList<>();
        for (Node node : CircuitNodes.values()) {
            if (node.height > 16) {
                macroCells.add(node);
            } else if (node.height > 1) {
                standardCells.add(node);
            } else {
                padCells.add(node);
            }
        }

        // Sort standard cells by width
        standardCells.sort(Comparator.comparing(node -> node.width));

        // Create a list of Row objects for macro cells
        macroRows.add(new Row(0, rowWidth, horizontalSpacing));

        // Place macro cells randomly
        int currentMacroRow = 0;
        for (Node macro : macroCells) {
            while (!macroRows.get(currentMacroRow).canFitNode(macro)) { // Increment the number of rows
                currentMacroRow++; // until we find a row that can fit the cell.
                macroRows.add(new Row(
                        macroRows.get(currentMacroRow - 1).yCoordinate + macroRows.get(currentMacroRow - 1).RowHeight
                                + verticalSpacing,
                        rowWidth, horizontalSpacing));
            }
            macroRows.get(currentMacroRow).addNode(macro); // Add the cells to the rows.
        }

        // Calculate the starting y-coordinate for standard cells (The y-coordinate is
        // calculated by adding a vertical spacing to the last row added)
        int startY = macroRows.get(currentMacroRow).yCoordinate + macroRows.get(currentMacroRow).RowHeight
                + verticalSpacing;

        // Create a list of Row objects for standard cells
        standardrows.add(new Row(startY, rowWidth, horizontalSpacing));

        // Place standard cells
        int currentStandardRow = 0;
        for (Node standardCell : standardCells) {
            while (!standardrows.get(currentStandardRow).canFitNode(standardCell)) { // Increment the number of rows
                currentStandardRow++; // until we find a row that can fit the cell.
                standardrows.add(new Row(
                        standardrows.get(currentStandardRow - 1).yCoordinate
                                + standardrows.get(currentStandardRow - 1).RowHeight + verticalSpacing,
                        rowWidth, horizontalSpacing));
            }
            standardrows.get(currentStandardRow).addNode(standardCell); // Add the cells to the rows.
        }

        // Calculate the starting y-coordinate for pad cells (The y-coordinate is
        // calculated by adding a vertical spacing to the last row added)
        int startYPad = standardrows.get(currentStandardRow).yCoordinate
                + standardrows.get(currentStandardRow).RowHeight + verticalSpacing;

        // Create a list of Row objects for pad cells
        ArrayList<Row> padRows = new ArrayList<>();
        padRows.add(new Row(startYPad, rowWidth, horizontalSpacing));

        // Place pad cells
        int currentPadRow = 0;
        for (Node padCell : padCells) {
            while (!padRows.get(currentPadRow).canFitNode(padCell)) { // Increment the number of rows
                currentPadRow++; // until we find a row that can fit the cell.
                padRows.add(new Row(
                        padRows.get(currentPadRow - 1).yCoordinate
                                + padRows.get(currentPadRow - 1).RowHeight + verticalSpacing,
                        rowWidth, horizontalSpacing));
            }
            padRows.get(currentPadRow).addNode(padCell); // Add the cells to the rows.
        }

    }

    // Function to Check Overlaps
    public static void hasOverlaps() {
        // This checks the overlap for one node with all other nodes in the circuit
        for (Node node1 : CircuitNodes.values()) { // for each node
            for (Node node2 : CircuitNodes.values()) {
                if (node1 != node2) {

                    // X an Y coordinate of node 1
                    double x1 = node1.xCoordinate;
                    double y1 = node1.yCoordinate;
                    // Width and height of node 1
                    double w1 = node1.width;
                    double h1 = node1.height;

                    double x2 = node2.xCoordinate;
                    double y2 = node2.yCoordinate;
                    double w2 = node2.width;
                    double h2 = node2.height;
                    if (x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2) {
                        System.out.println("There is Overlap"); // Overlap was Found
                        return;
                    }
                }
            }
        }
        System.out.println(" No OverLaps Found"); // No overlaps were Detected
    }

    // Function to Perturb(Make a move) a node
    public static Object[] perturb() {
        int randomRowIndex;
        Row selectedRow;
        do {
            randomRowIndex = new Random().nextInt(standardrows.size()); // This Code Block Gets a random Row to select a
                                                                        // Node to Move from
            selectedRow = standardrows.get(randomRowIndex);
        } while (selectedRow.nodes.isEmpty()); // Check if the row is empty or not

        int randomCellIndex = new Random().nextInt(selectedRow.nodes.size()); // Select a random index from the row
        Node selectedCell = selectedRow.nodes.get(randomCellIndex); // Select the Node at that Random Index

        int originalX = selectedCell.xCoordinate; // Store original x and y coordinates if move is not accepted
        int originalY = selectedCell.yCoordinate;

        selectedRow.removeNode(selectedCell); // Remove the node from that row.

        // Choose a new row for the selected cell
        int newRowIdx = new Random().nextInt(standardrows.size());
        Row newRow = standardrows.get(newRowIdx);
        int newX = newRow.calculateRandomNewX(selectedCell); // Get a random new X for the node in that row

        // Add the selected cell to the new row and update its x and y coordinates
        newRow.insertNodeAtX(selectedCell, newX);

        return new Object[] { selectedCell, originalX, originalY }; // Return the moved node with its original
                                                                    // coordinates if move does not get accepted
    }

    // Function to Calculate the WireLength
    public static int calculateTotalWireLength() {
        int totalWireLength = 0;

        for (ArrayList<String> net : CircuitNets.values()) { // For each net in the circuit
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (String nodeId : net) { // For Each node in net
                // Gets the center for the node
                Node node = CircuitNodes.get(nodeId);
                int centerX = node.xCoordinate + node.width / 2;
                int centerY = node.yCoordinate + node.height / 2;
                // Maximum and minimum coordinates in the net
                minX = Math.min(minX, centerX);
                minY = Math.min(minY, centerY);
                maxX = Math.max(maxX, centerX);
                maxY = Math.max(maxY, centerY);
            }
            // Determines the distance
            int wireLength = (maxX - minX) + (maxY - minY);
            totalWireLength += wireLength;
        }

        return totalWireLength; // Returns the total wirelength
    }

    // Function to Calculate the ChipArea
    public static int calculateTotalChipArea() {
        int chipArea = 0;
        int chipWidth = CircuitNodes.values().stream().mapToInt(node -> node.xCoordinate + node.width).max().orElse(0);// Calculate
                                                                                                                       // the
                                                                                                                       // total
                                                                                                                       // Width
        int chipHeight = CircuitNodes.values().stream().mapToInt(node -> node.yCoordinate + node.height).max()// Calculate
                                                                                                              // the
                                                                                                              // total
                                                                                                              // Area
                .orElse(0);
        chipArea = chipWidth * chipHeight; // Total Chip area
        return chipArea;
    }

    // Function to determine the cost
    public static double calculateCombinedCost(int totalWireLength, int totalChipArea) {
        double combinedCost = 0.9 * totalWireLength + 0.1 * totalChipArea;
        return combinedCost;
    }

    // Function to Check the acceptance of a Move
    public static double accept(double deltaCost, double temperature) {
        if (deltaCost < 0) {
            return 1.0; // If the new cost is lower than the current cost, always accept the new
                        // placement
        } else {
            return Math.exp(-deltaCost / temperature); // If the new cost is higher than the current cost, accept the
                                                       // new placement
                                                       // with a probability that depends on the temperature
        }
    }

    // Row Class
    static class Row {
        private int yCoordinate; // Determines lower left ycoordinate of node
        private int currentX; // Determines the lower left xcoordinate of node
        private int rowWidth; // Total width of the row
        private List<Node> nodes; // List of nodes to store the node in each row
        private int horizontalSpacing; // Defines the Horizontal Spacing
        private int RowHeight = 0;
        private int RowID = 0;

        public Row(int yCoordinate, int rowWidth, int HS) { // This is the Constructor for the Row Class
            this.yCoordinate = yCoordinate;
            this.rowWidth = rowWidth;
            this.currentX = 0;
            this.horizontalSpacing = HS;
            nodes = new ArrayList<>();
            this.RowID = RowID++;
        }

        public int getYCoordinate() { // Gets The Ycoordinate for the row.
            return yCoordinate;
        }

        public List<Node> getNodes() { // Function to get the list of nodes in the row
            return nodes;
        }

        public boolean canFitNode(Node node) { // Checks if the new node fits in the row or not
            int currentWidth = nodes.stream().mapToInt(n -> n.width).sum(); // Total Occupied width of the Row
            return (currentWidth + node.width + horizontalSpacing) <= rowWidth; // Check if there is space for a new
                                                                                // Node
        }

        public void addNode(Node node) { // Adds the node into the rows
            node.setPlacementParameters(currentX, yCoordinate, node.orientation); // Update the x and y coordinates of
                                                                                  // the node

            nodes.add(node); // Add node to the list of nodes in the row

            currentX += node.width + horizontalSpacing; // Increment the current X that determines xcoordinate for the
                                                        // node

            if (node.height > RowHeight) { // Sets the height of Row as the nodes height if the nodes height is bigger
                                           // than initialized height
                RowHeight = node.height;
            }
        }

        public void removeNode(Node node) { // Remove the node from row for perturbation
            nodes.remove(node);
            currentX -= (node.width + horizontalSpacing); // Decrement the Current x for the new node that comes here
        }

        public void insertNodeAtX(Node node, int newX) { // Inserting node at a particular index in the row

            node.setPlacementParameters(newX, yCoordinate, node.orientation); // Set the new parameters for the Node

            nodes.add(node); // Add node to list of node

            int occupiedSpace = newX + node.width + horizontalSpacing; // Check if position available state
            if (occupiedSpace > currentX) { // If space is available add node and increment currentx for other node
                currentX = occupiedSpace;
            }

            if (node.height > RowHeight) { // Update the height of Row if necessary
                RowHeight = node.height;
            }
        }

        public int calculateRandomNewX(Node selectedCell) { // Generates a random integer to decide the xcoordinate of
                                                            // the node being moved
            int minX = horizontalSpacing;
            int maxX = rowWidth - selectedCell.width - horizontalSpacing;
            return new Random().nextInt(maxX - minX + 1) + minX;
        }
    }

}

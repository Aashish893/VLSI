import java.util.*;

class Node {
    public String nodeName;
    public int width;
    public int height;
    public int terminal;
    public int xCoordinate;
    public int yCoordinate;
    public int xBy2;
    public int yBy2;
    public String orientation;
    public ArrayList<Integer> Netlist = new ArrayList<>();

    public Node(String nodeName, int width, int height, int terminal) {
        this.nodeName = nodeName;
        this.width = width;
        this.height = height;
        this.terminal = terminal;
    }

    public void setNetlist(int Id) {
        Netlist.add(Id);
    }

    public void setPlacementParameters(int xCoordinate, int yCoordinate, String orientation) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.orientation = orientation;
    }

    public void setCenter(int xBy2, int yBy2) {
        this.xBy2 = xBy2;
        this.yBy2 = yBy2;
    }

}

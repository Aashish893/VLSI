import java.io.*;
import java.util.*;

public class Finalparser {

  // Parse the .net file and return a list of nets and their corresponding modules

  public static ArrayList<ArrayList<String>> parseNetFile(String fileName) throws IOException {

    ArrayList<ArrayList<String>> Edges = new ArrayList<ArrayList<String>>();
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      // Read in the header and ignore
      String line = reader.readLine();
      if (line == null) {
        throw new IllegalArgumentException("Invalid .net file format: header is missing");
      }

      line = reader.readLine();
      if (line == null) {
        throw new IllegalArgumentException("Invalid .net file format: header is missing");
      }

      line = reader.readLine();
      if (line == null) {
        throw new IllegalArgumentException("Invalid .net file format: header is missing");
      }

      line = reader.readLine();
      if (line == null) {
        throw new IllegalArgumentException("Invalid .net file format: header is missing");
      }

      line = reader.readLine();
      if (line == null) {
        throw new IllegalArgumentException("Invalid .net file format: header is missing");
      }

      // Parse each line individually
      Boolean flag = false;
      ArrayList<String> temp = new ArrayList<>();
      for (line = reader.readLine(); line != null; line = reader.readLine()) {

        if (line.contains("s")) {
          // flag = false;

          if (flag) {

            // hypergraph.addEdge(temp, temp.size() - 1);
            Edges.add(temp);
            temp = new ArrayList<>();
          }
          String[] N = line.trim().split("\\s+");
          temp.add(N[0]);
          flag = false;
        }

        if (!line.contains("s")) {
          flag = true;
          String[] N = line.trim().split("\\s+");
          temp.add(N[0]);

        }
      }
      Edges.add(temp);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return Edges;
  }

  // Parse the .are file and return a map of module names and their areas
  public static Map<String, Integer> parseAreFile(String fileName) {
    Map<String, Integer> areas = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] fields = line.split("\\s+");
        String moduleName = fields[0];
        int area = Integer.parseInt(fields[1]);
        // System.out.println(moduleName);
        areas.put(moduleName, area);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return areas;
  }

  public static void main(String[] args) throws IOException {
    String netFileName = "ibm02.net";
    String areFileName = "ibm02.are";

    // Map<String, ArrayList<String>> edges = parseNetFile(netFileName);
    ArrayList<ArrayList<String>> edges = parseNetFile(netFileName);
    Map<String, Integer> areas = parseAreFile(areFileName);

    Map<String, Cell> cellMap = new HashMap<>();
    for (Map.Entry<String, Integer> entry : areas.entrySet()) {
      String cellId = entry.getKey();
      int cellArea = entry.getValue();
      Cell cell = new Cell(cellId, cellArea);
      cellMap.put(cellId, cell);
    }

    HashSet<Cell> CellSet = new HashSet<>();

    // Extract the Cell objects from the Map and add them to the HashSet
    for (Cell cell : cellMap.values()) {
      CellSet.add(cell);
    }
    // create a map to store the Net objects
    // HashMap<String, Net> nets = new HashMap<>();

    // iterate over each Cell object
    HashSet<Net> nets = new HashSet<>();

    for (ArrayList<String> cellNames : edges) {
      Net net = new Net("net_" + nets.size());

      for (String cellName : cellNames) {
        Cell cell = cellMap.get(cellName);
        net.addCell(cell);
        cell.addNet(net);
      }

      nets.add(net);
    }

    // Hypergraph hypergraph = new Hypergraph();
    // hypergraph.createHypergraph(CellSet, nets);
    // Fiduccia.performFM(nets, cellMap);
    Fiduccia.perform(nets, cellMap);

  }

}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class parser {
    public static Map<String, Node> CircuitNodes = new HashMap<>();
    public static Map<Integer, ArrayList<String>> CircuitNets = new HashMap<>();

    public static void NodeParser(String filename) {
        int i = 0;
        int val = 2;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Read in the header and ignore
            String line;
            while ((line = reader.readLine()) != null) {
                i++;
                if (i > 7) {
                    String[] fields = line.replaceFirst("^\\W+", "").split("\\W+");
                    String nodeName = fields[0];
                    if (fields.length == 4) {
                        val = 1;
                    } else {
                        val = 0;
                    }
                    Node n = new Node(nodeName, Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), val);
                    CircuitNodes.put(nodeName, n);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void NetsParser(String filename) {
        int i = 0, a = 0, j = 0;
        int NetID = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Read in the header and ignore
            String line;
            while ((line = reader.readLine()) != null) {
                i++;
                if (i > 7) {
                    ArrayList<String> tempNets = new ArrayList<String>();
                    String[] fields = line.replaceFirst("^\\W+", "").split("\\W+");
                    a = Integer.parseInt(fields[1]);
                    for (j = 0; j < a; j++) {
                        line = reader.readLine();
                        String[] temp = line.replaceFirst("^\\W+", "").split("\\W+");
                        String nodename = temp[0];
                        tempNets.add(nodename);
                        Node n = CircuitNodes.get(nodename);
                        n.setNetlist(NetID);
                    }
                    CircuitNets.put(NetID, tempNets);
                    NetID++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void PlParser(String filename) {
        int i = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Read in the header and ignore
            String line;
            while ((line = reader.readLine()) != null) {
                i++;
                if (i > 6) {

                    String[] fields = line.replaceFirst("^\\W+", "").split("\\W+");
                    Node n = CircuitNodes.get(fields[0]);
                    n.setPlacementParameters(Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), fields[3]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

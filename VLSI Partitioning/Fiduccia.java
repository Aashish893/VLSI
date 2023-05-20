import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class Division {
    int size;
    double maxArea;
    double currArea;
    HashMap<Integer, Bucket> node;

    Division() {
        size = 0;
        maxArea = 0;
        currArea = 0;
        node = new HashMap<>();
    }
}

class NodeCutSize {
    int cutsize;
    Cell cell;

    NodeCutSize(int ct, Cell c) {
        cutsize = ct;
        cell = c;
    }
}

public class Fiduccia {
    public static HashSet<Net> net;
    public static Map<String, Cell> cellMap;
    public static int MAX_GAIN = 0;
    public static double PERCENTAGE = 0;
    public static double DATA_AREA = 0;
    public static Division leftBucket;
    public static Division rightBucket;
    public static ArrayList<NodeCutSize> visitedNodes = new ArrayList<NodeCutSize>();
    public static Integer initialcutsize;

    public static void perform(HashSet<Net> nets, Map<String, Cell> cm) {
        cellMap = cm;
        net = nets;
        randomizePartitions();
        updateMaxGain();
        calculateGains();
        initializeBuckets(0.5);
        initialcutsize = calculateInitialCutsize();
        System.out.println("INITIAL CUT SIZE " + initialcutsize);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < cellMap.size(); i++)
            fm();
        long endTime = System.currentTimeMillis();
        System.out.println("That took " + (endTime - startTime) + " milliseconds");
        System.out.println("MAX GAIN " + MAX_GAIN);
        System.out.println("NO OF CELLS " + cellMap.size());
        printOutput();
    }

    public static void performFM(HashSet<Net> nets, Map<String, Cell> cm) {
        cellMap = cm;
        net = nets;

        // int initialcutsize = GetCutSize(net);
        System.out.println(initialcutsize);
        Map<Integer, Bucket> bucketMap = new HashMap<>();
        for (Cell cell : cellMap.values()) {
            int gain = cell.getGain();
            if (!bucketMap.containsKey(gain)) {
                Bucket newBucket = new Bucket(cell);
                bucketMap.put(gain, newBucket);
                // cell.setBucketNode(bucketMap.get(gain));
            } else {// Get the bucket for this gain value and add the cell to it
                Bucket HeadBucket = bucketMap.get(gain);
                HeadBucket = push(HeadBucket, cell);
                bucketMap.put(gain, HeadBucket);
                // cell.setBucketNode(HeadBucket);
            }
        }

        long startTime = System.currentTimeMillis();
        // for (int a = 0; a < cellMap.size();) {
        while (!bucketMap.isEmpty()) {
            int max = Collections.max(bucketMap.keySet());
            if (max < 0)
                break;
            Bucket bucket = bucketMap.get(max);
            if (bucket == null) {
                System.out.println("null");
            }
            while (bucket != null) {
                Bucket Head = bucket;
                Head = deleteNode(Head, bucket);

                if (Head == null) {
                    bucketMap.remove(max);
                } else {
                    bucketMap.put(max, Head);
                }

                cellMap.get(bucket.cell.getId()).setLocked(true);
                cellMap.get(bucket.cell.getId()).setPartition(1 - cellMap.get(bucket.cell.getId()).getPartition());

                for (Net n : cellMap.get(bucket.cell.getId()).getNets()) {
                    int prevLeft = n.leftcount;
                    int prevRight = n.rightcount;
                    Boolean prevZero = (prevLeft == 0 || prevRight == 0);
                    if (cellMap.get(bucket.cell.getId()).getPartition() == 0) {
                        n.leftcount -= 1;
                        n.rightcount += 1;
                    } else {
                        n.leftcount += 1;
                        n.rightcount -= 1;
                    }
                    int currLeft = n.leftcount;
                    int currRight = n.rightcount;
                    Boolean currZero = (currLeft == 0 || currRight == 0);
                    if (prevZero && !currZero)
                        initialcutsize += 1;
                    else if (!prevZero && currZero)
                        initialcutsize -= 1;
                }
                UpdateNets(cellMap.get(bucket.cell.getId()).getId(), cellMap.get(bucket.cell.getId()));
                int newGain = bucket.cell.calculateGain();
                cellMap.get(bucket.cell.getId()).setOldGain(bucket.cell.getGain());
                cellMap.get(bucket.cell.getId()).setGain(newGain);
                updateGains(bucket.cell, bucketMap);
                bucket = bucket.next;
            }

        }
        System.out.println("Final Linked Lists");
        System.out.println("Net-cut cost: " + initialcutsize);
        long endTime = System.currentTimeMillis();
        System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }

    public static void UpdateNets(String CellId, Cell currentcell) {
        try {
            for (Net n : net) {
                for (Cell cell : n.getCells()) {
                    if (cell.getId() == CellId) {
                        cell = currentcell;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static void updatebucketofHashSet(Cell currentcell, Map<Integer, HashSet<Cell>> BucketMapWithHashSet) {
        HashSet<String> Considered = new HashSet<>();
        for (Net n : currentcell.getNets()) {
            for (Cell neighbor : n.getCells()) {
                if (neighbor != currentcell && !Considered.contains(neighbor.getId())) {
                    Considered.add(neighbor.getId());
                    int oldgainofneighbor = neighbor.getGain();
                    neighbor.setOldGain(oldgainofneighbor);
                    neighbor.setGain(neighbor.calculateGain());
                    int newgainofneighbor = neighbor.getGain();
                    HashSet<Cell> OldSetofNeighbor = BucketMapWithHashSet.get(neighbor.getGain());
                    HashSet<Cell> NewSetofNeighbor = BucketMapWithHashSet.getOrDefault(newgainofneighbor,
                            new HashSet<Cell>());
                    OldSetofNeighbor.remove(neighbor);
                    BucketMapWithHashSet.put(oldgainofneighbor, OldSetofNeighbor);
                    NewSetofNeighbor.add(neighbor);
                    BucketMapWithHashSet.put(newgainofneighbor, NewSetofNeighbor);
                }
            }
        }
    }

    public static int GetCutSize(HashSet<Net> Nets) {
        int cutsize = 0;
        for (Net net : Nets) {
            boolean inPartition0 = false;
            boolean inPartition1 = false;
            for (Cell cell : net.getCells()) {
                if (cell.getPartition() == 0) {
                    inPartition0 = true;
                } else {
                    inPartition1 = true;
                }
            }
            if (inPartition0 && inPartition1) {
                cutsize++;
            }
        }
        return cutsize;
    }

    // function to update the gains of the cells and their neighbors
    public static void updateGains(Cell currentcell, Map<Integer, Bucket> bucketMap) {
        HashSet<String> Considered = new HashSet<>();
        for (Net net : currentcell.getNets()) {
            for (Cell neighbor : net.getCells()) {
                boolean found = false;
                if (neighbor != currentcell && !Considered.contains(neighbor.getId())) {
                    Considered.add(neighbor.getId());
                    Bucket Head = bucketMap.get(neighbor.getGain());
                    int neighborGain = neighbor.getGain();
                    int newNeighborGain = neighbor.calculateGain();
                    cellMap.get(neighbor.getId()).setOldGain(neighborGain);
                    cellMap.get(neighbor.getId()).setGain(newNeighborGain);
                    Bucket Temp = Head;
                    if (neighborGain == newNeighborGain)
                        continue;
                    while (Temp != null) {
                        if (Temp.cell.getId() == neighbor.getId()) {
                            Head = deleteNode(Head, Temp);
                            if (Head == null) {
                                // Bucket newBuck = new Bucket(neighbor);
                                bucketMap.remove(neighborGain);

                            } else {
                                bucketMap.put(neighborGain, Head);
                            }
                            Bucket CurrBucket = bucketMap.get(newNeighborGain);
                            CurrBucket = push(CurrBucket, neighbor);
                            if (CurrBucket == null) {
                                break;
                            }
                            bucketMap.put(newNeighborGain, CurrBucket);
                            break;

                        }
                        Temp = Temp.next;

                    }

                    if (!found && !Considered.contains(neighbor.getId())) {
                        System.out.println(neighbor.getId() + "not");
                        break;
                    }

                }
            }

        }
    }

    static Bucket deleteNode(Bucket head, Bucket del) {
        // base case
        if (head == null || del == null)
            return null;

        // If node to be deleted is head node
        if (head == del)
            head = del.next;

        // Change next only if node to be
        // deleted is NOT the last node
        if (del.next != null)
            del.next.prev = del.prev;

        // Change prev only if node to be
        // deleted is NOT the first node
        if (del.prev != null)
            del.prev.next = del.next;

        del = null;

        return head;
    }

    static Bucket push(Bucket head, Cell cell) {
        // allocate node
        Bucket new_node = new Bucket(cell);

        // since we are adding at the beginning,
        // prev is always NULL

        new_node.prev = null;

        // link the old list of the new node
        new_node.next = head;

        // change prev of head node to new node
        if (head != null)
            head.prev = new_node;

        // move the head to point to the new node
        head = new_node;
        return head;
    }

    // function to update the buckets after a cell is moved to a different gain
    // value
    public static void updateBuckets(Map<String, Cell> cellMap, Map<Integer, Bucket> bucketMap) {
        // iterate over the cells and update their positions in the buckets
        for (Cell cell : cellMap.values()) {
            int oldGain = cell.getOldGain();
            int newGain = cell.getGain();
            if (oldGain != newGain) {
                Bucket oldBucket = bucketMap.get(oldGain);
                oldBucket.prev.next = oldBucket.next;
                oldBucket.next.prev = oldBucket.prev;
                Bucket TempBucket = oldBucket;
                oldBucket = oldBucket.next;
                TempBucket.prev = TempBucket.next = null;
                if (!bucketMap.containsKey(newGain)) {
                    Bucket newBucket = new Bucket(cell);
                    bucketMap.put(newGain, newBucket);
                    cell.setBucketNode(bucketMap.get(newGain));
                } else {// Get the bucket for this gain value and add the cell to it
                    Bucket HeadBucket = bucketMap.get(newGain);
                    Bucket CurrentBucket = HeadBucket;
                    CurrentBucket.prev = TempBucket;
                    HeadBucket = TempBucket;
                    HeadBucket.next = CurrentBucket;
                    bucketMap.put(newGain, HeadBucket);
                    cell.setBucketNode(HeadBucket);
                }
            }
        }
    }

    // public static Bucket getBucketWithHighestGain(Map<Integer, Bucket> bucketMap)
    // {
    // // iterate over the buckets and find the one with the highest gain
    // int maxGain = Integer.MIN_VALUE;
    // Bucket maxGainBucket = null;
    // for (Bucket bucket : bucketMap.values()) {
    // int gain = bucket.getMaxGain();
    // if (gain > maxGain) {
    // maxGain = gain;
    // maxGainBucket = bucket;
    // }
    // }
    // return maxGainBucket;
    // }

    static void randomizePartitions() {
        int i = 0;
        int area = 0;
        for (Cell cell : cellMap.values()) {
            area += cell.getArea();
            if (i % 2 == 0) {
                cell.setPartition(0);
                for (Net n : cell.getNets()) {
                    n.leftcount++;
                }
            } else {
                cell.setPartition(1);
                for (Net n : cell.getNets()) {
                    n.rightcount++;
                }
            }
            i++;
        }
        DATA_AREA = area;
    }

    static void updateMaxGain() {
        int maxGain = 0;

        for (Cell cell : cellMap.values()) {
            if (cell.getNets().size() > maxGain)
                maxGain = cell.getNets().size();
        }
        MAX_GAIN = maxGain;

    }

    static void intializeGain() {
        for (Cell cell : cellMap.values()) {
            cell.setGain(cell.calculateGain());
            cell.setOldGain(cell.getGain());
        }
    }

    static void initializeBuckets(double areaPercentage) {
        leftBucket = new Division();
        rightBucket = new Division();
        PERCENTAGE = areaPercentage;
        double bucketArea = DATA_AREA * areaPercentage;

        leftBucket.maxArea = bucketArea;
        rightBucket.maxArea = DATA_AREA - bucketArea;

        for (Cell cell : cellMap.values()) {
            if (cell.getPartition() == 0) {
                leftBucket.currArea += cell.getArea();
                if (leftBucket.node.containsKey(cell.getGain())) {
                    Bucket head = leftBucket.node.get(cell.getGain());
                    head = push(head, cell);
                    leftBucket.node.put(cell.getGain(), head);
                } else
                    leftBucket.node.put(cell.getGain(), new Bucket(cell));
            } else {
                rightBucket.currArea += cell.getArea();
                if (rightBucket.node.containsKey(cell.getGain())) {
                    Bucket head = rightBucket.node.get(cell.getGain());
                    head = push(head, cell);
                    rightBucket.node.put(cell.getGain(), head);
                } else
                    rightBucket.node.put(cell.getGain(), new Bucket(cell));
            }
        }
    }

    static int calculateInitialCutsize() {
        HashSet<Net> vis = new HashSet<>();
        int cutsize = 0;
        for (Cell cell : cellMap.values()) {
            for (Net n : cell.getNets()) {
                if (vis.contains(n))
                    continue;
                if (n.leftcount != 0 && n.rightcount != 0)
                    cutsize++;
                vis.add(n);
            }

        }
        return cutsize;
    }

    static Bucket findMaximumGainNodeInBucket(int maxGain, HashMap<Integer, Bucket> node) {
        for (int i = maxGain; i >= -maxGain; i--) {
            Bucket head = node.get(i);
            while (head != null) {
                return head;
            }
        }
        return null;
    }

    static Bucket findMaximumGainNode() {

        if (leftBucket.currArea > leftBucket.maxArea) {
            return findMaximumGainNodeInBucket(MAX_GAIN, leftBucket.node);
        }
        if (rightBucket.currArea > rightBucket.maxArea) {
            return findMaximumGainNodeInBucket(MAX_GAIN, rightBucket.node);
        }
        // if (leftBucket->size > rightBucket->size) {
        // return leftBucket->findMaximumGainNodeInBucket(data.maxGain);
        // } else if (rightBucket->size > leftBucket->size) {
        // return rightBucket->findMaximumGainNodeInBucket(data.maxGain);
        // } else {
        Bucket leftNode = findMaximumGainNodeInBucket(MAX_GAIN, leftBucket.node);
        Bucket rightNode = findMaximumGainNodeInBucket(MAX_GAIN, rightBucket.node);
        //
        if (leftNode == null && rightNode == null)
            return null;
        if (leftNode == null)
            return rightNode;
        if (rightNode == null)
            return leftNode;
        //
        return (leftNode.cell.getGain() > rightNode.cell.getGain()) ? leftNode : rightNode;
        // }
    }

    static Bucket deletenth(String id, Bucket head) {
        // if (head == null)
        // return head;
        // if (head.next == null)
        // return null;
        // // Bucket current = head;
        // int n = 0;
        // while (head != null && head.cell.getId() != id) {
        // head = head.next;
        // n++;
        // }
        // if (head == null)
        // return null;
        // if (head.prev != null)
        // head.prev.next = head.next;
        // if (head.next != null)
        // head.next.prev = head.prev;
        // // Delete the middle node
        // // current = null;
        // return head;
        if (head == null)
            return null;

        Bucket current = head;

        /*
         * traverse up to the node at
         * position 'n' from the beginning
         */
        while (current != null && current.cell.getId() != id) {
            current = current.next;
        }

        // if 'n' is greater than the number of nodes
        // in the doubly linked list
        if (current == null)
            return null;

        // delete the node pointed to by 'current'
        return deleteNode(head, current);
    }

    static void fm() {
        Bucket node = findMaximumGainNode();
        if (node == null)
            return;
        node.cell.setLocked(true);
        int cutsize = (visitedNodes.isEmpty()) ? initialcutsize : visitedNodes.get(visitedNodes.size() - 1).cutsize;
        for (Net n : node.cell.getNets()) {
            int prevLeft = n.leftcount;
            int prevRight = n.rightcount;
            Boolean prevZero = (prevLeft == 0 || prevRight == 0);
            if (node.cell.getPartition() == 0) {
                n.leftcount -= 1;
                n.rightcount += 1;
            } else {
                n.leftcount += 1;
                n.rightcount -= 1;
            }
            int currLeft = n.leftcount;
            int currRight = n.rightcount;
            Boolean currZero = (currLeft == 0 || currRight == 0);
            if (prevZero && !currZero)
                cutsize += 1;
            else if (!prevZero && currZero)
                cutsize -= 1;
        }
        // System.out.println("New cutsize: " + cutsize);
        Division bucket = (node.cell.getPartition() == 0) ? leftBucket : rightBucket;
        bucket.currArea -= node.cell.getArea();
        Bucket head = bucket.node.get(node.cell.getGain());
        head = deleteNode(head, node);
        if (head != null)
            bucket.node.put(node.cell.getGain(), head);
        else
            bucket.node.remove(node.cell.getGain());
        for (Net n : node.cell.getNets()) {
            for (Cell c : n.getCells()) {
                if (c.getId() == node.cell.getId()) {
                    continue;
                }
                if (c.isLocked()) {
                    continue;
                }
                int previousGain = c.getGain();
                int newGain = c.calculateGain();
                if (newGain != previousGain) {
                    c.setGain(newGain);
                    Division netBucketDivision = (c.getPartition() == 0) ? leftBucket : rightBucket;
                    // netBucketDivision.currArea -= c.getArea();
                    Bucket headNOde = netBucketDivision.node.get(previousGain);
                    Bucket newBucket = netBucketDivision.node.get(newGain);
                    headNOde = deletenth(c.getId(), headNOde);
                    newBucket = push(newBucket, c);
                    netBucketDivision.node.put(newGain, newBucket);
                    if (headNOde != null)
                        netBucketDivision.node.put(previousGain, headNOde);
                    else
                        netBucketDivision.node.remove(previousGain);
                }
            }
        }

        node.cell.setPartition(1 - node.cell.getPartition());
        Division newPartitDivision = (node.cell.getPartition() == 0) ? leftBucket : rightBucket;
        newPartitDivision.currArea += node.cell.getArea();
        NodeCutSize newnodecut = new NodeCutSize(cutsize, node.cell);
        visitedNodes.add(newnodecut);
    }

    static void printOutput() {
        int minCutsize = Integer.MAX_VALUE;
        for (int i = 0; i < visitedNodes.size(); i++) {
            if (minCutsize > visitedNodes.get(i).cutsize)
                minCutsize = visitedNodes.get(i).cutsize;
        }
        System.out.println("FINAL CUT SIZE " + minCutsize);
        System.out.println("TOTAL VISITED " + visitedNodes.size());
    }

    static void calculateGains() {
        for (Cell c : cellMap.values()) {
            c.setGain(c.calculateGain());
        }
    }

}

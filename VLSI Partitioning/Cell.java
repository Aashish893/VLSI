import java.util.HashSet;

public class Cell {
    private final String id;
    private int partition;
    private int gain;
    private boolean locked;
    private int area;
    private HashSet<Net> nets;
    private Bucket bucketNode;
    private int oldgain;

    public Cell(String id, int area) {
        this.id = id;
        this.partition = -1; // not yet assigned to a partition
        this.gain = 0;
        this.locked = false;
        this.area = area;
        this.nets = new HashSet<>();
        this.oldgain = 0;

    }

    public String getId() {
        return id;
    }

    public int getPartition() {
        try {
            return partition;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void setPartition(int partition) {
        try {
            this.partition = partition;
        } catch (Exception ex) {
            throw ex;
        }

    }

    public int getGain() {
        return gain;
    }

    public int getOldGain() {
        return oldgain;
    }

    public void setGain(int gain) {

        this.gain = gain;
    }

    public void setOldGain(int gain) {

        this.oldgain = gain;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public HashSet<Net> getNets() {
        return nets;
    }

    public void addNet(Net net) {
        this.nets.add(net);
    }

    public void removeNet(Net net) {
        this.nets.remove(net);
    }

    public int calculateGain() {

        int currentpartition = this.getPartition();
        int gain = 0;
        for (Net n : this.getNets()) {
            if (currentpartition == 0) {
                if (n.leftcount == 1) {
                    gain++;
                }
                if (n.rightcount == 0) {
                    gain--;
                }
            } else {
                if (n.rightcount == 1) {
                    gain++;
                }
                if (n.leftcount == 0) {
                    gain--;
                }
            }
        }
        return gain;
    }

    public void setBucketNode(Bucket node) {
        this.bucketNode = node;
    }

    public Bucket getBucketNode() {
        return bucketNode;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "id='" + id + '\'' +
                ", partition=" + partition +
                ", gain=" + gain +
                ", locked=" + locked +
                ", area=" + area +
                ", oldgain=" + oldgain +
                '}';
    }

}
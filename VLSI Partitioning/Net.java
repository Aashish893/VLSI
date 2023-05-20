import java.util.HashSet;

public class Net {
    private final String id;
    private HashSet<Cell> cells;
    public int leftcount;
    public int rightcount;

    public Net(String id) {
        this.id = id;
        this.cells = new HashSet<>();
        this.leftcount = 0;
        this.rightcount = 0;
    }

    public String getId() {
        return id;
    }

    // public void setleftcount(int count) {
    // this.leftcount = count;
    // }

    // public void setrightcount(int count) {
    // this.rightcount = count;
    // }

    public HashSet<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    public void removeCell(Cell cell) {
        this.cells.remove(cell);
    }

    public int computeDeltaGain(Cell cell, int targetPartition) {
        int numInTargetPartition = 0;
        int numInOtherPartition = 0;
        int gain = 0;
        for (Cell otherCell : cells) {
            if (otherCell == cell) {
                continue;
            }

            if (otherCell.getPartition() == targetPartition) {
                numInTargetPartition++;
            } else {
                numInOtherPartition++;
            }
        }

        if (cell.getPartition() == targetPartition) {
            gain = numInOtherPartition - numInTargetPartition;
        } else {
            gain = numInTargetPartition - numInOtherPartition;
        }

        return gain;
    }

    @Override
    public String toString() {
        return "Net{" +
                "id='" + id + '\'' +
                '}';
    }
}

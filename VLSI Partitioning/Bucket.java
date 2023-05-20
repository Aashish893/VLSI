public class Bucket {
    public Cell cell;
    public Bucket next;
    public Bucket prev;
    public Boolean skip;

    public Bucket(Cell cell) {
        this.cell = cell;
        next = null;
        prev = null;
        skip = false;
    }

    // function for deleting Nth node

}

class DListNode {
    public Cell cell;
    public int gain;
    DListNode prev;
    DListNode next;

    public DListNode(Cell cell, int gain) {
        this.cell = cell;
        this.gain = gain;
        prev = next = null;
    }

    public Cell getCell() {
        return cell;
    }

    public int getGain() {
        return gain;
    }
}

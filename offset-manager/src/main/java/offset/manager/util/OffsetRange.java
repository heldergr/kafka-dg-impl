package offset.manager.util;

public class OffsetRange {
    private long firstOffset;
    private long lastOffset;

    public OffsetRange(final long firstOffset, final long lastOffset) {
        this.firstOffset = firstOffset;
        this.lastOffset = lastOffset;
    }

    @Override
    public String toString() {
        return "OffsetRange{" +
                "firstOffset=" + firstOffset +
                ", lastOffset=" + lastOffset +
                '}';
    }

    public long getFirstOffset() {
        return firstOffset;
    }

    public void setFirstOffset(long firstOffset) {
        this.firstOffset = firstOffset;
    }

    public long getLastOffset() {
        return lastOffset;
    }

    public void setLastOffset(long lastOffset) {
        this.lastOffset = lastOffset;
    }

    /**
     * Returns a random offset between the range represented in the object.
     *
     * @return A random offset
     */
    public long randomOffset() {
        final var offsetsOffset = this.lastOffset - this.firstOffset + 1;
        return this.firstOffset + (long)(Math.random() * offsetsOffset);
     }

    public static void main(String[] args) {
        var or = new OffsetRange(1, 5);
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println("####################");
        or = new OffsetRange(10, 25);
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
        System.out.println(or.randomOffset());
    }
}

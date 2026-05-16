package com.jjjcfy.greatech.content.wireless;

public class DimensionEuPool {
    private long stored;
    private long capacity;

    public DimensionEuPool(long capacity) {
        this.capacity = Math.max(0L, capacity);
    }

    public long stored() {
        return stored;
    }

    public long capacity() {
        return capacity;
    }

    public long remainingCapacity() {
        return Math.max(0L, capacity - stored);
    }

    public void setStored(long stored) {
        this.stored = Math.max(0L, Math.min(stored, capacity));
    }

    public void setCapacity(long capacity) {
        this.capacity = Math.max(0L, capacity);
        if (stored > this.capacity) {
            stored = this.capacity;
        }
    }

    public long insert(long maxEu, boolean simulate) {
        long inserted = Math.min(Math.max(0L, maxEu), remainingCapacity());
        if (!simulate && inserted > 0) {
            stored += inserted;
        }
        return inserted;
    }

    public long extract(long maxEu, boolean simulate) {
        long extracted = Math.min(Math.max(0L, maxEu), stored);
        if (!simulate && extracted > 0) {
            stored -= extracted;
        }
        return extracted;
    }
}

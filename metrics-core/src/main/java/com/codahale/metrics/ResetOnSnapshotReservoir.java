package com.codahale.metrics;

/**
 * A Reservoir [backed by a UniformReservoir] that resets its internal state on each snapshot.
 */
public class ResetOnSnapshotReservoir implements Reservoir {
    private Reservoir reservoir;

    public ResetOnSnapshotReservoir() {
        this.reservoir = getNewReservoir();
    }

    @Override
    public void update(long value) {
        reservoir.update(value);
    }

    @Override
    public Snapshot getSnapshot() {
        Reservoir reservoirCopy = reservoir;
        reservoir = getNewReservoir();
        return reservoirCopy.getSnapshot();
    }

    @Override
    public int size() {
        return reservoir.size();
    }

    private Reservoir getNewReservoir() {
        return new UniformReservoir();
    }
}

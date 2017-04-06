package com.codahale.metrics;

import static java.util.Arrays.binarySearch;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author bstorozhuk
 */
public class ChunkedAssociativeLongArray {
    private static final long[] EMPTY = new long[0];
    private static final int DEFAULT_CHUNK_SIZE = 512;

    private final ReentrantLock activeChunkLock = new ReentrantLock();
    private final int chunkSize;
    private Chunk activeChunk;

    public ChunkedAssociativeLongArray() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public ChunkedAssociativeLongArray(int chunkSize) {
        this.chunkSize = chunkSize;
        this.activeChunk = new Chunk(chunkSize);
    }

    public void put(long key, long value) {
        activeChunkLock.lock();
        try {
            boolean isFull = activeChunk.cursor - activeChunk.startIndex == activeChunk.chunkSize;
            if (isFull) {
                activeChunk = new Chunk(activeChunk, this.chunkSize);
            }
            activeChunk.append(key, value);
        } finally {
            activeChunkLock.unlock();
        }
    }

    public int traverse(Deque<Chunk> traversedChunksDeque) {
        Chunk currentChunk;
        activeChunkLock.lock();
        try {
            currentChunk = activeChunk;
        } finally {
            activeChunkLock.unlock();
        }

        int valuesSize = 0;
        while (true) {
            valuesSize += currentChunk.cursor - currentChunk.startIndex;
            if (traversedChunksDeque != null) {
                traversedChunksDeque.addLast(currentChunk);
            }
            if (currentChunk.tailChunk == null) {
                break;
            }
            currentChunk = currentChunk.tailChunk;
        }

        return valuesSize;
    }

    public long[] values() {
        Deque<Chunk> chunksDeque = new ArrayDeque<Chunk>();
        int valuesSize = traverse(chunksDeque);
        if (valuesSize == 0) {
            return EMPTY;
        }

        long[] values = new long[valuesSize];
        int valuesIndex = 0;
        while (!chunksDeque.isEmpty()) {
            Chunk copySourceChunk = chunksDeque.removeLast();
            int length = copySourceChunk.cursor - copySourceChunk.startIndex;
            System.arraycopy(copySourceChunk.values, copySourceChunk.startIndex, values, valuesIndex, length);
            valuesIndex += length;
        }

        return values;
    }

    public int size() {
        int valueSize = traverse(null);
        return valueSize;
    }

    String out() {
        ArrayDeque<Chunk> chunksDeque = new ArrayDeque<Chunk>();
        int valuesSize = traverse(chunksDeque);
        if (valuesSize == 0) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        while (!chunksDeque.isEmpty()) {
            Chunk copySourceChunk = chunksDeque.removeLast();
            builder.append('[');
            for (int i = copySourceChunk.startIndex; i < copySourceChunk.cursor; i++) {
                long key = copySourceChunk.keys[i];
                long value = copySourceChunk.values[i];
                builder.append('(').append(key).append(": ").append(value).append(')').append(' ');
            }
            builder.append(']');
            if (!chunksDeque.isEmpty()) {
                builder.append("->");
            }
        }
        return builder.toString();
    }

    /**
     * Try to trim all beyond specified boundaries.
     * All items that are less then startKey or greater/equals then endKey
     *
     * @param startKey
     * @param endKey
     */
    public void trim(long startKey, long endKey) {
        /*
         * [3, 4, 5, 9] -> [10, 13, 14, 15] -> [21, 24, 29, 30] -> [31] :: start layout
         *       |5______________________________23|                    :: trim(5, 23)
         *       [5, 9] -> [10, 13, 14, 15] -> [21]                     :: result layout
         */
        Chunk head;
        activeChunkLock.lock();
        try {
            head = findChunkWhereKeyShouldBe(activeChunk, endKey);
            activeChunk = head;
            int newEndIndex = findFirstIndexOfGreaterEqualElements(
                activeChunk.keys, activeChunk.startIndex, activeChunk.cursor, endKey
            );
            activeChunk.cursor = newEndIndex;

        } finally {
            activeChunkLock.unlock();
        }

        Chunk tail = findChunkWhereKeyShouldBe(head, startKey);
        int newStartIndex = findFirstIndexOfGreaterEqualElements(
            tail.keys, tail.startIndex, tail.cursor, startKey
        );
        tail.startIndex = newStartIndex;
        tail.chunkSize = tail.cursor - tail.startIndex;
        tail.tailChunk = null; // get rid of all forward chunks
    }

    /**
     * Clear all in specified boundaries.
     * Remove all items between startKey(inclusive) and endKey(exclusive)
     *
     * @param startKey
     * @param endKey
     */
    public void clear(long startKey, long endKey) {
        /*
         * [3, 4, 5, 9] -> [10, 13, 14, 15] -> [21, 24, 29, 30] -> [31] :: start layout
         *       |5______________________________23|                    :: clear(5, 23)
         * [3, 4]               ->                 [24, 29, 30] -> [31] :: result layout
         */
        Chunk tail;
        activeChunkLock.lock();
        try {
            tail = findChunkWhereKeyShouldBe(activeChunk, endKey);
        } finally {
            activeChunkLock.unlock();
        }

        Chunk gapStartChunk = splitChunkOnTwoSeparateChunks(tail, endKey);
        // now we should skip specified gap [startKey, endKey]
        // and concatenate our tail with new head four after gap
        Chunk afterGapHead = findChunkWhereKeyShouldBe(gapStartChunk, startKey);
        if (afterGapHead != null) {
            int newEndIndex = findFirstIndexOfGreaterEqualElements(
                afterGapHead.keys, afterGapHead.startIndex, afterGapHead.cursor, startKey
            );
            afterGapHead.cursor = newEndIndex;
            afterGapHead.chunkSize = afterGapHead.cursor - afterGapHead.startIndex;
        }
        tail.tailChunk = afterGapHead; // concat
    }

    private Chunk splitChunkOnTwoSeparateChunks(Chunk chunk, long key) {
        /*
         * [1, 2, 3, 4, 5, 6, 7, 8] :: beforeSplit
         * |s--------chunk-------e|
         *
         *  splitChunkOnTwoSeparateChunks(chunk, 5)
         *
         * [1, 2, 3, 4, 5, 6, 7, 8] :: afterSplit
         * |s--tail--e||s--head--e|
         */
        int splitIndex = findFirstIndexOfGreaterEqualElements(
            chunk.keys, chunk.startIndex, chunk.cursor, key
        );
        int newTailSize = splitIndex - chunk.startIndex;
        Chunk newTail = new Chunk(chunk.keys, chunk.values, chunk.startIndex, splitIndex, newTailSize, chunk.tailChunk);
        chunk.startIndex = splitIndex;
        chunk.chunkSize = chunk.cursor - chunk.startIndex;
        chunk.tailChunk = newTail;
        return newTail;
    }

    private Chunk findChunkWhereKeyShouldBe(Chunk currentChunk, long key) {
        while (true) {
            if (isFirstElementIsGreaterEqualThanKey(currentChunk, key) && currentChunk.tailChunk != null) {
                currentChunk = currentChunk.tailChunk;
                continue;
            }
            break;
        }
        return currentChunk;
    }

    private boolean isFirstElementIsGreaterEqualThanKey(Chunk chunk, long key) {
        return chunk.cursor != chunk.startIndex
            && chunk.keys[chunk.startIndex] >= key;
    }


    public void clear() {
        activeChunkLock.lock();
        try {
            activeChunk.tailChunk = null;
            activeChunk.startIndex = 0;
            activeChunk.chunkSize = activeChunk.keys.length;
            activeChunk.cursor = 0;
        } finally {
            activeChunkLock.unlock();
        }
    }

    private int findFirstIndexOfGreaterEqualElements(long[] array, int startIndex, int endIndex, long minKey) {
        if (endIndex == startIndex || array[startIndex] >= minKey) {
            return startIndex;
        }
        int searchIndex = binarySearch(array, startIndex, endIndex, minKey);
        int realIndex;
        if (searchIndex < 0) {
            realIndex = -(searchIndex + 1);
        } else {
            realIndex = searchIndex;
        }
        return realIndex;
    }

    private class Chunk {
        private final long[] keys;
        private final long[] values;

        private int chunkSize; // can differ from keys.length after half clear()
        private int startIndex = 0;
        private int cursor = 0;
        private Chunk tailChunk;

        private Chunk(int chunkSize) {
            this.chunkSize = chunkSize;
            this.tailChunk = null;
            this.keys = new long[chunkSize];
            this.values = new long[chunkSize];
        }

        private Chunk(Chunk tailChunk, int size) {
            this.chunkSize = size;
            this.tailChunk = tailChunk;
            this.keys = new long[chunkSize];
            this.values = new long[chunkSize];
        }

        private Chunk(final long[] keys, final long[] values,
                      final int startIndex, final int cursor, final int chunkSize, final Chunk tailChunk) {
            this.keys = keys;
            this.values = values;
            this.startIndex = startIndex;
            this.cursor = cursor;
            this.chunkSize = chunkSize;
            this.tailChunk = tailChunk;
        }

        public void append(long key, long value) {
            keys[cursor] = key;
            values[cursor] = value;
            cursor++;
        }
    }
}

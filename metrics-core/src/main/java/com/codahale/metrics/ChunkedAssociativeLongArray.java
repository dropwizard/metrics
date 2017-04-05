package com.codahale.metrics;

import static java.lang.Math.min;
import static java.util.Arrays.binarySearch;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author bstorozhuk
 */
public class ChunkedAssociativeLongArray {
    private static final long[] EMPTY = new long[0];
    private static final int DEFAULT_CHUNK_SIZE = 100;
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
            boolean isFull = activeChunk.cursor == activeChunk.chunkSize;
            if (isFull) {
                activeChunk = new Chunk(activeChunk);
            }
            activeChunk.append(key, value);
        } finally {
            activeChunkLock.unlock();
        }
    }

    public long[] values(long minKey) {
        Chunk currentChunk;
        int currentChunkCursor;
        activeChunkLock.lock();
        try {
            currentChunk = activeChunk;
            currentChunkCursor = activeChunk.cursor;
        } finally {
            activeChunkLock.unlock();
        }

        // traverse
        if (currentChunkCursor == 0 || currentChunk.keys[currentChunkCursor - 1] < minKey) {
            return EMPTY;
        }

        long[] values;
        int valuesSize = 0;
        Deque<Chunk> chunksDeque = new ArrayDeque<Chunk>();
        while (true) {
            int realIndex = findFirstIndexOfGreaterEqualElements(currentChunk.keys, currentChunkCursor, minKey);
            if (realIndex == 0 && currentChunk.tailChunk != null) {
                // we need to continue
                valuesSize += currentChunkCursor;
                chunksDeque.addLast(currentChunk);

                currentChunk = currentChunk.tailChunk;
                currentChunkCursor = currentChunk.cursor;
            } else {
                // last actual chunk reached
                int actualElementsCount = currentChunkCursor - realIndex;
                valuesSize += actualElementsCount;
                values = new long[valuesSize];

                int valuesIndex = 0;
                int copyStartIndex = realIndex;
                Chunk copySourceChunk = currentChunk;
                while (valuesIndex < valuesSize) {
                    int canBeCopiedFromChunk = copySourceChunk.cursor - copyStartIndex;
                    int leftToCopy = valuesSize - valuesIndex;
                    int length = min(canBeCopiedFromChunk, leftToCopy);
                    if(copyStartIndex + length != 0) {
                        System.arraycopy(copySourceChunk.values, copyStartIndex, values, valuesIndex, length);
                    }
                    valuesIndex += length;
                    copyStartIndex = 0;
                    if (!chunksDeque.isEmpty()) {
                        copySourceChunk = chunksDeque.removeLast();
                    }
                }

                break;
            }
        }

        return values;
    }

    private int findFirstIndexOfGreaterEqualElements(long[] array, int endIndex, long minKey) {
        if (endIndex == 0 || array[0] >= minKey) {
            return 0;
        }
        int searchIndex = binarySearch(array, 0, endIndex, minKey);
        int realIndex;
        if (searchIndex < 0) {
            realIndex = -(searchIndex + 1);
        } else {
            realIndex = searchIndex;
        }
        return realIndex;
    }

    public int size(long minKey) {
        Chunk currentChunk;
        int currentChunkCursor;
        activeChunkLock.lock();
        try {
            currentChunk = activeChunk;
            currentChunkCursor = activeChunk.cursor;
        } finally {
            activeChunkLock.unlock();
        }

        if (currentChunkCursor == 0 || currentChunk.keys[currentChunkCursor - 1] < minKey) {
            return 0;
        }

        // traverse
        int valuesSize = 0;
        while (true) {
            int realIndex = findFirstIndexOfGreaterEqualElements(currentChunk.keys, currentChunkCursor, minKey);
            if (realIndex == 0 && currentChunk.tailChunk != null) {
                // we need to continue
                valuesSize += currentChunkCursor;

                currentChunk = currentChunk.tailChunk;
                currentChunkCursor = currentChunk.cursor;
            } else {
                int actualElementsCount = currentChunkCursor - realIndex;
                valuesSize += actualElementsCount;
                return valuesSize;
            }
        }
    }

    /**
     * Try to trim all beyond specified boundaries.
     * All items that are less then startKey or greater/equals then endKey
     *
     * @param startKey
     * @param endKey
     */
    public void trim(long startKey, long endKey) {
        Chunk currentChunk;
        activeChunkLock.lock();
        try {
            currentChunk = activeChunk;
            while (currentChunk.cursor != 0 && currentChunk.keys[0] >= endKey && currentChunk.tailChunk != null) {
                currentChunk = currentChunk.tailChunk;
            }
            activeChunk = currentChunk;
            int endIndex = findFirstIndexOfGreaterEqualElements(activeChunk.keys, activeChunk.cursor, endKey);
            activeChunk.cursor = endIndex;
        } finally {
            activeChunkLock.unlock();
        }

        while (true) {
            synchronized (currentChunk) {
                if (currentChunk.tailChunk == null) {
                    return;
                }

                currentChunk = currentChunk.tailChunk;
                if (currentChunk.cursor != 0 && currentChunk.keys[0] < startKey) {
                    currentChunk.tailChunk = null;
                }
            }
        }
    }

    /**
     * Clear all in specified boundaries.
     * Remove all items between startKey(inclusive) and endKey(exclusive)
     *
     * @param startKey
     * @param endKey
     */
    public void clear(long startKey, long endKey) {
        activeChunkLock.lock();
        try {
            Chunk headPartLastChunk = activeChunk;
            while (true) {
                int endIndex = findFirstIndexOfGreaterEqualElements(headPartLastChunk.keys, headPartLastChunk.cursor, endKey);
                if (endIndex != 0) {
                    // shift array and make chunk active
                    int resultLength = headPartLastChunk.cursor - endIndex;
                    System.arraycopy(headPartLastChunk.keys, endIndex, headPartLastChunk.keys, 0, resultLength);
                    System.arraycopy(headPartLastChunk.values, endIndex, headPartLastChunk.values, 0, resultLength);
                    headPartLastChunk.cursor = resultLength;
                    break;
                }
                if (headPartLastChunk.tailChunk == null) {
                    // there is nothing to clear
                    return;
                }
                // go to next chunk
                headPartLastChunk = headPartLastChunk.tailChunk;
            }
            // new head found, now we need to concat it with tail
            Chunk tailPartFirstChunk = headPartLastChunk.tailChunk;
            while (true) {
                if (tailPartFirstChunk == null) {
                    return;
                }
                int startIndex = findFirstIndexOfGreaterEqualElements(tailPartFirstChunk.keys, tailPartFirstChunk.cursor, startKey);
                if (startIndex != 0) {
                    //concat it with tail
                    tailPartFirstChunk.cursor = startIndex;
                    tailPartFirstChunk.chunkSize = startIndex;
                    headPartLastChunk.tailChunk = tailPartFirstChunk;
                }
                tailPartFirstChunk = tailPartFirstChunk.tailChunk;
            }
        } finally {
            activeChunkLock.unlock();
        }
    }


    String out(long minKey) {
        Chunk currentChunk;
        int currentChunkCursor;
        activeChunkLock.lock();
        try {
            currentChunk = activeChunk;
            currentChunkCursor = activeChunk.cursor;
        } finally {
            activeChunkLock.unlock();
        }

        // traverse
        if (currentChunkCursor == 0 || currentChunk.keys[currentChunkCursor - 1] < minKey) {
            return "[]";
        }

        StringBuilder values = new StringBuilder();
        int valuesSize = 0;
        Deque<Chunk> chunksDeque = new ArrayDeque<Chunk>();
        while (true) {
            int realIndex = findFirstIndexOfGreaterEqualElements(currentChunk.keys, currentChunkCursor, minKey);
            if (realIndex == 0 && currentChunk.tailChunk != null) {
                // we need to continue
                valuesSize += currentChunkCursor;
                chunksDeque.addLast(currentChunk);

                currentChunk = currentChunk.tailChunk;
                currentChunkCursor = currentChunk.cursor;
            } else {
                // last actual chunk reached
                int actualElementsCount = currentChunkCursor - realIndex;
                valuesSize += actualElementsCount;

                int valuesIndex = 0;
                int copyStartIndex = realIndex;
                Chunk copySourceChunk = currentChunk;
                while(valuesIndex < valuesSize) {
                    int canBeCopiedFromChunk = copySourceChunk.cursor - copyStartIndex;
                    int leftToCopy = valuesSize - valuesIndex;
                    int length = min(canBeCopiedFromChunk, leftToCopy);
                    if(copyStartIndex + length != 0) {
                        values.append('[');
                        for (int i = copyStartIndex; i < copyStartIndex + length; i++) {
                            long key = copySourceChunk.keys[i];
                            long value = copySourceChunk.values[i];
                            values.append('(').append(key).append(": ").append(value).append(')').append(' ');
                        }
                        values.append(']');
                    }
                    valuesIndex += length;
                    copyStartIndex = 0;
                    if (!chunksDeque.isEmpty()) {
                        copySourceChunk = chunksDeque.removeLast();
                        values.append("->");
                    }
                }

                break;
            }
        }

        return values.toString();
    }

    public void clear() {
        activeChunkLock.lock();
        try {
            activeChunk.tailChunk = null;
            activeChunk.cursor = 0;
        } finally {
            activeChunkLock.unlock();
        }
    }

    private class Chunk {
        private final long[] keys;
        private final long[] values;

        private int chunkSize; // can differ from keys.length after half clear()
        private int cursor = 0;
        private Chunk tailChunk;

        private Chunk(int chunkSize) {
            this.chunkSize = chunkSize;
            this.tailChunk = null;
            this.keys = new long[chunkSize];
            this.values = new long[chunkSize];
        }

        private Chunk(Chunk tailChunk) {
            this.chunkSize = tailChunk.chunkSize;
            this.tailChunk = tailChunk;
            this.keys = new long[chunkSize];
            this.values = new long[chunkSize];
        }

        public void append(long key, long value) {
            keys[cursor] = key;
            values[cursor] = value;
            cursor++;
        }
    }
}

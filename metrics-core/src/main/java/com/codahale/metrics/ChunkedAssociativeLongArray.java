package com.codahale.metrics;

import static java.lang.System.arraycopy;
import static java.util.Arrays.binarySearch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

class ChunkedAssociativeLongArray {
    private static final long[] EMPTY = new long[0];
    private static final int DEFAULT_CHUNK_SIZE = 512;

    private final int defaultChunkSize;
    private final LinkedList<Chunk> chunks = new LinkedList<Chunk>();

    ChunkedAssociativeLongArray() {
        this(DEFAULT_CHUNK_SIZE);
    }

    ChunkedAssociativeLongArray(int chunkSize) {
        this.defaultChunkSize = chunkSize;
    }

    synchronized boolean put(long key, long value) {
        Chunk activeChunk = chunks.peekLast();

        if (activeChunk == null) { // lazy chunk creation
            activeChunk = new Chunk(this.defaultChunkSize);
            chunks.add(activeChunk);

        } else {
            if (activeChunk.cursor != 0 && activeChunk.keys[activeChunk.cursor - 1] > key) {
                return false; // key should be the same as last inserted or bigger
            }
            boolean isFull = activeChunk.cursor - activeChunk.startIndex == activeChunk.chunkSize;
            if (isFull) {
                activeChunk = new Chunk(this.defaultChunkSize);
                chunks.add(activeChunk);
            }
        }

        activeChunk.append(key, value);
        return true;
    }

    synchronized long[] values() {
        int valuesSize = size();
        if (valuesSize == 0) {
            return EMPTY;
        }

        long[] values = new long[valuesSize];
        int valuesIndex = 0;
        for (Chunk copySourceChunk : chunks) {
            int length = copySourceChunk.cursor - copySourceChunk.startIndex;
            int itemsToCopy = Math.min(valuesSize - valuesIndex, length);
            arraycopy(copySourceChunk.values, copySourceChunk.startIndex, values, valuesIndex, itemsToCopy);
            valuesIndex += length;
        }
        return values;
    }

    synchronized int size() {
        int result = 0;
        for (Chunk chunk : chunks) {
            result += chunk.cursor - chunk.startIndex;
        }
        return result;
    }

    synchronized String out() {
        Iterator<Chunk> fromTailIterator = chunks.iterator();
        StringBuilder builder = new StringBuilder();
        while (fromTailIterator.hasNext()) {
            Chunk copySourceChunk = fromTailIterator.next();
            builder.append('[');
            for (int i = copySourceChunk.startIndex; i < copySourceChunk.cursor; i++) {
                long key = copySourceChunk.keys[i];
                long value = copySourceChunk.values[i];
                builder.append('(').append(key).append(": ").append(value).append(')').append(' ');
            }
            builder.append(']');
            if (fromTailIterator.hasNext()) {
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
    synchronized void trim(long startKey, long endKey) {
        /*
         * [3, 4, 5, 9] -> [10, 13, 14, 15] -> [21, 24, 29, 30] -> [31] :: start layout
         *       |5______________________________23|                    :: trim(5, 23)
         *       [5, 9] -> [10, 13, 14, 15] -> [21]                     :: result layout
         */
        ListIterator<Chunk> fromHeadIterator = chunks.listIterator(chunks.size());
        while (fromHeadIterator.hasPrevious()) {
            Chunk currentHead = fromHeadIterator.previous();
            if (isFirstElementIsEmptyOrGreaterEqualThanKey(currentHead, endKey)) {
                fromHeadIterator.remove();
            } else {
                int newEndIndex = findFirstIndexOfGreaterEqualElements(
                    currentHead.keys, currentHead.startIndex, currentHead.cursor, endKey
                );
                currentHead.cursor = newEndIndex;
                break;
            }
        }

        ListIterator<Chunk> fromTailIterator = chunks.listIterator();
        while (fromTailIterator.hasNext()) {
            Chunk currentTail = fromTailIterator.next();
            if (isLastElementIsLessThanKey(currentTail, startKey)) {
                fromTailIterator.remove();
            } else {
                int newStartIndex = findFirstIndexOfGreaterEqualElements(
                    currentTail.keys, currentTail.startIndex, currentTail.cursor, startKey
                );
                if (currentTail.startIndex != newStartIndex) {
                    currentTail.startIndex = newStartIndex;
                    currentTail.chunkSize = currentTail.cursor - currentTail.startIndex;
                }
                break;
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
    synchronized void clear(long startKey, long endKey) {
        /*
         * [3, 4, 5, 9] -> [10, 13, 14, 15] -> [21, 24, 29, 30] -> [31] :: start layout
         *       |5______________________________23|                    :: clear(5, 23)
         * [3, 4]               ->                 [24, 29, 30] -> [31] :: result layout
         */
        ListIterator<Chunk> fromHeadIterator = chunks.listIterator(chunks.size());
        while (fromHeadIterator.hasPrevious()) {
            Chunk currentTail = fromHeadIterator.previous();
            if (!isFirstElementIsEmptyOrGreaterEqualThanKey(currentTail, endKey)) {
                Chunk afterTailChunk = splitChunkOnTwoSeparateChunks(currentTail, endKey);
                if (afterTailChunk != null) {
                    fromHeadIterator.add(afterTailChunk);
                    break;
                }
            }
        }

        // now we should remove specified gap [startKey, endKey]
        while (fromHeadIterator.hasPrevious()) {
            Chunk afterGapHead = fromHeadIterator.previous();
            if (isFirstElementIsEmptyOrGreaterEqualThanKey(afterGapHead, startKey)) {
                fromHeadIterator.remove();
            } else {
                int newEndIndex = findFirstIndexOfGreaterEqualElements(
                    afterGapHead.keys, afterGapHead.startIndex, afterGapHead.cursor, startKey
                );
                if (newEndIndex == afterGapHead.startIndex) {
                    break;
                }
                if (afterGapHead.cursor != newEndIndex) {
                    afterGapHead.cursor = newEndIndex;
                    afterGapHead.chunkSize = afterGapHead.cursor - afterGapHead.startIndex;
                    break;
                }
            }
        }
    }

    synchronized void clear() {
        chunks.clear();
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
        if (splitIndex == chunk.startIndex || splitIndex == chunk.cursor) {
            return null;
        }
        int newTailSize = splitIndex - chunk.startIndex;
        Chunk newTail = new Chunk(chunk.keys, chunk.values, chunk.startIndex, splitIndex, newTailSize);
        chunk.startIndex = splitIndex;
        chunk.chunkSize = chunk.chunkSize - newTailSize;
        return newTail;
    }

    private boolean isFirstElementIsEmptyOrGreaterEqualThanKey(Chunk chunk, long key) {
        return chunk.cursor == chunk.startIndex
            || chunk.keys[chunk.startIndex] >= key;
    }

    private boolean isLastElementIsLessThanKey(Chunk chunk, long key) {
        return chunk.cursor == chunk.startIndex
            || chunk.keys[chunk.cursor - 1] < key;
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

    private static class Chunk {

        private final long[] keys;
        private final long[] values;

        private int chunkSize; // can differ from keys.length after half clear()
        private int startIndex = 0;
        private int cursor = 0;

        private Chunk(int chunkSize) {
            this.chunkSize = chunkSize;
            this.keys = new long[chunkSize];
            this.values = new long[chunkSize];
        }

        private Chunk(final long[] keys, final long[] values,
                      final int startIndex, final int cursor, final int chunkSize) {
            this.keys = keys;
            this.values = values;
            this.startIndex = startIndex;
            this.cursor = cursor;
            this.chunkSize = chunkSize;
        }

        private void append(long key, long value) {
            keys[cursor] = key;
            values[cursor] = value;
            cursor++;
        }
    }
}

package io.dropwizard.metrics5;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import static java.lang.System.arraycopy;
import static java.util.Arrays.binarySearch;

class ChunkedAssociativeLongArray {
    private static final long[] EMPTY = new long[0];
    private static final int DEFAULT_CHUNK_SIZE = 512;
    private static final int MAX_CACHE_SIZE = 128;

    private final int defaultChunkSize;

    /*
     * We use this ArrayDeque as cache to store chunks that are expired and removed from main data structure.
     * Then instead of allocating new Chunk immediately we are trying to poll one from this deque.
     * So if you have constant or slowly changing load ChunkedAssociativeLongArray will never
     * throw away old chunks or allocate new ones which makes this data structure almost garbage free.
     */
    private final ArrayDeque<SoftReference<Chunk>> chunksCache = new ArrayDeque<>();

    private final Deque<Chunk> chunks = new ArrayDeque<>();

    ChunkedAssociativeLongArray() {
        this(DEFAULT_CHUNK_SIZE);
    }

    ChunkedAssociativeLongArray(int chunkSize) {
        this.defaultChunkSize = chunkSize;
    }

    private Chunk allocateChunk() {
        while (true) {
            final SoftReference<Chunk> chunkRef = chunksCache.pollLast();
            if (chunkRef == null) {
                return new Chunk(defaultChunkSize);
            }
            final Chunk chunk = chunkRef.get();
            if (chunk != null) {
                chunk.cursor = 0;
                chunk.startIndex = 0;
                chunk.chunkSize = chunk.keys.length;
                return chunk;
            }
        }
    }

    private void freeChunk(Chunk chunk) {
        if (chunksCache.size() < MAX_CACHE_SIZE) {
            chunksCache.add(new SoftReference<>(chunk));
        }
    }

    synchronized boolean put(long key, long value) {
        Chunk activeChunk = chunks.peekLast();
        if (activeChunk != null && activeChunk.cursor != 0 && activeChunk.keys[activeChunk.cursor - 1] > key) {
            // key should be the same as last inserted or bigger
            return false;
        }
        if (activeChunk == null || activeChunk.cursor - activeChunk.startIndex == activeChunk.chunkSize) {
            // The last chunk doesn't exist or full
            activeChunk = allocateChunk();
            chunks.add(activeChunk);
        }
        activeChunk.append(key, value);
        return true;
    }

    synchronized long[] values() {
        final int valuesSize = size();
        if (valuesSize == 0) {
            return EMPTY;
        }

        final long[] values = new long[valuesSize];
        int valuesIndex = 0;
        for (Chunk chunk : chunks) {
            int length = chunk.cursor - chunk.startIndex;
            int itemsToCopy = Math.min(valuesSize - valuesIndex, length);
            arraycopy(chunk.values, chunk.startIndex, values, valuesIndex, itemsToCopy);
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
        final StringBuilder builder = new StringBuilder();
        final Iterator<Chunk> iterator = chunks.iterator();
        while (iterator.hasNext()) {
            final Chunk chunk = iterator.next();
            builder.append('[');
            for (int i = chunk.startIndex; i < chunk.cursor; i++) {
                builder.append('(').append(chunk.keys[i]).append(": ")
                        .append(chunk.values[i]).append(')').append(' ');
            }
            builder.append(']');
            if (iterator.hasNext()) {
                builder.append("->");
            }
        }
        return builder.toString();
    }

    /**
     * Try to trim all beyond specified boundaries.
     *
     * @param startKey the start value for which all elements less than it should be removed.
     * @param endKey   the end value for which all elements greater/equals than it should be removed.
     */
    synchronized void trim(long startKey, long endKey) {
        /*
         * [3, 4, 5, 9] -> [10, 13, 14, 15] -> [21, 24, 29, 30] -> [31] :: start layout
         *       |5______________________________23|                    :: trim(5, 23)
         *       [5, 9] -> [10, 13, 14, 15] -> [21]                     :: result layout
         */
        final Iterator<Chunk> descendingIterator = chunks.descendingIterator();
        while (descendingIterator.hasNext()) {
            final Chunk currentTail = descendingIterator.next();
            if (isFirstElementIsEmptyOrGreaterEqualThanKey(currentTail, endKey)) {
                freeChunk(currentTail);
                descendingIterator.remove();
            } else {
                currentTail.cursor = findFirstIndexOfGreaterEqualElements(currentTail.keys, currentTail.startIndex,
                        currentTail.cursor, endKey);
                break;
            }
        }

        final Iterator<Chunk> iterator = chunks.iterator();
        while (iterator.hasNext()) {
            final Chunk currentHead = iterator.next();
            if (isLastElementIsLessThanKey(currentHead, startKey)) {
                freeChunk(currentHead);
                iterator.remove();
            } else {
                final int newStartIndex = findFirstIndexOfGreaterEqualElements(currentHead.keys, currentHead.startIndex,
                        currentHead.cursor, startKey);
                if (currentHead.startIndex != newStartIndex) {
                    currentHead.startIndex = newStartIndex;
                    currentHead.chunkSize = currentHead.cursor - currentHead.startIndex;
                }
                break;
            }
        }
    }

    synchronized void clear() {
        chunks.clear();
    }

    private boolean isFirstElementIsEmptyOrGreaterEqualThanKey(Chunk chunk, long key) {
        return chunk.cursor == chunk.startIndex || chunk.keys[chunk.startIndex] >= key;
    }

    private boolean isLastElementIsLessThanKey(Chunk chunk, long key) {
        return chunk.cursor == chunk.startIndex || chunk.keys[chunk.cursor - 1] < key;
    }

    private int findFirstIndexOfGreaterEqualElements(long[] array, int startIndex, int endIndex, long minKey) {
        if (endIndex == startIndex || array[startIndex] >= minKey) {
            return startIndex;
        }
        final int keyIndex = binarySearch(array, startIndex, endIndex, minKey);
        return keyIndex < 0 ? -(keyIndex + 1) : keyIndex;
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

        private void append(long key, long value) {
            keys[cursor] = key;
            values[cursor] = value;
            cursor++;
        }
    }
}

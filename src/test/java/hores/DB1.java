package hores;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hores.DB1.ChunkManager.RangedChunk;

public class DB1 {

    @Test
    public void testName() throws Exception {

        // serialize(str).length = 7 + str.length()
        System.out.println(serialize("").length);
        System.out.println(serialize("a").length);
        System.out.println(serialize("aaa").length);
        System.out.println(serialize("aaaaaa").length);

        {
            RandomAccessFile rafIndex = new RandomAccessFile(new File("index"), "rw");
            ChunkRanges<Long> rs = new ChunkRanges<>();

            ChunkRange<Long> r1 = new ChunkRange<Long>();
            r1.min = null;
            r1.max = 5L;
            r1.numChunk = 8;
            rs.ranges.add(r1);

            System.out.println(rs.ranges);

            rs.store(rafIndex);
            r1.min = 1L;
            r1.max = 2L;
            r1.numChunk = 3;
            rs.load(rafIndex);

            System.out.println(rs.ranges);
        }

        {
            RandomAccessFile rafChunk = new RandomAccessFile(new File("data"), "rw");
            Chunk<Long, String> c = new Chunk<>();

            c.numChunk = 10L;
            c.props.put(1L, "one");
            c.props.put(2L, "two");
            c.props.put(3L, "three");
            System.out.println(c.props);

            c.store(rafChunk, 10 * 1024);
            c.props.remove(2L);
            c.load(rafChunk, 10 * 1024);
            System.out.println(c.props);
        }

        {
            ChunkManager<Long, String> cm = new ChunkManager<>("test");
            cm.recreate(256);

            Random rnd = new Random(0L);

            cm.open();
            for (int i = 0; i < 100; i++) {

                long k = Math.abs(rnd.nextLong() % 100);

                {
                    ChunkRanges<Long> crs = cm.loadChunkRanges();
                    RangedChunk<Long, String> rangedChunk = cm.loadRangedChunkFor(crs, k);

                    rangedChunk.chunk.props.put(k, "joujuasjaja" + k);

                    cm.storeRangedChunk(crs, rangedChunk);
                    cm.storeChunkRanges(crs);
                }

                {
                    ChunkRanges<Long> crs = cm.loadChunkRanges();
                    RangedChunk<Long, String> rangedChunk = cm.loadRangedChunkFor(crs, k);

                    assertEquals("joujuasjaja" + k, rangedChunk.chunk.props.get(k));

                }

            }
            ChunkRanges<Long> crs = cm.loadChunkRanges();
            // System.out.println("=> " + crs);

            List<RangedChunk<Long, String>> results = cm.loadRangedChunksFor(crs, 5L, 10L);
            System.out.println(results);

            cm.info();
            cm.close();

        }

//        {
//            ChunkManager<Long, String> cm = new ChunkManager<>("test");
//            cm.recreate(256);
//
//            cm.open();
//
//            {
//                ChunkRanges<Long> crs = cm.loadChunkRanges();
//                RangedChunk<Long, String> rangedChunk = cm.loadRangedChunkFor(crs, k);
//
//                rangedChunk.chunk.props.put(2L, "2");
//                rangedChunk.chunk.props.put(4L, "4");
//                rangedChunk.chunk.props.put(8L, "8");
//                rangedChunk.chunk.props.put(16L, "16");
//
//                cm.storeRangedChunk(crs, rangedChunk);
//                cm.storeChunkRanges(crs);
//            }
//
//            cm.info();
//            cm.close();
//
//        }
    }

    // static class DB2<K extends Comparable<K> & Serializable, V extends
    // Serializable> {
    //
    // final ChunkManager<K, V> cman;
    // final List<RangedChunk<K, V>> cache = new ArrayList<>();
    //
    // public DB2(String fileName) throws IOException {
    // super();
    // this.cman = new ChunkManager<>(fileName);
    // }
    //
    // public void beginTransaction() {
    //
    // }
    //
    // }

    static class ChunkManager<K extends Comparable<K> & Serializable, V extends Serializable> {

        static final Logger LOG = LoggerFactory.getLogger(ChunkManager.class);

        static final int DEFAULT_chunkMaxSizeInBytes = 1024;

        final String fileName;
        // final File lockFile;
        final File indexFile;
        final File chunkFile;

        RandomAccessFile indexRaf;
        RandomAccessFile chunkRaf;

        public ChunkManager(String fileName) throws IOException {
            super();
            this.fileName = fileName;

            // this.lockFile = new File(fileName + ".lock");
            this.indexFile = new File(fileName + ".index");
            this.chunkFile = new File(fileName + ".chunks");
            if (!indexFile.exists() || !chunkFile.exists()) {
                recreate();
            }
        }

        public void open() throws FileNotFoundException {
            this.indexRaf = new RandomAccessFile(indexFile, "rw");
            this.chunkRaf = new RandomAccessFile(chunkFile, "rw");
        }

        public void close() throws IOException {
            this.indexRaf.close();
            this.chunkRaf.close();
            this.indexRaf = null;
            this.chunkRaf = null;
        }

        public void recreate(int chunkMaxSizeInBytes) throws IOException {
            // if (lockFile.exists()) {
            // lockFile.delete();
            // }
            if (indexFile.exists()) {
                indexFile.delete();
            }
            if (chunkFile.exists()) {
                chunkFile.delete();
            }
            indexFile.createNewFile();
            chunkFile.createNewFile();
            open();
            initStore(chunkMaxSizeInBytes);
            close();
            LOG.info("recreated DB: " + fileName);
        }

        public void recreate() throws IOException {
            recreate(DEFAULT_chunkMaxSizeInBytes);
        }

        protected void initStore(int chunkMaxSizeInBytes) throws IOException {

            ChunkRanges<K> crs = new ChunkRanges<>();
            crs.chunkMaxSizeInBytes = chunkMaxSizeInBytes;
            crs.ranges = new TreeSet<>();
            ChunkRange<K> cr = new ChunkRange<K>();
            cr.min = null;
            cr.max = null;
            cr.numChunk = 0L;
            crs.ranges.add(cr);

            Chunk<K, V> c = new Chunk<>();
            c.numChunk = 0L;
            c.props = new TreeMap<K, V>();

            try {
                c.store(chunkRaf, chunkMaxSizeInBytes);
            } catch (ChunkSizeException e) {
                throw new RuntimeException(e);
            }
            crs.store(indexRaf);
        }

        public void info() throws IOException {

            ChunkRanges<K> crs = loadChunkRanges();

            int numChunks = crs.ranges.size();
            long indexBytes = indexFile.length();
            long dataBytes = chunkFile.length();

            LOG.info("*** " + numChunks + " chunks, index=" + indexBytes + "b, data=" + dataBytes + "b)");
        }

        public ChunkRanges<K> loadChunkRanges() throws IOException {
            ChunkRanges<K> r = new ChunkRanges<>();
            r.load(indexRaf);
            // LOG.info("loaded ranges: " + r);
            return r;
        }

        public void storeChunkRanges(ChunkRanges<K> ranges) throws IOException {
            ranges.store(indexRaf);
            // LOG.info("stored ranges: " + ranges);
        }

        public static class RangedChunk<K extends Comparable<K> & Serializable, V extends Serializable> {

            public final ChunkRange<K> range;
            public final Chunk<K, V> chunk;

            public RangedChunk(ChunkRange<K> range, Chunk<K, V> chunk) {
                super();
                this.range = range;
                this.chunk = chunk;
            }

            @Override
            public String toString() {
                return range + " => " + chunk;
            }
        }

        public RangedChunk<K, V> loadRangedChunkFor(ChunkRanges<K> crs, K key) throws IOException {
            ChunkRange<K> findFor = new ChunkRange<>();
            findFor.min = key;
            ChunkRange<K> range = crs.ranges.floor(findFor);

            Chunk<K, V> chunk = new Chunk<>();
            chunk.numChunk = range.numChunk;
            chunk.load(chunkRaf, crs.chunkMaxSizeInBytes);

            RangedChunk<K, V> r = new RangedChunk<>(range, chunk);
            // LOG.info("loaded: " + r);
            return r;
        }

        public List<RangedChunk<K, V>> loadRangedChunksFor(ChunkRanges<K> crs, K start, K end) throws IOException {
            ChunkRange<K> min = new ChunkRange<>();
            min.min = start;
            ChunkRange<K> minCr = crs.ranges.floor(min);

            ChunkRange<K> max = new ChunkRange<>();
            max.min = end;
            ChunkRange<K> maxCr = crs.ranges.floor(max);

            SortedSet<ChunkRange<K>> ranges = crs.ranges.subSet(minCr, true, maxCr, true);

            List<RangedChunk<K, V>> r = new ArrayList<>();
            for (ChunkRange<K> range : ranges) {
                Chunk<K, V> chunk = new Chunk<>();
                chunk.numChunk = range.numChunk;
                chunk.load(chunkRaf, crs.chunkMaxSizeInBytes);
                r.add(new RangedChunk<>(range, chunk));
            }
            return r;
        }

        public void storeRangedChunk(ChunkRanges<K> crs, RangedChunk<K, V> rc) throws IOException {

            try {
                rc.chunk.store(chunkRaf, crs.chunkMaxSizeInBytes);
                // LOG.info("stored: " + rc);
            } catch (ChunkSizeException e) {
                // LOG.info("splitting: " + rc);
                RangedChunk<K, V> newChunk = splitChunk(crs, rc.range, rc.chunk);
                RangedChunk<K, V> oldChunk = new RangedChunk<>(rc.range, rc.chunk);
                // LOG.info("splitting: " + oldChunk + " || " + newChunk);

                storeRangedChunk(crs, oldChunk);
                storeRangedChunk(crs, newChunk);
            }
        }

        protected RangedChunk<K, V> splitChunk(ChunkRanges<K> crs, ChunkRange<K> chunkRange, Chunk<K, V> chunk) {
            Chunk<K, V> chunk1 = new Chunk<>();
            Chunk<K, V> chunk2 = new Chunk<>();
            ChunkRange<K> chunkRange1 = chunkRange;
            ChunkRange<K> chunkRange2 = new ChunkRange<>();

            chunk1.numChunk = chunkRange1.numChunk = chunkRange.numChunk;
            chunk2.numChunk = chunkRange2.numChunk = crs.ranges.size();

            chunkRange1.min = chunkRange.min;
            chunkRange2.max = chunkRange.max;

            boolean toFirst = true;
            int c = 0;
            for (K k : chunk.props.keySet()) {
                if (c == chunk.props.size() / 2) {
                    toFirst = false;
                    chunkRange1.max = k;
                    chunkRange2.min = k;
                }
                if (toFirst) {
                    chunk1.props.put(k, chunk.props.get(k));
                } else {
                    chunk2.props.put(k, chunk.props.get(k));
                }
                c++;
            }

            crs.ranges.add(chunkRange2);

            chunk.props = chunk1.props;
            chunk.numChunk = chunk1.numChunk;

            return new RangedChunk<K, V>(chunkRange2, chunk2);
        }
    }

    static class ChunkRanges<K extends Comparable<K> & Serializable> {

        int chunkMaxSizeInBytes;
        TreeSet<ChunkRange<K>> ranges = new TreeSet<>();

        @SuppressWarnings("unchecked")
        public void load(RandomAccessFile raf) throws IOException {
            raf.seek(0L);
            this.chunkMaxSizeInBytes = raf.readInt();
            int len = raf.readInt();
            byte[] bs = new byte[len];
            raf.read(bs);
            this.ranges = (TreeSet<ChunkRange<K>>) deserialize(bs);
        }

        public void store(RandomAccessFile raf) throws IOException {
            raf.seek(0L);
            raf.writeInt(chunkMaxSizeInBytes);
            byte[] bs = serialize(ranges);
            raf.writeInt(bs.length);
            raf.write(bs);
        }

        @Override
        public String toString() {
            return "ChunkRanges(num=" + ranges.size() + ") [chunkMaxSizeInBytes=" + chunkMaxSizeInBytes + ", ranges="
                    + ranges + "]";
        }

    }

    static class ChunkRange<K extends Comparable<K> & Serializable> implements Comparable<ChunkRange<K>>, Serializable {

        private static final long serialVersionUID = -1342483814662970232L;

        K min;
        K max;
        long numChunk;

        @Override
        public int compareTo(ChunkRange<K> o) {
            if (this.min == null) {
                return -1;
            }
            if (o.min == null) {
                return 1;
            }
            return min.compareTo(o.min);
        }

        @Override
        public String toString() {
            return "[" + min + ".." + max + ": " + numChunk + "]";
        }

    }

    static class Chunk<K extends Comparable<K> & Serializable, V extends Serializable> {

        Long numChunk;
        TreeMap<K, V> props = new TreeMap<>();

        @SuppressWarnings("unchecked")
        public void load(RandomAccessFile raf, int chunkSize) throws IOException {
            raf.seek(this.numChunk * chunkSize);
            byte[] bs = new byte[chunkSize];
            raf.read(bs);
            this.props = (TreeMap<K, V>) deserialize(bs);
        }

        public void store(RandomAccessFile raf, long chunkSize) throws ChunkSizeException, IOException {
            byte[] bs = serialize(props);
            if (bs.length > chunkSize) {
                throw new ChunkSizeException();
            }
            raf.seek(this.numChunk * chunkSize);
            raf.write(bs);
        }

        @Override
        public String toString() {
            return "(" + numChunk + ":" + props.size() + ") " + props;
        }

    }

    public static class ChunkSizeException extends Exception {

        private static final long serialVersionUID = 7670311782431069829L;
    }

    /**
     * serialitza un bean
     *
     * @param o el bean a serialitzar
     * @return el bean serialitzat
     */
    public static byte[] serialize(final Serializable o) {
        try {
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            final ObjectOutputStream os = new ObjectOutputStream(bs);
            os.writeObject(o);
            os.close();
            final byte[] bytes = bs.toByteArray();
            bs.close();
            return bytes;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * deserialitza un bean
     *
     * @param byteArray el bean serialitzat
     * @return el bean deserialitzat
     */
    public static Serializable deserialize(final byte[] byteArray) {

        try {
            final ByteArrayInputStream bs = new ByteArrayInputStream(byteArray);
            final ObjectInputStream is = new ObjectInputStream(bs);
            final Serializable o = (Serializable) is.readObject();
            is.close();
            bs.close();
            return o;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}

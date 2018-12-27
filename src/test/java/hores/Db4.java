package hores;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hores.DB3.RangesManager.SegmentExceedsSizeException;

/**
 * <pre>
 *
 *  propietats transient
 *  un do{}while
 *
 * </pre>
 */
public class Db4 {

    @Test
    public void testName() throws Exception {
        M<Long, String> map = new M<>("testo", 5);
        map.create(256);

        map.load();
        map.put(1L, "one");
        map.put(2L, "two");
        map.put(3L, "three");
        map.put(4L, "four");
        map.put(5L, "five");
        assertEquals("one", map.get(1L));
        assertEquals("five", map.get(5L));
        map.store();

        map.load();
        assertEquals("one", map.get(1L));
        assertEquals("five", map.get(5L));

        assertEquals("{1=one, 2=two, 3=three, 4=four, 5=five}", map.get(0L, 6L).toString());
        assertEquals("{1=one, 2=two, 3=three, 4=four, 5=five}", map.get(1L, 5L).toString());
        assertEquals("{2=two, 3=three, 4=four}", map.get(2L, 4L).toString());
        assertEquals("{3=three}", map.get(3L, 3L).toString());

        assertEquals("{1=one, 2=two, 3=three}", map.get(null, 3L).toString());
        assertEquals("{3=three, 4=four, 5=five}", map.get(3L, null).toString());
        assertEquals("{1=one, 2=two, 3=three, 4=four, 5=five}", map.get(null, null).toString());
        map.store();

        map.load();
        map.remove(3L);
        assertEquals("{1=one, 2=two, 4=four, 5=five}", map.get(null, null).toString());
        map.store();

        map.load();
        assertEquals("{1=one, 2=two, 4=four, 5=five}", map.get(null, null).toString());
        map.store();

    }

    @Test
    public void testName2() throws Exception {
        M<Long, String> map = new M<>("testo", 1);
        map.create(256);

        map.load();
        for (long i = 0L; i < 500L; i++) {
            map.put(i, "jou" + i);
        }
        map.store();

        map.load();
        assertEquals("jou0", map.get(0L));
        assertEquals("jou100", map.get(100L));
        assertEquals("jou499", map.get(499L));
        map.info();
        map.store();

    }

    @Test
    public void testThreads() throws Exception {

        M<String, String> map = new M<>("testo", 32);
        map.create(256);

        threadableSimpleTest(map);

        // for (int i = 0; i < 50; i++) {
        // new Thread(() -> threadableSimpleTest(map)).start();
        // }

    }

    private void threadableSimpleTest(M<String, String> map) {

        long prefix = Thread.currentThread().getId();

        try {

            map.load();
            for (long i = 0L; i < 501L; i++) {
                map.put(String.valueOf(prefix) + "_" + i, String.valueOf(prefix) + "_" + i);
            }
            map.store();

            map.load();
            for (long i = 0L; i < 501L; i++) {
//                map.put(String.valueOf(prefix) + "_" + i, String.valueOf(prefix) + "_" + i);
                assertEquals(String.valueOf(prefix) + "_" + i, map.get(String.valueOf(prefix) + "_" + i));
            }
            map.store();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Segment<K extends Comparable<K> & Serializable, V extends Serializable> {

        TreeMap<K, V> props;

        transient boolean changed;
        transient int hits;

        @Override
        public String toString() {
            return props + " changed=" + changed + ", hits=" + hits;
        }

    }

    public static class M<K extends Comparable<K> & Serializable, V extends Serializable> {

        static final Logger LOG = LoggerFactory.getLogger(M.class);

        static final int DEFAULT_chunkMaxSizeInBytes = 1024;

        int segmentSize = -1;
        TreeMap<Range<K, V>, Segment<K, V>> segments;

        final String fileName;
        final int MAX_CACHE_SIZE;

        // final File lockFile;
        final File rangesFile;
        final File segmentsFile;

        RandomAccessFile rangesRaf;
        RandomAccessFile segmentsRaf;

        // final ReadWriteLock lock = new ReentrantReadWriteLock();// TODO

        public M(String fileName, int MAX_CACHE_SIZE) throws IOException {
            super();
            this.fileName = fileName;
            this.MAX_CACHE_SIZE = MAX_CACHE_SIZE;

            // this.lockFile = new File(fileName + ".lock");
            this.rangesFile = new File(fileName + ".index");
            this.segmentsFile = new File(fileName + ".chunks");
            // if (!rangesFile.exists() || !segmentsFile.exists()) {
            //// recreateFiles(); // TODO ?
            // }
        }

        public void info() {
            LOG.info("*** [" + fileName + "]: " + segments.size() + " segments (" + segments.size() * segmentSize / 1024
                    + " Kb)");
        }

        public void create(int chunkMaxSizeInBytes) throws IOException {
            recreateFiles();
            init(chunkMaxSizeInBytes);
            store();
        }

        protected void recreateFiles() throws IOException {
            // if (lockFile.exists()) {
            // lockFile.delete();
            // }
            if (rangesFile.exists()) {
                rangesFile.delete();
            }
            if (segmentsFile.exists()) {
                segmentsFile.delete();
            }
            rangesFile.createNewFile();
            segmentsFile.createNewFile();

            LOG.info("recreated DB: " + fileName);
        }

        protected void init(int chunkMaxSizeInBytes) throws IOException {

            // TODO comprovar que no estigui ja open()
            this.rangesRaf = new RandomAccessFile(rangesFile, "rw");
            this.segmentsRaf = new RandomAccessFile(segmentsFile, "rw");

            this.segmentSize = chunkMaxSizeInBytes;
            this.segments = new TreeMap<>();

            Range<K, V> initialRange = new Range<>();
            initialRange.min = null;
            initialRange.max = null;
            initialRange.numSegment = 0;

            Segment<K, V> initialSegment = new Segment<>();
            initialSegment.props = new TreeMap<>();
            initialSegment.changed = true;
            initialSegment.hits = 0;

            this.segments.put(initialRange, initialSegment);

            // store();
        }

        @SuppressWarnings("unchecked")
        public void load() throws IOException {

            // TODO comprovar que no estigui ja open()
            this.rangesRaf = new RandomAccessFile(rangesFile, "rw");
            this.segmentsRaf = new RandomAccessFile(segmentsFile, "rw");

            rangesRaf.seek(0L);
            this.segmentSize = rangesRaf.readInt();
            int len = rangesRaf.readInt();
            byte[] bs = new byte[len];
            rangesRaf.read(bs);
            ArrayList<Range<K, V>> ranges = (ArrayList<Range<K, V>>) DB1.deserialize(bs);
            this.segments = new TreeMap<>();
            for (Range<K, V> range : ranges) {
                this.segments.put(range, null);
            }

        }

        public void store() throws IOException {

            Entry<Range<K, V>, Segment<K, V>> candidate;
            do {
                candidate = null;
                for (Entry<Range<K, V>, Segment<K, V>> entry : segments.entrySet()) {
                    Segment<K, V> segment = entry.getValue();
                    if (segment != null && segment.changed) {
                        candidate = entry;
                        break;
                    }
                }
                if (candidate != null) {
                    storeSegment(segmentsRaf, candidate.getKey(), candidate.getValue());
                }
            } while (candidate != null);

            // guarda ranges
            rangesRaf.seek(0L);
            rangesRaf.writeInt(segmentSize);
            ArrayList<Range<K, V>> ranges = new ArrayList<>(segments.keySet());
            byte[] bs = DB1.serialize(ranges);
            rangesRaf.writeInt(bs.length);
            rangesRaf.write(bs);

            // TODO comprovar que no estigui ja close()
            // TODO deslockar
            this.rangesRaf.close();
            this.segmentsRaf.close();
            this.rangesRaf = null;
            this.segmentsRaf = null;
            this.segments.clear();
        }

        protected void storeSegment(RandomAccessFile segmentsRaf, Range<K, V> range, Segment<K, V> segment)
                throws IOException {

            try {
                long atSeek = segmentSize * range.numSegment;
                segmentsRaf.seek(atSeek);
                byte[] bs = DB1.serialize(segment.props);
                if (bs.length > segmentSize) {
                    throw new SegmentExceedsSizeException();
                }
                segmentsRaf.writeInt(bs.length);
                segmentsRaf.write(bs);

                LOG.info("stored segment: " + range + " => " + segment);

                this.segments.put(range, null);

            } catch (SegmentExceedsSizeException e) {

                /*
                 * el segment supera lo permès => splitta en 2 i crida recursiva
                 */

                Range<K, V> range1 = new Range<>();
                Range<K, V> range2 = new Range<>();

                Segment<K, V> segment1 = new Segment<>();
                Segment<K, V> segment2 = new Segment<>();

                segment1.changed = true;
                segment1.props = new TreeMap<>();
                segment2.changed = true;
                segment2.props = new TreeMap<>();

                range1.min = range.min;
                range2.max = range.max;

                boolean addToFirst = true;
                int i = 0;
                for (Entry<K, V> prop : segment.props.entrySet()) {

                    if (i == segment.props.size() / 2) {
                        range1.max = range2.min = prop.getKey();
                        addToFirst = false;
                    }
                    if (addToFirst) {
                        segment1.props.put(prop.getKey(), prop.getValue());
                    } else {
                        segment2.props.put(prop.getKey(), prop.getValue());
                    }

                    i++;
                }

                range1.numSegment = range.numSegment;
                range2.numSegment = this.segments.size();

                this.segments.remove(range); // ?
                this.segments.put(range1, segment1);
                this.segments.put(range2, segment2);

                storeSegment(segmentsRaf, range1, segment1);
                storeSegment(segmentsRaf, range2, segment2);
            }
        }

        @SuppressWarnings("unchecked")
        protected void loadSegment(Range<K, V> range) throws IOException {
            long atSeek = segmentSize * range.numSegment;
            segmentsRaf.seek(atSeek);
            int bslen = segmentsRaf.readInt();
            byte[] bs = new byte[bslen];
            segmentsRaf.read(bs);
            TreeMap<K, V> props = (TreeMap<K, V>) DB1.deserialize(bs);

            Segment<K, V> segment = new Segment<>();
            segment.props = props;
            segment.changed = false;
            segment.hits = 0;

            this.segments.put(range, segment);

            LOG.info("loaded segment: " + range + " => " + segment);
        }

        protected Segment<K, V> loadSegmentFor(Range<K, V> range) throws IOException {
            Segment<K, V> segment = this.segments.get(range);
            if (segment == null) {
                garbageCollect(range);
                loadSegment(range);
                segment = this.segments.get(range);
            }
            segment.hits++;
            return segment;
        }

        /**
         * revisa els segments carregats, i persisteix el que tingui menys hits
         * (descarregant-lo de "this.segments"). Repeteix fins a tenir el #.
         */
        protected void garbageCollect(Range<K, V> excludeRange) throws IOException {

            int cacheSize = 0;
            for (Entry<Range<K, V>, Segment<K, V>> e : this.segments.entrySet()) {
                if (e.getValue() != null) {
                    cacheSize++;
                }
            }

            if (cacheSize >= MAX_CACHE_SIZE) {
                Entry<Range<K, V>, Segment<K, V>> best = null;
                int lowestHits = Integer.MAX_VALUE;
                for (Entry<Range<K, V>, Segment<K, V>> e : this.segments.entrySet()) {
                    if (!e.getKey().equals(excludeRange) && e.getValue() != null && lowestHits > e.getValue().hits) {
                        best = e;
                        lowestHits = e.getValue().hits;
                    }
                }
                if (best != null) {
                    if (best.getValue().changed) {
                        storeSegment(segmentsRaf, best.getKey(), best.getValue());
                    }
                    LOG.info("deallocated " + best);
                }
            }
        }

        public void put(K key, V value) throws IOException {
            Range<K, V> findFor = new Range<>();
            findFor.min = key;
            Range<K, V> range = this.segments.floorKey(findFor);
            Segment<K, V> segment = loadSegmentFor(range);
            segment.props.put(key, value);
            segment.changed = true;
        }

        public void remove(K key) throws IOException {
            Range<K, V> findFor = new Range<>();
            findFor.min = key;
            Range<K, V> range = this.segments.floorKey(findFor);
            Segment<K, V> segment = loadSegmentFor(range);
            segment.props.remove(key);
            segment.changed = true;
        }

        public V get(K key) throws IOException {
            Range<K, V> findFor = new Range<>();
            findFor.min = key;
            Range<K, V> range = this.segments.floorKey(findFor);
            Segment<K, V> segment = loadSegmentFor(range);
            return segment.props.get(key);
        }

        public TreeMap<K, V> get(K min, K max) throws IOException {

            Range<K, V> findForMin = new Range<>();
            findForMin.min = min;
            Range<K, V> rangeMin = this.segments.floorKey(findForMin);

            Range<K, V> findForMax = new Range<>();
            findForMax.min = max;
            Range<K, V> rangeMax = this.segments.floorKey(findForMax);

            Set<Range<K, V>> ranges = this.segments.subMap(rangeMin, true, rangeMax, true).keySet();

            TreeMap<K, V> r = new TreeMap<>();
            for (Range<K, V> range : ranges) {

                Segment<K, V> segment = loadSegmentFor(range);
                for (K key : segment.props.keySet()) {

                    // if (Range.compare(min, key) <= 0 && Range.compare(key, max) <= 0) {
                    // r.put(key, segment.props.get(key));
                    // }
                    if (min == null && max == null) {
                        r.put(key, segment.props.get(key));
                    } else if (min == null) {
                        if (key.compareTo(max) <= 0) {
                            r.put(key, segment.props.get(key));
                        }
                    } else if (max == null) {
                        if (min.compareTo(key) <= 0) {
                            r.put(key, segment.props.get(key));
                        }
                    } else if (min.compareTo(key) <= 0 && key.compareTo(max) <= 0) {
                        r.put(key, segment.props.get(key));
                    }
                }

            }
            return r;
        }

    }

    public static class Range<K extends Comparable<K> & Serializable, V extends Serializable>
            implements Comparable<Range<K, V>>, Serializable {

        private static final long serialVersionUID = -4634386315544736133L;

        K min;
        K max;
        Integer numSegment;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Range other = (Range) obj;
            if (max == null) {
                if (other.max != null) {
                    return false;
                }
            } else if (!max.equals(other.max)) {
                return false;
            }
            if (min == null) {
                if (other.min != null) {
                    return false;
                }
            } else if (!min.equals(other.min)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(Range<K, V> o) {
            return compare(this.min, o.min);
        }

        public static <K extends Comparable<K> & Serializable> int compare(K a, K b) {
            if (a == null && b == null) {
                return 0;
            }
            if (a == null) {
                return -1;
            }
            if (b == null) {
                return 1;
            }
            return a.compareTo(b);
        }

        @Override
        public String toString() {
            return "[" + min + ".." + max + ": " + numSegment + "]";
        }

    }
}

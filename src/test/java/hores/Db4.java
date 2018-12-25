package hores;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hores.DB3.RangesManager.SegmentExceedsSizeException;

/**
 * <pre>
 *
 *  0 0 000000
 *  | | |
 *  | | +-- N
 *  | +---- add/sub ant + N
 *  +------ inverteix mode compressió/noncompressió
 *
 * </pre>
 */
public class Db4 {

    @Test
    public void testName() throws Exception {
        M<Long, String> map = new M<>("testo");
        map.recreate();
        map.initStore(512);

        map.load();
        map.put(1L, "one");
        map.put(2L, "two");
        map.put(3L, "three");
        map.store();
    }

    public static class Segment<K extends Comparable<K> & Serializable, V extends Serializable> {

        TreeMap<K, V> props;
        boolean changed;
        int hits;

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
        // final File lockFile;
        final File rangesFile;
        final File segmentsFile;

        RandomAccessFile rangesRaf;
        RandomAccessFile segmentsRaf;

        public M(String fileName) throws IOException {
            super();
            this.fileName = fileName;

            // this.lockFile = new File(fileName + ".lock");
            this.rangesFile = new File(fileName + ".index");
            this.segmentsFile = new File(fileName + ".chunks");
            if (!rangesFile.exists() || !segmentsFile.exists()) {
                recreate();
            }
        }

        public void recreate() throws IOException {
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

        public void initStore(int chunkMaxSizeInBytes) throws IOException {

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

            store();
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

            // guarda ranges
            rangesRaf.seek(0L);
            rangesRaf.writeInt(segmentSize);
            ArrayList<Range<K, V>> ranges = new ArrayList<>(segments.keySet());
            byte[] bs = DB1.serialize(ranges);
            rangesRaf.writeInt(bs.length);
            rangesRaf.write(bs);

            // guarda segments
            for (Entry<Range<K, V>, Segment<K, V>> entry : segments.entrySet()) {
                Segment<K, V> segment = entry.getValue();
                if (segment != null && segment.changed) {
                    storeSegment(segmentsRaf, entry.getKey(), segment);
                }
            }

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

            } catch (SegmentExceedsSizeException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
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

        public void put(K key, V value) throws IOException {
            Range<K, V> findFor = new Range<>();
            findFor.min = key;
            Range<K, V> range = this.segments.floorKey(findFor);
            Segment<K, V> segment = this.segments.get(range);
            if (segment == null) {
                loadSegment(range);
                segment = this.segments.get(range);
            }
            segment.hits++;
            segment.changed = true;
            segment.props.put(key, value);
        }

    }

    public static class Range<K extends Comparable<K> & Serializable, V extends Serializable>
            implements Comparable<Range<K, V>>, Serializable {

        private static final long serialVersionUID = -4634386315544736133L;

        K min;
        K max;
        Integer numSegment;

        @Override
        public int compareTo(Range<K, V> o) {
            if (this.min == null && o.min == null) {
                return 0;
            }
            if (this.min == null) {
                return -1;
            }
            if (o.min == null) {
                return 1;
            }
            return this.min.compareTo(o.min);
        }

        @Override
        public String toString() {
            return "[" + min + ".." + max + ": " + numSegment + "]";
        }

    }
}

package hores;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import java.util.Map.Entry;

import hores.DB3.CachedSegment;
import hores.DB3.Range;
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
        M<Long,String>map=new M<>();
        map.segmentSize=512;
    }

    public static class Segment<K extends Comparable<K> & Serializable, V extends Serializable> {

        TreeMap<K, V> props;
        boolean changed;
        int hits;
    }

    public static class M<K extends Comparable<K> & Serializable, V extends Serializable> {

        int segmentSize = -1;
        TreeMap<Range<K, V>, Segment<K, V>> segments;

        @SuppressWarnings("unchecked")
        public void load(RandomAccessFile raf) throws IOException {
            raf.seek(0L);
            this.segmentSize = raf.readInt();
            int len = raf.readInt();
            byte[] bs = new byte[len];
            raf.read(bs);
            ArrayList<Range<K, V>> ranges = (ArrayList<Range<K, V>>) DB1.deserialize(bs);
            this.segments = new TreeMap<>();
            for (Range<K, V> range : ranges) {
                this.segments.put(range, null);
            }
        }

        public void store(RandomAccessFile rangesRaf, RandomAccessFile segmentsRaf) throws IOException {

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
            } catch (SegmentExceedsSizeException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        protected void loadSegment(RandomAccessFile segmentsRaf, Range<K, V> range) throws IOException {
            long atSeek = segmentSize * range.numSegment;
            segmentsRaf.seek(atSeek);
            int bslen = segmentsRaf.readInt();
            byte[] bs = new byte[bslen];
            segmentsRaf.read(bs);
            TreeMap<K, V> props = (TreeMap<K, V>) DB1.deserialize(bs);

            Segment<K, V> segment = new Segment<>();
            segment.props = props;
            segment.changed = false;
            segment.hits = 1;

            this.segments.put(range, segment);
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

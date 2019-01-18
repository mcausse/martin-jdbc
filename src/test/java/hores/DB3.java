//
// package hores;
//
// import static org.junit.Assert.assertEquals;
//
// import java.io.File;
// import java.io.IOException;
// import java.io.RandomAccessFile;
// import java.io.Serializable;
// import java.util.ArrayList;
// import java.util.Map.Entry;
// import java.util.TreeMap;
//
// import org.junit.Test;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// import hores.DB3.RangesManager.SegmentExceedsSizeException;
//
// public class DB3 {
//
// @Test
// public void testName() throws Exception {
// DB<Long, String> db = new DB<>("test");
// db.recreate();
//
// db.open();
// db.begin();
// db.put(2L, "2");
// db.put(3L, "3");
// db.put(1L, "1");
// db.commit();
// db.close();
//
// db.open();
// db.begin();
// assertEquals("2", db.get(2L));
// db.close();
//
// }
//
// public static class DB<K extends Comparable<K> & Serializable, V extends
// Serializable> {
//
// static final Logger LOG = LoggerFactory.getLogger(DB.class);
//
// static final int DEFAULT_chunkMaxSizeInBytes = 1024;
//
// final String fileName;
// // final File lockFile;
// final File rangesFile;
// final File segmentsFile;
//
// RandomAccessFile rangesRaf;
// RandomAccessFile segmentsRaf;
//
// final RangesManager<K, V> rangesManager;
//
// public DB(String fileName) throws IOException {
// super();
// this.fileName = fileName;
// this.rangesManager = new RangesManager<>();
//
// // this.lockFile = new File(fileName + ".lock");
// this.rangesFile = new File(fileName + ".index");
// this.segmentsFile = new File(fileName + ".chunks");
// if (!rangesFile.exists() || !segmentsFile.exists()) {
// recreate();
// }
// }
//
// public void open() throws IOException {
// // TODO comprovar que no estigui ja open()
// // TODO lockar
// this.rangesRaf = new RandomAccessFile(rangesFile, "rw");
// this.segmentsRaf = new RandomAccessFile(segmentsFile, "rw");
// }
//
// public void begin() throws IOException {
// this.rangesManager.load(rangesRaf);
// }
//
// public void commit() throws IOException {
// this.rangesManager.store(rangesRaf, segmentsRaf);
// }
//
// public void close() throws IOException {
// // TODO comprovar que no estigui ja close()
// // TODO deslockar
// this.rangesRaf.close();
// this.segmentsRaf.close();
// this.rangesRaf = null;
// this.segmentsRaf = null;
// this.rangesManager.clear();
// }
//
// public void recreate(int chunkMaxSizeInBytes) throws IOException {
// // if (lockFile.exists()) {
// // lockFile.delete();
// // }
// if (rangesFile.exists()) {
// rangesFile.delete();
// }
// if (segmentsFile.exists()) {
// segmentsFile.delete();
// }
// rangesFile.createNewFile();
// segmentsFile.createNewFile();
//
// initStore(chunkMaxSizeInBytes);
//
// LOG.info("recreated DB: " + fileName);
// }
//
// public void recreate() throws IOException {
// recreate(DEFAULT_chunkMaxSizeInBytes);
// }
//
// protected void initStore(int chunkMaxSizeInBytes) throws IOException {
//
// open();
//
// rangesManager.segmentSize = chunkMaxSizeInBytes;
// rangesManager.entries = new TreeMap<>();
//
// Range<K, V> initialRange = new Range<>();
// initialRange.min = null;
// initialRange.max = null;
// initialRange.numSegment = 0;
//
// Segment<K, V> initialSegment = new Segment<>();
// initialSegment.props = new TreeMap<>();
//
// rangesManager.entries.put(initialRange, new CachedSegment<>(initialSegment));
// // rangesManager.store(rangesRaf, segmentsRaf);
//
// commit();
// close();
// }
//
// public void put(K key, V value) throws IOException {
// rangesManager.put(segmentsRaf, key, value);
// }
//
// public V get(K key) throws IOException {
// return rangesManager.get(segmentsRaf, key);
// }
//
// }
//
// public static class CachedSegment<K extends Comparable<K> & Serializable, V
// extends Serializable> {
//
// final Segment<K, V> segment;
// int hitCount;
//
// public CachedSegment(Segment<K, V> segment) {
// super();
// this.segment = segment;
// this.hitCount = 0;
// }
//
// @Override
// public String toString() {
// return "hitCount=" + hitCount + ", " + segment + "]";
// }
//
// }
//
// public static class RangesManager<K extends Comparable<K> & Serializable, V
// extends Serializable> {
//
// static final Logger LOG = LoggerFactory.getLogger(RangesManager.class);
//
// int segmentSize = -1;
// TreeMap<Range<K, V>, CachedSegment<K, V>> entries;
//
// public void put(RandomAccessFile segmentsRaf, K key, V value) throws
// IOException {
// Entrye<K, V> entry = findFor(segmentsRaf, key);
// entry.segment.segment.props.put(key, value);
// }
//
// public void clear() {
// this.segmentSize = -1;
// this.entries = null;
// }
//
// public V get(RandomAccessFile segmentsRaf, K key) throws IOException {
// Entrye<K, V> entry = findFor(segmentsRaf, key);
// return entry.segment.segment.props.get(key);
// }
//
// public static class Entrye<K extends Comparable<K> & Serializable, V extends
// Serializable> {
//
// public final Range<K, V> range;
// public final CachedSegment<K, V> segment;
//
// public Entrye(Range<K, V> range, CachedSegment<K, V> segment) {
// super();
// this.range = range;
// this.segment = segment;
// }
// }
//
// /**
// * busca el rang candidat a contenir K. Si el segment del rang no està
// carregat,
// * el carrega.
// */
// protected Entrye<K, V> findFor(RandomAccessFile segmentsRaf, K key) throws
// IOException {
// Range<K, V> findFor = new Range<>();
// findFor.min = key;
// Entry<Range<K, V>, CachedSegment<K, V>> entry = entries.floorEntry(findFor);
// CachedSegment<K, V> cachedSegment = entry.getValue();
// if (cachedSegment == null) {
// Segment<K, V> segment = new Segment<>();
// segment.load(segmentsRaf, entry.getKey().numSegment, segmentSize);
// cachedSegment = new CachedSegment<>(segment);
//
// System.out.println(this.entries.containsKey(entry.getKey()));
// this.entries.put(entry.getKey(), cachedSegment);
// System.out.println(this.entries.containsKey(entry.getKey()));
// }
// cachedSegment.hitCount++;
//
// return new Entrye<K, V>(entry.getKey(), this.entries.get(entry.getKey()));
// }
//
// public static class SegmentExceedsSizeException extends Exception {
//
// private static final long serialVersionUID = 7670311782431069829L;
// }
//
// public void store(RandomAccessFile rangesRaf, RandomAccessFile segmentsRaf)
// throws IOException {
//
// LOG.info("Storing ranges: " + entries);
//
// // guarda ranges
// rangesRaf.seek(0L);
// rangesRaf.writeInt(segmentSize);
// ArrayList<Range<K, V>> ranges = new ArrayList<>(entries.keySet());
// byte[] bs = DB1.serialize(ranges);
// rangesRaf.writeInt(bs.length);
// rangesRaf.write(bs);
//
// // guarda segments
// for (Entry<Range<K, V>, CachedSegment<K, V>> entry : entries.entrySet()) {
// CachedSegment<K, V> segment = entry.getValue();
// if (segment != null) {
// LOG.info("Storing segment: " + segment);
// storeSegment(segmentsRaf, entry.getKey(), segment);
// }
// }
// }
//
// protected void storeSegment(RandomAccessFile segmentsRaf, Range<K, V> range,
// CachedSegment<K, V> segment)
// throws IOException {
//
// try {
// segment.segment.store(segmentsRaf, segmentSize, range.numSegment);
// } catch (SegmentExceedsSizeException e) {
// // TODO Auto-generated catch block
// throw new RuntimeException(e);
// }
// }
//
// @SuppressWarnings("unchecked")
// public void load(RandomAccessFile raf) throws IOException {
// raf.seek(0L);
// this.segmentSize = raf.readInt();
// int len = raf.readInt();
// byte[] bs = new byte[len];
// raf.read(bs);
// ArrayList<Range<K, V>> ranges = (ArrayList<Range<K, V>>) DB1.deserialize(bs);
// this.entries = new TreeMap<>();
// for (Range<K, V> range : ranges) {
// this.entries.put(range, null);
// }
// }
//
// }
//
// public static class Range<K extends Comparable<K> & Serializable, V extends
// Serializable>
// implements Comparable<Range<K, V>>, Serializable {
//
// private static final long serialVersionUID = -4634386315544736133L;
//
// K min;
// K max;
// int numSegment = -1;
//
// @Override
// public int hashCode() {
// final int prime = 31;
// int result = 1;
// result = prime * result + (max == null ? 0 : max.hashCode());
// result = prime * result + (min == null ? 0 : min.hashCode());
// return result;
// }
//
// @Override
// public boolean equals(Object obj) {
// if (this == obj) {
// return true;
// }
// if (obj == null) {
// return false;
// }
// if (getClass() != obj.getClass()) {
// return false;
// }
// Range other = (Range) obj;
// if (max == null) {
// if (other.max != null) {
// return false;
// }
// } else if (!max.equals(other.max)) {
// return false;
// }
// if (min == null) {
// if (other.min != null) {
// return false;
// }
// } else if (!min.equals(other.min)) {
// return false;
// }
// return true;
// }
//
// @Override
// public int compareTo(Range<K, V> o) {
// if (this.min == null) {
// return -1;
// }
// if (o.min == null) {
// return 1;
// }
// return this.min.compareTo(o.min);
// }
//
// @Override
// public String toString() {
// return "[" + min + ".." + max + ": " + numSegment + "]";
// }
//
// }
//
// public static class Segment<K extends Comparable<K> & Serializable, V extends
// Serializable> {
//
// TreeMap<K, V> props;
//
// public void store(RandomAccessFile segmentsRaf, long segmentSize, long
// numSegment)
// throws IOException, SegmentExceedsSizeException {
// long atSeek = segmentSize * numSegment;
// segmentsRaf.seek(atSeek);
// byte[] bs = DB1.serialize(props);
// if (bs.length > segmentSize) {
// throw new SegmentExceedsSizeException();
// }
// segmentsRaf.writeInt(bs.length);
// segmentsRaf.write(bs);
// }
//
// @SuppressWarnings("unchecked")
// public void load(RandomAccessFile segmentsRaf, long numSegment, int
// segmentSize) throws IOException {
// long atSeek = segmentSize * numSegment;
// segmentsRaf.seek(atSeek);
// int bslen = segmentsRaf.readInt();
// byte[] bs = new byte[bslen];
// segmentsRaf.read(bs);
// this.props = (TreeMap<K, V>) DB1.deserialize(bs);
// }
//
// @Override
// public String toString() {
// return props.toString();
// }
//
// }
//
// }

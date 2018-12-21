package hores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CucarachaDB - micro Key-Value Store
 */
public class CucarachaDb {

//    public abstract class S<E, ID> {
//
//        final CucarachaDb db;
//        final Class<E> entityClass;
//
//        public S(CucarachaDb db, Class<E> entityClass) {
//            super();
//            this.db = db;
//            this.entityClass = entityClass;
//        }
//
//        protected abstract String serialKey(ID t);
//
//        protected abstract String serialValue(E entity);
//
//        protected abstract void deserialKey(E entity, String k);
//
//        protected abstract void deserialValue(E entity, String v);
//
//        public boolean exist(ID id) {
//            String key = serialKey(id);
//            return db.exist(key);
//        }
//
//        public E get(String key) throws Exception {
//            E r = entityClass.newInstance();
//            deserialKey(r, key);
//            String value = db.get(key);
//            deserialValue(r, value);
//            return r;
//        }
//
////        public Map<String, String> find(Predicate<ID> keyPredicate, Predicate<E> valuePredicate) {
////            return db.find(keyPredicate, valuePredicate);
////        }
//
//        public void remove(ID id) {
//            String key = serialKey(id);
//            db.remove(key);
//        }
//
//        public void put(E entity) {
//            String key = serialKey(entity);
//            String value = serialValue(entity);
//            db.put(key, value);
//        }
//
//    }
//
//    class ExpId {
//        int anyExp;
//        long numExp;
//    }
//
//    class Exp {
//        ExpId id;
//        String name;
//        float cost;
//    }
//
//    class ExpS extends S<Exp> {
//
//        public ExpS(CucarachaDb db) {
//            super(db);
//        }
//
//        @Override
//        public String serialKey(Exp t) {
//            if (t.id == null) {
//                throw new RuntimeException();
//            }
//            return t.id.anyExp + "/" + t.id.numExp;
//        }
//
//        @Override
//        public String serialValue(Exp t) {
//            return "name=" + t.name + ",cost=" + t.cost;
//        }
//
//        @Override
//        public void deserialKey(Exp t, String k) {
//            if (t.id == null) {
//                t.id = new ExpId();
//            }
//            String[] ps = k.split("\\/");
//            t.id.anyExp = Integer.parseInt(ps[0]);
//            t.id.numExp = Long.parseLong(ps[1]);
//        }
//
//        @Override
//        public void deserialValue(Exp t, String v) {
//            String[] ps = v.split("\\,");
//            t.name = ps[0].split("\\=")[1];
//            t.cost = Float.parseFloat(ps[1].split("\\=")[1]);
//        }
//
//    }

    public static void main(String[] args) throws Throwable {

        CucarachaDb db = new CucarachaDb(new File("cucarachadb-test.properties"));
        db.removeDatabase();

        threadableSimpleTest(db);

        for (int i = 0; i < 50; i++) {
            new Thread(() -> threadableSimpleTest(db)).start();
        }

        System.out.println("OK");
    }

    private static void threadableSimpleTest(CucarachaDb db) {

        String prefix = String.valueOf(Thread.currentThread().getId()) + ".";

        db.transactional(() -> {
            db.put(prefix + "08-2018/1", "name=chucho,age=9,sex=FEMALE");
            db.put(prefix + "08-2018/2", "name=faria,age=8,sex=FEMALE");
            db.put(prefix + "08-2018/3", "name=din,age=7,sex=MALE");
        });

        db.readOnly(() -> {
            assertEquals("name=chucho,age=9,sex=FEMALE", db.get(prefix + "08-2018/1"));
            assertEquals("name=faria,age=8,sex=FEMALE", db.get(prefix + "08-2018/2"));
            assertEquals("name=din,age=7,sex=MALE", db.get(prefix + "08-2018/3"));
        });

        db.beginTransaction();
        try {
            assertEquals("{" + prefix + "08-2018/2=name=faria,age=8,sex=FEMALE}",
                    db.find(k -> k.startsWith(prefix) && k.endsWith("/2"), v -> v.contains(",age=8,")).toString());
            assertEquals("{}", db.find(k -> false, null).toString());
            assertEquals("{}", db.find(k -> k.startsWith(prefix), v -> false).toString());

            assertFalse(db.find(null, null).isEmpty());

            db.commit();
        } catch (Throwable e) {
            db.rollback();
            throw e;
        }

        db.beginTransaction();
        try {
            db.remove(prefix + "08-2018/1");
            assertEquals(2, db.find(k -> k.startsWith(prefix), null).size());

            db.put(prefix + "08-2018/2", "name=null,age=null,sex=null");
            assertEquals("name=null,age=null,sex=null", db.get(prefix + "08-2018/2"));
            db.commit();
        } catch (Throwable e) {
            db.rollback();
            throw e;
        }

        db.transactional(() -> {
            db.find(k -> k.startsWith(prefix), null).forEach((k, v) -> db.remove(k));
            assertEquals(0, db.find(k -> k.startsWith(prefix), null).size());
        });

    }

    static final Logger LOG = LoggerFactory.getLogger(CucarachaDb.class);

    final File file;
    Properties p;

    final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CucarachaDb(File file) {
        super();
        this.file = file;
        this.p = null;
        createFileIfNotExists();
    }

    protected void createFileIfNotExists() {
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
                LOG.debug("created data file: " + this.file.toString());
            } catch (IOException e) {
                throw new RuntimeException("creating file: " + this.file.toString(), e);
            }
        }
    }

    /* ===== LOCK & TRANSACTION ===== */

    public void transactional(Runnable runnable) {
        beginTransaction();
        try {

            runnable.run();

            commit();
        } catch (Throwable e) {
            rollback();
            throw e;
        }
    }

    public void readOnly(Runnable runnable) {
        beginTransaction();
        try {

            runnable.run();

        } finally {
            rollback();
        }
    }

    public void beginTransaction() {
        // if (p != null) {
        // throw new RuntimeException("yet in active transaction");
        // }
        lock.writeLock().lock();
        loadProperties();
        LOG.debug("=>");
    }

    public void commit() {
        if (p == null) {
            throw new RuntimeException("not in active transaction");
        }
        saveProperties();
        p = null;
        lock.writeLock().unlock();
        LOG.debug("<= commit");
    }

    public void rollback() {
        if (p == null) {
            throw new RuntimeException("not in active transaction");
        }
        p = null;
        lock.writeLock().unlock();
        LOG.debug("<= rollback");
    }

    /* ===== C R U D ===== */

    public boolean exist(String key) {
        boolean r = p.containsKey(key);
        LOG.debug("? " + key + " => " + r);
        return r;
    }

    public String get(String key) {
        if (!p.containsKey(key)) {
            throw new IllegalArgumentException("not found: " + key);
        }
        String r = p.getProperty(key);
        LOG.debug("r " + key + " => " + r);
        return r;
    }

    public Map<String, String> find(Predicate<String> keyPredicate, Predicate<String> valuePredicate) {
        Map<String, String> r = new LinkedHashMap<>();
        for (Object okey : p.keySet()) {
            String key = (String) okey;
            if (keyPredicate == null || keyPredicate.test(key)) {
                String value = p.getProperty(key);
                if (valuePredicate == null || valuePredicate.test(value)) {
                    r.put(key, value);
                }
            }
        }

        LOG.debug("f => " + r.size() + " results.");
        return r;
    }

    public void remove(String key) {
        if (!p.containsKey(key)) {
            throw new IllegalArgumentException("not found: " + key);
        }
        p.remove(key);
        LOG.debug("- " + key);
    }

    public void put(String key, String value) {
        String v;
        if (value == null) {
            v = "";
        } else {
            v = String.valueOf(value);
        }
        p.setProperty(key, v);

        LOG.debug("+ " + key + "=" + value);
    }

    public void putAll(Properties p) {
        for (String k : p.stringPropertyNames()) {
            this.p.put(k, p.getProperty(k));
        }
    }

    public long getNextValue(String seqName) {
        String seqPropName = "seq." + seqName;
        long id;
        if (p.containsKey(seqPropName)) {
            id = Long.valueOf(p.getProperty(seqPropName));
            id++;
            p.setProperty(seqPropName, String.valueOf(id));
        } else {
            id = 0;
            p.setProperty(seqPropName, String.valueOf(id));
        }

        LOG.debug("g " + seqName + " => " + id);
        return id;
    }

    /* ===== PERSISTENCE ===== */

    public Properties getCurrentProperties() {
        return p;
    }

    public void removeDatabase() {
        if (p != null) {
            throw new RuntimeException("cannot remove in active transaction");
        }
        this.file.delete();
        LOG.debug("deleted data file: " + this.file.toString());
    }

    protected void saveProperties() {
        createFileIfNotExists();
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(file);
            p.store(fo, "");
            fo.close();
        } catch (Exception e) {
            throw new RuntimeException("saving transaction to: " + this.file.toString(), e);
        } finally {
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void loadProperties() {
        createFileIfNotExists();
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            p = new Properties();
            p.load(fi);
            fi.close();
        } catch (Exception e) {
            throw new RuntimeException("reading file: " + this.file.toString(), e);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
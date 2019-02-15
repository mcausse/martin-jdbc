// package trash;
//
// import java.util.ArrayList;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
//
// import org.lenteja.jdbc.DataAccesFacade;
// import org.lenteja.jdbc.exception.EmptyResultException;
// import org.lenteja.jdbc.query.IQueryObject;
// import org.lenteja.mapper.Column;
// import org.lenteja.mapper.EntityManager;
// import org.lenteja.mapper.Table;
// import org.lenteja.mapper.query.Operations;
// import org.lenteja.mapper.query.Restrictions;
//
// public class ManyToOne<S, R> {
//
// final Table<S> selfTable;
// final Table<R> refTable;
// final JoinColumn<S, R, ?>[] joinColumns;
//
// @SafeVarargs
// public ManyToOne(Table<S> selfTable, Table<R> refTable, JoinColumn<S, R,
// ?>... joinColumns) {
// super();
// this.selfTable = selfTable;
// this.refTable = refTable;
// this.joinColumns = joinColumns;
//
// // validar que la part dreta de joinColumns siguin columnes PK
// for (JoinColumn<S, R, ?> jc : joinColumns) {
// if (!jc.refColumn.isPk()) {
// throw new RuntimeException("required a PK column, but received: " +
// jc.refColumn.toString());
// }
// }
// }
//
// @SuppressWarnings("unchecked")
// public R fetch(DataAccesFacade facade, S entity) {
// Operations o = new Operations();
//
// List<IQueryObject> onRestrictions = new ArrayList<>();
// for (JoinColumn<S, R, ?> jc : joinColumns) {
// onRestrictions.add(jc.getRestriction());
// }
//
// List<IQueryObject> whereRestrictions = new ArrayList<>();
// for (Column<S, ?> selfc : selfTable.getColumns()) {
// if (selfc.isPk()) {
// Object whereValue = selfc.getAccessor().get(entity);
// Column<S, Object> selfColumn = (Column<S, Object>) selfc;
// whereRestrictions.add(selfColumn.eq(whereValue));
// }
// }
//
// try {
// return o.query(refTable) //
// .append("select {} from {} ", refTable.all(), refTable) //
// .append("join {} ", selfTable) //
// .append("on {} ", Restrictions.and(onRestrictions)) //
// .append("where {}", Restrictions.and(whereRestrictions)) //
// .getExecutor(facade) //
// .loadUnique();
// } catch (EmptyResultException e) {
// return null;
// }
// }
//
// public Map<S, R> fetch(DataAccesFacade facade, Iterable<S> entities) {
// Map<S, R> r = new LinkedHashMap<>();
// for (S e : entities) {
// r.put(e, fetch(facade, e));
// }
// return r;
// }
//
// /**
// * <ul>
// * <li>guarda parent/ref si es demana, i si es pot (pot ser null)
// * <li>actualitza les FK del child/self
// * <li>guarda el child/self
// */
// public void storeParentAndChild(DataAccesFacade facade, S child, R parent) {
// storeParentAndChild(facade, child, parent, true);
// }
//
// /**
// * <ul>
// * <li>guarda parent/ref si es demana, i si es pot (pot ser null)
// * <li>actualitza les FK del child/self
// * <li>guarda el child/self
// */
// public void storeParentAndChild(DataAccesFacade facade, S child, R parent,
// boolean storeParentToo) {
//
// EntityManager em = new EntityManager(facade);
//
// if (parent == null) {
// /**
// * actualitza FKs del parent a null per a desfer la relació
// */
// for (JoinColumn<S, R, ?> jc : joinColumns) {
// jc.selfColumn.getAccessor().set(child, null);
// }
//
// } else {
//
// if (storeParentToo) {
// em.store(refTable, parent);
// }
//
// /**
// * actualitza FKs del parent
// */
// for (JoinColumn<S, R, ?> jc : joinColumns) {
// Object value = jc.refColumn.getAccessor().get(parent);
// jc.selfColumn.getAccessor().set(child, value);
// }
//
// }
//
// em.store(selfTable, child);
// }
//
// public Table<S> getSelfTable() {
// return selfTable;
// }
//
// public Table<R> getRefTable() {
// return refTable;
// }
//
// public JoinColumn<S, R, ?>[] getJoinColumns() {
// return joinColumns;
// }
//
// }
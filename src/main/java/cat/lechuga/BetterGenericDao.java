// package cat.lechuga;
//
// import java.util.ArrayList;
// import java.util.List;
//
// import cat.lechuga.mql.Executor;
// import cat.lechuga.mql.Orders;
// import cat.lechuga.mql.Orders.Order;
// import cat.lechuga.mql.QueryBuilder;
// import cat.lechuga.tsmql.Criterion;
// import cat.lechuga.tsmql.MetaTable;
// import cat.lechuga.tsmql.Restrictions;
// import cat.lechuga.tsmql.TOrders;
// import cat.lechuga.tsmql.TOrders.TOrder;
// import cat.lechuga.tsmql.TypeSafeQueryBuilder;
//
//// TODO deprecar
// public class BetterGenericDao<E, ID> {
//
// final EntityManager em;
// final GenericDao<E, ID> genericDao;
// final MetaTable<E> metaTable;
//
// public BetterGenericDao(EntityManager em, MetaTable<E> metaTable) {
// super();
// this.em = em;
// this.genericDao = new GenericDao<>(em, metaTable.getEntityClass());
// this.metaTable = metaTable;
// }
//
// public E loadUniqueBy(Criterion... criterions) {
// return getExecutorFor(criterions, null).loadUnique();
// }
//
// public E loadFirstBy(Criterion... criterions) { // TODO afegir el loadFirst
// arreu
// return getExecutorFor(criterions, null).loadFirst();
// }
//
// public List<E> loadBy(Criterion... criterions) {
// return getExecutorFor(criterions, null).load();
// }
//
// public List<E> loadBy(List<Criterion> criterions, TOrders<E> orders) {
// return loadBy(Restrictions.and(criterions), orders);
// }
//
// public List<E> loadBy(Criterion criterion, TOrders<E> orders) {
// TypeSafeQueryBuilder qb = new TypeSafeQueryBuilder(em);
// qb.addAlias(metaTable);
// qb.append("select {} ", metaTable.all());
// qb.append("from {} ", metaTable);
// qb.append("where {}", criterion);
// if (orders != null) {
// qb.append("order by ");
// int c = 0;
// for (TOrder<E> o : orders.getOrders()) {
// if (c > 0) {
// qb.append(",");
// }
// qb.append("{} ", o.getMetaColumn());
// qb.append(o.getOrder());
// c++;
// }
// }
// return qb.getExecutor(genericDao.getEntityClass()).load();
// }
//
// public Executor<E> getExecutorFor(Criterion[] criterions, TOrders<E> orders)
// {
// TypeSafeQueryBuilder qb = new TypeSafeQueryBuilder(em);
// qb.addAlias(metaTable);
// qb.append("select {} ", metaTable.all());
// qb.append("from {} ", metaTable);
// if (criterions != null && criterions.length > 0) {
// qb.append("where ", metaTable);
// int c = 0;
// for (Criterion criterion : criterions) {
// if (c > 0) {
// qb.append(" and ");
// }
// c++;
// qb.append("{}", criterion);
// }
// }
// if (orders != null && !orders.getOrders().isEmpty()) {
// qb.append("order by ");
// int c = 0;
// for (TOrder<E> o : orders.getOrders()) {
// if (c > 0) {
// qb.append(",");
// }
// qb.append("{} ", o.getMetaColumn());
// qb.append(o.getOrder());
// c++;
// }
// }
// return qb.getExecutor(genericDao.getEntityClass());
// }
//
// // ===========================================================
// // ===========================================================
// // ===========================================================
//
// public QueryBuilder buildQuery() {
// return em.buildQuery();
// }
//
// public TypeSafeQueryBuilder buildTypedQuery() {
// return em.buildTypeSafeQuery();
// }
//
// public EntityManager getEntityManager() {
// return em;
// }
//
// public Class<E> getEntityClass() {
// return this.metaTable.getEntityClass();
// }
//
// // ===========================================================
// // ===========================================================
// // ===========================================================
//
// public E loadUniqueByExample(E example) {
// return genericDao.loadUniqueByExample(example);
// }
//
// public List<E> loadByExample(E example) {
// return genericDao.loadByExample(example);
// }
//
// public List<E> loadByExample(E example, Orders<E> orders) {
// return genericDao.loadByExample(example, orders);
// }
//
// public List<E> loadByExample(E example, TOrders<E> orders) {
// List<Order<E>> os = new ArrayList<>();
// for (TOrder<E> o : orders.getOrders()) {
// if (Order.ASC.equals(o.getOrder())) {
// os.add(Order.asc(o.getMetaColumn().getPropertyName()));
// } else {
// os.add(Order.desc(o.getMetaColumn().getPropertyName()));
// }
// }
// return loadByExample(example, new Orders<>(os));
// }
//
// public List<E> loadAll() {
// return genericDao.loadAll();
// }
//
// public E loadById(ID id) {
// return genericDao.loadById(id);
// }
//
// public void refresh(E entity) {
// genericDao.refresh(entity);
// }
//
// public void store(E entity) {
// genericDao.store(entity);
// }
//
// public void update(E entity) {
// genericDao.update(entity);
// }
//
// public void insert(E entity) {
// genericDao.insert(entity);
// }
//
// public void delete(E entity) {
// genericDao.delete(entity);
// }
//
// public boolean existsById(ID id) {
// return genericDao.existsById(id);
// }
//
// public boolean exists(E entity) {
// return genericDao.exists(entity);
// }
//
// public void storeAll(Iterable<E> entities) {
// genericDao.storeAll(entities);
// }
//
// public void updateAll(Iterable<E> entities) {
// genericDao.updateAll(entities);
// }
//
// public void insertAll(Iterable<E> entities) {
// genericDao.insertAll(entities);
// }
//
// public void deleteAll(Iterable<E> entities) {
// genericDao.deleteAll(entities);
// }
//
// }

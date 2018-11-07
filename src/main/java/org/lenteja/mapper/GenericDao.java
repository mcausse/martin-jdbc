package org.lenteja.mapper;

import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Query;

public class GenericDao<E, ID> {

    final DataAccesFacade facade;
    final Table<E> table;
    final EntityManager em;

    public GenericDao(DataAccesFacade facade, Table<E> table) {
        super();
        this.facade = facade;
        this.table = table;
        this.em = new EntityManager(facade);
    }

    public void refresh(E entity) {
        em.refresh(table, entity);
    }

    public E loadById(ID id) {
        return em.loadById(table, id);
    }

    public void store(E entity) {
        em.store(table, entity);
    }

    public boolean exists(E entity) {
        return em.exists(table, entity);
    }

    public void insert(E entity) {
        em.insert(table, entity);
    }

    public void update(E entity) {
        em.update(table, entity);
    }

    public void update(E entity, Iterable<Column<E, ?>> columnsToUpdate) {
        em.update(table, entity, columnsToUpdate);
    }

    public void update(Table<E> table, E example, Iterable<Column<E, ?>> columnsToUpdate, IQueryObject wherePredicate) {
        em.update(table, example, columnsToUpdate, wherePredicate);
    }

    public void delete(E entity) {
        em.delete(table, entity);
    }

    public void storeAll(Iterable<E> entities) {
        em.storeAll(table, entities);
    }

    public void insertAll(Iterable<E> entities) {
        em.insertAll(table, entities);
    }

    public void updateAll(Iterable<E> entities) {
        em.updateAll(table, entities);
    }

    public void deleteAll(Iterable<E> entities) {
        em.deleteAll(table, entities);
    }

    public Query<E> queryFor() {
        return em.queryFor(table);
    }

    public Query<E> queryFor(Table<E> aliasedTable) {
        return em.queryFor(aliasedTable);
    }

    public <C> Query<C> scalarQueryFor(Column<?, C> column) {
        return em.scalarQueryFor(column);
    }

    public List<E> query(IQueryObject restriction, List<Order<E>> orders) {
        return em.query(table, restriction, orders);
    }

    // public List<E> query(List<Order<E>> orders) {
    // return em.query(table, orders);
    // }

    public List<E> query(IQueryObject restriction) {
        return em.query(table, restriction);
    }

    public E queryUnique(IQueryObject restrictions) {
        return em.queryUnique(table, restrictions);
    }

    public List<E> query(E example, List<Order<E>> orders) {
        return em.query(table, example, orders);
    }

    public List<E> query(E example) {
        return em.query(table, example);
    }

    public E queryUnique(E example) {
        return em.queryUnique(table, example);
    }

    public DataAccesFacade getFacade() {
        return facade;
    }

    public EntityManager getEntityManager() {
        return em;
    }

}

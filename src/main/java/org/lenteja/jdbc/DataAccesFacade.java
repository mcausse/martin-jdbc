package org.lenteja.jdbc;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.lenteja.jdbc.exception.EmptyResultException;
import org.lenteja.jdbc.exception.TooManyResultsException;
import org.lenteja.jdbc.extractor.ResultSetExtractor;
import org.lenteja.jdbc.query.IQueryObject;

import cat.lechuga.Mapable;

public interface DataAccesFacade {

    DataSource getDataSource();

    <T> T loadUnique(IQueryObject q, Mapable<T> mapable) throws TooManyResultsException, EmptyResultException;

    <T> T loadFirst(IQueryObject q, Mapable<T> mapable) throws EmptyResultException;

    <T> List<T> load(IQueryObject q, Mapable<T> mapable);

    int update(IQueryObject q);

    <T> T extract(IQueryObject q, ResultSetExtractor<T> extractor);

    void begin();

    void commit();

    void rollback();

    boolean isValidTransaction();

    Connection getCurrentConnection();

}

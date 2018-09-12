package org.lenteja.mapper.query;

import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.extractor.Pager;
import org.lenteja.jdbc.extractor.ResultSetExtractor;
import org.lenteja.jdbc.extractor.ResultSetPagedExtractor;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Mapable;

public class Executor<E> {

    final DataAccesFacade facade;
    final IQueryObject qo;
    final Mapable<E> mapable;

    public Executor(DataAccesFacade facade, IQueryObject qo, Mapable<E> mapable) {
        super();
        this.facade = facade;
        this.qo = qo;
        this.mapable = mapable;
    }

    public int update() {
        return facade.update(qo);
    }

    public E loadUnique() {
        return facade.loadUnique(qo, mapable);
    }

    public List<E> load() {
        return facade.load(qo, mapable);
    }

    public Pager<E> loadPage(Pager<E> pager) {
        ResultSetExtractor<Pager<E>> extractor = new ResultSetPagedExtractor<E>(mapable, pager.getPageSize(),
                pager.getNumPage());
        return facade.extract(qo, extractor);
    }
}
// package cat.lechuga.jdbc;
//
// import org.lenteja.jdbc.DataAccesFacade;
// import org.lenteja.jdbc.query.IQueryObject;
// import org.lenteja.mapper.autogen.ScalarHandler;
// import org.lenteja.mapper.autogen.ScalarMappers;
// import org.lenteja.mapper.query.Operations;
//
// public abstract class Generator {
//
// final ScalarHandler<?> handler;
// final boolean beforeInsert;
//
// public Generator(Class<?> columnClass, boolean beforeInsert) {
// super();
// this.handler = ScalarMappers.getScalarMapperFor(columnClass);
// this.beforeInsert = beforeInsert;
// }
//
// public Object generate(DataAccesFacade facade) {
// return new
// Operations().query(handler).append(getQuery()).getExecutor(facade).loadUnique();
// }
//
// public boolean isBeforeInsert() {
// return beforeInsert;
// }
//
// protected abstract IQueryObject getQuery();
//
// }
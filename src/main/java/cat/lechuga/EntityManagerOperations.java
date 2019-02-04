package cat.lechuga;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class EntityManagerOperations {

    public IQueryObject loadAll(EntityMeta<?> entityMeta) {
        QueryObject q = new QueryObject();
        q.append("select ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getAllProps()) {
                if (c > 0) {
                    q.append(",");
                }
                q.append(p.getColumnName());
                c++;
            }
        }
        q.append(" from ");
        q.append(entityMeta.getTableName());
        return q;
    }

    public IQueryObject loadById(EntityMeta<?> entityMeta, Object id) {
        QueryObject q = new QueryObject();
        q.append("select ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getAllProps()) {
                if (c > 0) {
                    q.append(",");
                }
                q.append(p.getColumnName());
                c++;
            }
        }
        q.append(" from ");
        q.append(entityMeta.getTableName());
        q.append(" where ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (c > 0) {
                    q.append(" and ");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(1, id));
                c++;
            }
        }
        return q;
    }

    public IQueryObject refresh(EntityMeta<?> entityMeta, Object entity) {
        QueryObject q = new QueryObject();
        q.append("select ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getAllProps()) {
                if (c > 0) {
                    q.append(",");
                }
                q.append(p.getColumnName());
                c++;
            }
        }
        q.append(" from ");
        q.append(entityMeta.getTableName());
        q.append(" where ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (c > 0) {
                    q.append(" and ");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(entity));
                c++;
            }
        }
        return q;
    }

    public IQueryObject insert(EntityMeta<?> entityMeta, Object entity) {
        QueryObject q = new QueryObject();
        q.append("insert into ");
        q.append(entityMeta.getTableName());
        q.append(" (");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getAllProps()) {
                if (c > 0) {
                    q.append(",");
                }
                q.append(p.getColumnName());
                c++;
            }
        }
        q.append(") values (");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getAllProps()) {
                if (c > 0) {
                    q.append(",");
                }
                q.append("?");
                q.addArg(p.getJdbcValue(entity));
                c++;
            }
        }
        q.append(")");
        return q;
    }

    public IQueryObject update(EntityMeta<?> entityMeta, Object entity) {
        QueryObject q = new QueryObject();
        q.append("update ");
        q.append(entityMeta.getTableName());
        q.append(" set ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getRegularProps()) {
                if (c > 0) {
                    q.append(",");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(entity));
                c++;
            }
        }
        q.append(" where ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (c > 0) {
                    q.append(" and ");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(entity));
                c++;
            }
        }
        return q;
    }

    public IQueryObject delete(EntityMeta<?> entityMeta, Object entity) {
        QueryObject q = new QueryObject();
        q.append("delete from ");
        q.append(entityMeta.getTableName());
        q.append(" where ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (c > 0) {
                    q.append(" and ");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(entity));
                c++;
            }
        }
        return q;
    }

    public IQueryObject existsById(EntityMeta<?> entityMeta, Object id) {
        QueryObject q = new QueryObject();
        q.append("select count(*) from ");
        q.append(entityMeta.getTableName());
        q.append(" where ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (c > 0) {
                    q.append(" and ");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(1, id));
                c++;
            }
        }
        return q;
    }

    public IQueryObject exists(EntityMeta<?> entityMeta, Object entity) {
        QueryObject q = new QueryObject();
        q.append("select count(*) from ");
        q.append(entityMeta.getTableName());
        q.append(" where ");
        {
            int c = 0;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (c > 0) {
                    q.append(" and ");
                }
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(entity));
                c++;
            }
        }
        return q;
    }
}
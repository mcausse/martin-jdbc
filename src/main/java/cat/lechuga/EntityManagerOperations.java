package cat.lechuga;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class EntityManagerOperations<E> {

    private final EntityMeta<E> entityMeta;

    public EntityManagerOperations(EntityMeta<E> entityMeta) {
        super();
        this.entityMeta = entityMeta;
    }

    public IQueryObject loadAll() {
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

    public IQueryObject loadById(Object id) {
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

    public IQueryObject refresh(E entity) {
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

    public IQueryObject insert(E entity) {
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

    public IQueryObject update(E entity) {
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

    public IQueryObject delete(E entity) {
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

    public IQueryObject deleteById(Object id) {
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
                q.addArg(p.getJdbcValue(1, id));
                c++;
            }
        }
        return q;
    }

    public IQueryObject existsById(Object id) {
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

    public IQueryObject exists(E entity) {
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
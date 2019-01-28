package cat.lechuga;

public abstract class EntityListener<E> {

    public void afterLoad(E entity) {
    }

    public void beforeStore(E entity) {
    }

    public void afterStore(E entity) {
    }

    public void beforeInsert(E entity) {
    }

    public void afterInsert(E entity) {
    }

    public void beforeUpdate(E entity) {
    }

    public void afterUpdate(E entity) {
    }

    public void beforeDelete(E entity) {
    }

    public void afterDelete(E entity) {
    }

}
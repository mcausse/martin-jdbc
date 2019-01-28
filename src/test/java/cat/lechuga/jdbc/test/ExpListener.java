package cat.lechuga.jdbc.test;

import cat.lechuga.EntityListener;

public class ExpListener extends EntityListener<Exp> {

    @Override
    public void afterLoad(Exp p) {
        System.out.println("afterLoad: " + p);
    }

    @Override
    public void beforeStore(Exp p) {
        System.out.println("beforeStore: " + p);
    }

    @Override
    public void afterStore(Exp p) {
        System.out.println("afterStore: " + p);
    }

    @Override
    public void beforeInsert(Exp p) {
        System.out.println("beforeInsert: " + p);
    }

    @Override
    public void afterInsert(Exp p) {
        System.out.println("afterInsert: " + p);
    }

    @Override
    public void beforeUpdate(Exp p) {
        System.out.println("beforeUpdate: " + p);
    }

    @Override
    public void afterUpdate(Exp p) {
        System.out.println("afterUpdate: " + p);
    }

    @Override
    public void beforeDelete(Exp p) {
        System.out.println("beforeDelete: " + p);
    }

    @Override
    public void afterDelete(Exp p) {
        System.out.println("afterDelete: " + p);
    }

}

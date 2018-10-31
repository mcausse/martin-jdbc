package org.lenteja.test;

import org.lenteja.mapper.Column;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.autogen.impl.HsqldbIdentity;
import org.lenteja.mapper.handler.EnumColumnHandler;

public class DogTable extends Table<Dog> {

    public final Column<Dog, Integer> idDog = addPkColumn(Integer.class, "idDog", "id_dog");
    public final Column<Dog, String> name = addColumn(String.class, "name", "name");
    public final Column<Dog, Boolean> alive = addColumn(Boolean.class, "alive", "is_alive");
    public final Column<Dog, ESex> sex = addColumn(ESex.class, "sex", "sex", new EnumColumnHandler<>(ESex.class));
    public final Column<Dog, Long> idJefe = addColumn(Long.class, "idJefe", "id_jefe");

    public DogTable() {
        super("dogs");
        addAutoGenerated(new HsqldbIdentity<>(idDog));
    }

}
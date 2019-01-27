package cat.lechuga.tsmql;

import cat.lechuga.EntityMeta;
import cat.lechuga.PropertyMeta;

public class MetaGenerator {

    public static String generateMetaColumns(EntityMeta<?> meta, int uniqueIndex) {

        StringBuilder s = new StringBuilder();

        s.append("public static class " + meta.getEntityClass().getSimpleName() + "_ extends MetaTable<");
        s.append(meta.getEntityClass().getSimpleName());
        s.append("> {\n\n");
        for (PropertyMeta p : meta.getAllProps()) {
            s.append("\tpublic final MetaColumn<");
            s.append(meta.getEntityClass().getSimpleName());
            s.append(", ");
            s.append(p.getProp().getType().getSimpleName());
            s.append(">");
            s.append(p.getProp().getLastName() + " = ");
            s.append("addColumn(\"" + p.getProp().getFullName() + "\");\n");
        }

        s.append("\n");
        s.append("\tpublic " + meta.getEntityClass().getSimpleName() + "_() {\n");
        s.append("\t\tsuper(" + meta.getEntityClass().getSimpleName() + ".class, ");
        String alias = meta.getEntityClass().getSimpleName().toLowerCase();
        if (alias.length() >= 6) {
            alias = alias.substring(0, 6) + uniqueIndex;
        }
        s.append("\"" + alias + "\");\n");

        s.append("\t}\n");

        s.append("\n");
        s.append("\tpublic " + meta.getEntityClass().getSimpleName() + "_(String alias) {\n");
        s.append("\t\tsuper(" + meta.getEntityClass().getSimpleName() + ".class, alias);\n");
        s.append("\t}\n");

        s.append("}\n");
        return s.toString();
    }

}

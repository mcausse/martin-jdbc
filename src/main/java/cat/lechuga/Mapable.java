package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interfície que tributa la capacitat d'extreure un valor tipat d'un
 * {@link ResultSet}. Aquest valor pot ser una entitat, o un valor escalar.
 *
 * @author mhoms
 *
 * @param <T> resultat del mapping
 *
 * @see EntityManager
 * @see GenericDao
 */
public interface Mapable<T> {

    T map(ResultSet rs) throws SQLException;

}
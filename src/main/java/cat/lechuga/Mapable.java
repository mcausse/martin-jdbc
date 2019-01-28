package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interfície que tributa la capacitat de mapejar un valor d'un
 * {@link ResultSet}. Tal valor pot resultar ser una entitat, o un valor
 * escalar,
 *
 * @author mhoms
 *
 * @param <T> resultat del mapping
 *
 * @see EntityManager
 * @see GenericDao
 * @see BetterGenericDao
 */
public interface Mapable<T> {

    T map(ResultSet rs) throws SQLException;

}
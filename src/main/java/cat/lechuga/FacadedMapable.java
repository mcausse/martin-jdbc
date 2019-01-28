package cat.lechuga;

import org.lenteja.jdbc.DataAccesFacade;

/**
 * Interfície que a més de tributar les propietats de {@link Mapable} dóna accés
 * a {@link DataAccesFacade}.
 *
 * @author mhoms
 *
 * @param <T>
 *
 * @see Mapable
 */
public interface FacadedMapable<T> extends Facaded, Mapable<T> {

}
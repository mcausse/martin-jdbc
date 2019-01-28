package cat.lechuga;

import org.lenteja.jdbc.DataAccesFacade;

/**
 * Interfície que tributa la propietat de donar accés a {@link DataAccesFacade}.
 *
 * @author mhoms
 *
 * @param <T>
 *
 * @see Mapable
 */
public interface Facaded {

    DataAccesFacade getFacade();

}
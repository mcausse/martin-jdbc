package cat.lechuga;

/**
 * Interfície que tributa la propietat de donar accés a {@link EntityMeta}.
 *
 * @author mhoms
 *
 * @param <T>
 *
 */
public interface EntityMetable<E> {

    EntityMeta<E> getEntityMeta();

}
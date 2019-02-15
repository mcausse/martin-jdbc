package cat.lechuga.repository;

import java.util.List;
import java.util.Optional;

import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.TOrders;

// https://www.baeldung.com/rest-api-search-language-spring-data-specifications
public interface IRepository<E, ID, E_ extends MetaTable<E>> {

    void save(E entity);

    void saveAll(Iterable<E> entities);

    Optional<E> findById(ID id);

    boolean existsById(ID id);

    boolean exists(E entity);

    List<E> findAll();

    void deleteById(ID id);

    void delete(E entity);

    void deleteAll(Iterable<E> entities);

    ////////////////////////////////////////////////////

    Optional<E> findUniqueBy(Specification<E_> spec);

    List<E> findBy(Specification<E_> spec);

    List<E> findBy(Specification<E_> spec, TOrders orders);

}

package se.curvysoft.eve.tools.db;

import org.springframework.data.repository.CrudRepository;
import se.curvysoft.eve.tools.model.Item;

import java.util.Set;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    Item findFirstByTypeNameEquals(String typeName);
    Item findFirstByTypeIDEquals(Integer typeID);
    Set<Item> findAllByTypeNameIn(Set<String> names);
}

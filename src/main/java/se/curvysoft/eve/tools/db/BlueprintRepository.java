package se.curvysoft.eve.tools.db;

import org.springframework.data.repository.CrudRepository;
import se.curvysoft.eve.tools.model.Blueprint;

import java.util.Set;

public interface BlueprintRepository extends CrudRepository<Blueprint, Integer> {
    Set<Blueprint> findAllByItemTypeNameIn(Set<String> typeNames);
    Blueprint findByItemTypeNameEquals(String typeName);
    Set<Blueprint> findAllByTypeIDIn(Set<Integer> typeIds);
}

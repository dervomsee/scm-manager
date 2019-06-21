package sonia.scm.update;

import java.util.Map;
import java.util.function.BiConsumer;

public interface V1PropertyReader {

  V1PropertyReader REPOSITORY_PROPERTY_READER = new RepositoryV1PropertyReader();
  V1PropertyReader USER_PROPERTY_READER = new RepositoryV1PropertyReader();
  V1PropertyReader GROUP_PROPERTY_READER = new RepositoryV1PropertyReader();

  String getStoreName();

  Instance createInstance(Map<String, V1Properties> all);

  interface Instance {
    /**
     * Will call the given consumer for each id of the corresponding entity with its list of
     * properties converted from v1.
     * For example for repositories this will call the consumer with the id of each repository
     * that had properties attached in v1.
     */
    void forEachEntry(BiConsumer<String, V1Properties> propertiesForNameConsumer);

    /**
     * Filters for entities only having at least one property with a given key name.
     */
    Instance havingAnyOf(String... keys);

    /**
     * Filters for entities only having properties for all given key name.
     */
    Instance havingAllOf(String... keys);
  }
}

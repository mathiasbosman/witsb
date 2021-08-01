package be.mathiasbosman.db;

import be.mathiasbosman.entity.WitsbEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WitsbRepository <E extends WitsbEntity<UUID>> extends JpaRepository<E, UUID> {
  static <E extends WitsbEntity<UUID>> E getById(WitsbRepository<E> repo, UUID id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException(
        "No result for id = " + id + " in repository " + repo.getClass()));
  }
}

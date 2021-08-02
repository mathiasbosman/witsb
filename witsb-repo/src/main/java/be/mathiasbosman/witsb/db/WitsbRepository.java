package be.mathiasbosman.witsb.db;

import be.mathiasbosman.witsb.entity.WitsbEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface WitsbRepository <E extends WitsbEntity<UUID>> extends JpaRepository<E, UUID> {
  static <E extends WitsbEntity<UUID>> E getById(WitsbRepository<E> repo, UUID id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException(
        "No result for id = " + id + " in repository " + repo.getClass()));
  }
}

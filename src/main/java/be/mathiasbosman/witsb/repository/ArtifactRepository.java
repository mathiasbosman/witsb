package be.mathiasbosman.witsb.repository;

import be.mathiasbosman.witsb.domain.Artifact;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ArtifactRepository extends PagingAndSortingRepository<Artifact, UUID>,
    CrudRepository<Artifact, UUID> {

  Optional<Artifact> findByReference(UUID reference);

  Artifact getFirstByGroupIdOrderByVersionDesc(UUID groupId);

  Optional<Artifact> findByGroupIdAndVersion(UUID groupId, int version);

  List<Artifact> getByGroupId(UUID groupId, Sort sort);

  void deleteByGroupId(UUID groupId);

  List<Artifact> getByLockGroupId(UUID lockGroupId);

  List<Artifact> findByUploadedOnBefore(LocalDateTime checkpoint);

}

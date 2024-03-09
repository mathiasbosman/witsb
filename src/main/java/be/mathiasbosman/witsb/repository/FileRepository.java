package be.mathiasbosman.witsb.repository;

import be.mathiasbosman.witsb.domain.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FileRepository extends PagingAndSortingRepository<File, UUID>,
    CrudRepository<File, UUID> {

  Optional<File> findByReference(UUID reference);

  File getFirstByGroupIdOrderByVersionDesc(UUID groupId);

  Optional<File> findByGroupIdAndVersion(UUID groupId, int version);

  List<File> getByGroupId(UUID groupId, Sort sort);

  List<File> getByGroupId(UUID groupId);
}

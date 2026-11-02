package dev.outcrop.source;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceMethodRepository extends JpaRepository<SourceMethod, UUID> {

	List<SourceMethod> findAllBySourceClassId(UUID sourceClassId);

}

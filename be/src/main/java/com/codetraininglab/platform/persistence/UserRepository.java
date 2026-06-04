package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

  long countByDeletedAtIsNull();

  long countByDeletedAtIsNullAndRole(UserRole role);

  List<UserEntity> findAllByDeletedAtIsNullOrderByEmailAsc();

  List<UserEntity> findAllByOrderByEmailAsc();

  long countByDeletedAtIsNullAndRoleAndIdNot(UserRole role, UUID id);
}

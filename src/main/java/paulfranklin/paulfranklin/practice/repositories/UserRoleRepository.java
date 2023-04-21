package paulfranklin.paulfranklin.practice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paulfranklin.paulfranklin.practice.entities.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
}

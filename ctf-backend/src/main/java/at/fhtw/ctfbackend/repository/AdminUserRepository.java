package at.fhtw.ctfbackend.repository;

import at.fhtw.ctfbackend.entity.AdminUserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserEntity, String> {
}

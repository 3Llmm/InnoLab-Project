// FileRepository.java
package at.fhtw.ctfbackend.repository;

import at.fhtw.ctfbackend.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, String> {
}
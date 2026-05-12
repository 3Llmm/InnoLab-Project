package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.entity.AdminUserEntity;
import at.fhtw.ctfbackend.repository.AdminUserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final AdminUserRepository adminUserRepository;

    public AdminUserController(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @PostConstruct
    public void seedDefaultAdmins() {
        if (adminUserRepository.count() == 0) {
            List<AdminUserEntity> defaults = List.of(
                    new AdminUserEntity("if24b120"),
                    new AdminUserEntity("if24b234")
            );
            adminUserRepository.saveAll(defaults);
            logger.info("Seeded {} default admin users", defaults.size());
        }
    }

    @GetMapping
    public List<String> getAllAdmins() {
        return adminUserRepository.findAll().stream()
                .map(AdminUserEntity::getUsername)
                .toList();
    }

    @PutMapping("/{username}")
    public ResponseEntity<Map<String, String>> addAdmin(@PathVariable String username) {
        if (adminUserRepository.existsById(username)) {
            return ResponseEntity.ok(Map.of("message", "User is already an admin"));
        }
        adminUserRepository.save(new AdminUserEntity(username));
        logger.info("Added admin user: {}", username);
        return ResponseEntity.ok(Map.of("message", "Admin added successfully"));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> removeAdmin(@PathVariable String username) {
        if (!adminUserRepository.existsById(username)) {
            return ResponseEntity.notFound().build();
        }
        adminUserRepository.deleteById(username);
        logger.info("Removed admin user: {}", username);
        return ResponseEntity.ok(Map.of("message", "Admin removed successfully"));
    }
}

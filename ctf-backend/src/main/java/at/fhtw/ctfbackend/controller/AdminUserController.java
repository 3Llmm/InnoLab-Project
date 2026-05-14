package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.dto.UserAdminUpdateDto;
import at.fhtw.ctfbackend.dto.UserDto;
import at.fhtw.ctfbackend.entity.AdminUserEntity;
import at.fhtw.ctfbackend.entity.UserEntity;
import at.fhtw.ctfbackend.repository.AdminUserRepository;
import at.fhtw.ctfbackend.services.UserService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final UserService userService;
    private final AdminUserRepository adminUserRepository;

    public AdminUserController(
        UserService userService,
        AdminUserRepository adminUserRepository
    ) {
        this.userService = userService;
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
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService
            .getUserById(id)
            .map(this::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable Long id,
        @RequestBody UserAdminUpdateDto updateDto
    ) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity updated = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(toDto(updated));
    }

    private UserDto toDto(UserEntity entity) {
        return UserDto
            .builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .email(entity.getEffectiveEmail())
            .displayName(entity.getDisplayName())
            .isAdmin(entity.getIsAdmin())
            .isActive(entity.getIsActive())
            .lastLoginAt(entity.getLastLoginAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}

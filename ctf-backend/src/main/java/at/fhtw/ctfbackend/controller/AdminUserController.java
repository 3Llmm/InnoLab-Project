package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.dto.UserAdminUpdateDto;
import at.fhtw.ctfbackend.dto.UserDto;
import at.fhtw.ctfbackend.entity.UserEntity;
import at.fhtw.ctfbackend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserAdminUpdateDto updateDto) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity updated = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(toDto(updated));
    }

    private UserDto toDto(UserEntity entity) {
        return UserDto.builder()
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

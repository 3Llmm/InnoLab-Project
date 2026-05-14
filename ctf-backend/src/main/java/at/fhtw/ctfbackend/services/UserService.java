package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.dto.UserAdminUpdateDto;
import at.fhtw.ctfbackend.entity.UserEntity;
import at.fhtw.ctfbackend.repository.AdminUserRepository;
import at.fhtw.ctfbackend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final List<String> bootstrapAdminUsers;

    public UserService(
        UserRepository userRepository,
        AdminUserRepository adminUserRepository,
        @Value("${app.auth.admin-users:if24b241,if24b234}") List<
            String
        > bootstrapAdminUsers
    ) {
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.bootstrapAdminUsers = bootstrapAdminUsers
            .stream()
            .map(this::normalizeUsername)
            .toList();
    }

    public String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }

        String normalized = username.trim();

        if (normalized.startsWith("uid=") && normalized.contains(",")) {
            int uidStart = "uid=".length();
            int uidEnd = normalized.indexOf(',');
            if (uidEnd > uidStart) {
                normalized = normalized.substring(uidStart, uidEnd);
            }
        }

        int atIndex = normalized.indexOf('@');
        if (atIndex > 0) {
            normalized = normalized.substring(0, atIndex);
        }

        return normalized.toLowerCase(Locale.ROOT);
    }

    @Transactional
    public UserEntity ensureUserExistsForLogin(String rawUsername) {
        String username = normalizeUsername(rawUsername);
        boolean shouldBeAdmin = isBootstrapAdmin(username);

        UserEntity user = userRepository
            .findByUsername(username)
            .orElseGet(() ->
                userRepository.save(
                    UserEntity.builder()
                        .username(username)
                        .email(username + "@technikum-wien.at")
                        .displayName(username)
                        .isAdmin(shouldBeAdmin)
                        .isActive(true)
                        .build()
                )
            );

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.setEmail(user.getUsername() + "@technikum-wien.at");
        }
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(user.getUsername());
        }
        if (shouldBeAdmin && !Boolean.TRUE.equals(user.getIsAdmin())) {
            user.setIsAdmin(true);
        }

        return userRepository.save(user);
    }

    public boolean isBootstrapAdmin(String username) {
        String normalizedUsername = normalizeUsername(username);
        return bootstrapAdminUsers.contains(normalizedUsername) ||
        adminUserRepository.existsById(normalizedUsername);
    }

    @Transactional
    public UserEntity markSuccessfulLogin(UserEntity user) {
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Optional<UserEntity> findByUsername(String rawUsername) {
        return userRepository.findByUsername(normalizeUsername(rawUsername));
    }

    public UserEntity getRequiredUser(String rawUsername) {
        return findByUsername(rawUsername).orElseThrow(() ->
            new IllegalArgumentException("User not found: " + rawUsername)
        );
    }

    public List<UserEntity> getAllUsers() {
        return userRepository
            .findAll()
            .stream()
            .sorted((left, right) ->
                left.getUsername().compareToIgnoreCase(right.getUsername())
            )
            .toList();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public UserEntity updateUser(Long id, UserAdminUpdateDto updateDto) {
        UserEntity user = userRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found: " + id)
            );

        if (updateDto.getEmail() != null) {
            String email = updateDto.getEmail().trim();
            user.setEmail(email.isEmpty() ? null : email);
        }

        if (updateDto.getDisplayName() != null) {
            String displayName = updateDto.getDisplayName().trim();
            user.setDisplayName(
                displayName.isEmpty() ? user.getUsername() : displayName
            );
        }

        if (updateDto.getIsAdmin() != null) {
            user.setIsAdmin(updateDto.getIsAdmin());
        }

        if (updateDto.getIsActive() != null) {
            user.setIsActive(updateDto.getIsActive());
        }

        return userRepository.save(user);
    }
}

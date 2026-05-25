package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.controller.AdminStateConflictException;
import at.fhtw.ctfbackend.dto.UserAdminUpdateDto;
import at.fhtw.ctfbackend.entity.UserEntity;
import at.fhtw.ctfbackend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminSafetyTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, List.of("admin-bootstrap"));
    }

    @Test
    void selfDemotionThrowsAdminStateConflictException() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .username("admin-user")
                .isAdmin(true)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isAdmin(false)
                .build();

        AdminStateConflictException ex = assertThrows(
                AdminStateConflictException.class,
                () -> userService.updateUser(1L, dto, "admin-user")
        );
        assertEquals("Cannot demote yourself", ex.getMessage());
    }

    @Test
    void selfDeactivationThrowsAdminStateConflictException() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .username("admin-user")
                .isAdmin(true)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isActive(false)
                .build();

        AdminStateConflictException ex = assertThrows(
                AdminStateConflictException.class,
                () -> userService.updateUser(1L, dto, "admin-user")
        );
        assertEquals("Cannot deactivate yourself", ex.getMessage());
    }

    @Test
    void lastActiveAdminCannotBeDemoted() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .username("admin-user")
                .isAdmin(true)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByIsAdminTrueAndIsActiveTrue()).thenReturn(1L);

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isAdmin(false)
                .build();

        AdminStateConflictException ex = assertThrows(
                AdminStateConflictException.class,
                () -> userService.updateUser(1L, dto, "other-admin")
        );
        assertEquals("Cannot remove the last active administrator", ex.getMessage());
    }

    @Test
    void lastActiveAdminCannotBeDeactivated() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .username("admin-user")
                .isAdmin(true)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByIsAdminTrueAndIsActiveTrue()).thenReturn(1L);

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isActive(false)
                .build();

        AdminStateConflictException ex = assertThrows(
                AdminStateConflictException.class,
                () -> userService.updateUser(1L, dto, "other-admin")
        );
        assertEquals("Cannot remove the last active administrator", ex.getMessage());
    }

    @Test
    void nonLastAdminCanBeDemotedByAnotherAdmin() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .username("admin-user")
                .isAdmin(true)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByIsAdminTrueAndIsActiveTrue()).thenReturn(2L);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isAdmin(false)
                .build();

        UserEntity updated = userService.updateUser(1L, dto, "other-admin");
        assertFalse(updated.getIsAdmin());
    }

    @Test
    void sameUsernameDifferentCaseIsStillDetectedAsSelfEdit() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .username("AdminUser")
                .isAdmin(true)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isAdmin(false)
                .build();

        AdminStateConflictException ex = assertThrows(
                AdminStateConflictException.class,
                () -> userService.updateUser(1L, dto, "adminuser")
        );
        assertEquals("Cannot demote yourself", ex.getMessage());
    }

    @Test
    void userNotFoundThrowsIllegalArgumentException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserAdminUpdateDto dto = UserAdminUpdateDto.builder()
                .isAdmin(true)
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(99L, dto, "acting-admin")
        );
    }
}

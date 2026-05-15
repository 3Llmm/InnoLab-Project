package at.fhtw.ctfbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminUpdateDto {
    private String email;
    private String displayName;
    private Boolean isAdmin;
    private Boolean isActive;
}

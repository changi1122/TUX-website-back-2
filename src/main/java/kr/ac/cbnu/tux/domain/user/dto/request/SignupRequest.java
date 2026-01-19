package kr.ac.cbnu.tux.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequest {

    @Schema(description = "Username must be at least 4 characters long and contain only letters, numbers, or underscores.")
    @NotEmpty @Size(min = 4)
    @Pattern(regexp = "[a-zA-Z\\d_]+", message = "Username Rule : only alphabet + number + _")
    private String username;

    @Schema(description = "Nickname must not be empty.")
    @NotEmpty
    private String nickname;

    @Schema(description = "Password must not be empty.")
    @NotEmpty
    private String password;

    @Schema(description = "User's email address, must be a valid email format")
    @NotEmpty @Pattern(regexp = ".+@.+", message = "Invalid email address")
    private String email;

    private String department;
    private String studentNumber;
    private String phoneNumber;

    public User toEntity() {
        return User.builder()
                .username(username)
                .nickname(nickname)
                .email(email)
                .department(department)
                .studentNumber(studentNumber)
                .phoneNumber(phoneNumber)
                .build();
    }
}

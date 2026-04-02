package kr.ac.cbnu.tux.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataRequest {

    private String nickname;
    private String password;

    @Pattern(regexp = ".+@.+", message = "Invalid email address")
    private String email;

    private String department;
    private String studentNumber;
    private String phoneNumber;

}

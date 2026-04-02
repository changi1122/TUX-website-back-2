package kr.ac.cbnu.tux.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserDataRequest {

    private String nickname;
    private String password;

    @Pattern(regexp = ".+@.+", message = "Invalid email address")
    private String email;

    private String department;
    private String studentNumber;
    private String phoneNumber;

}

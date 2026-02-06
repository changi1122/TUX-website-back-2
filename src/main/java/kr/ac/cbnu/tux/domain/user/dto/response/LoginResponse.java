package kr.ac.cbnu.tux.domain.user.dto.response;

import kr.ac.cbnu.tux.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    UserResponse user;
    Token accessToken;
    Token refreshToken;

    public static LoginResponse of(User user, Token accessToken, Token refreshToken) {
        return LoginResponse.builder()
                .user(UserResponse.of(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

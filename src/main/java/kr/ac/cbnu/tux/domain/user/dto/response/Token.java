package kr.ac.cbnu.tux.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {

    private String token;
    private Long expiresIn;

    public static Token of(String token, long expiresIn) {
        return Token.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }
}

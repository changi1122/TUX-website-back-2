package kr.ac.cbnu.tux.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDTO {

    private String token;
    private Long expiresIn;

    public static TokenDTO build(String token, long expiresIn) {
        return TokenDTO.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }
}

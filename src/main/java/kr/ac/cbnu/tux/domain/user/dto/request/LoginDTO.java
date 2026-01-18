package kr.ac.cbnu.tux.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    private String username;
    private String password;
}

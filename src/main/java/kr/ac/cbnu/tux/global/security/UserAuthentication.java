package kr.ac.cbnu.tux.global.security;

import kr.ac.cbnu.tux.domain.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserAuthentication extends UsernamePasswordAuthenticationToken {

    public UserAuthentication(User principal, String credentials) {
        super(principal, credentials);
    }

    public UserAuthentication(User principal, String credentials,
                              Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}

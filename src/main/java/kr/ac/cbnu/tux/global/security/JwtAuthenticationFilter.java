package kr.ac.cbnu.tux.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.cbnu.tux.domain.user.dto.response.Token;
import kr.ac.cbnu.tux.domain.user.entity.RefreshToken;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.repository.RefreshTokenRepository;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.global.utility.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = CookieUtils.getCookieValue(request, CookieUtils.ACCESS_TOKEN_KEY);
            String refreshToken = CookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_KEY);

            // 1. 액세스 토큰이 유효하면 인증 처리
            if (jwtTokenProvider.validateToken(accessToken)) {
                authenticateUser(accessToken, request);
            }

            // 2. 액세스 토큰 만료 -> 리프레시 토큰 존재시 재발급 시도
            else if (jwtTokenProvider.isTokenExpired(accessToken) && refreshToken != null) {
                boolean refreshed = tryRefreshAccessToken(refreshToken, request, response);
                if (!refreshed) {
                    request.setAttribute("unauthorization", "401 Refresh token invalid");
                }
            }

            // 3. 두 토큰 모두 없거나 유효하지 않음
            else {
                request.setAttribute("unauthorization", "401 No token or token invalid");
            }
        } catch (Exception ignored) { }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtTokenProvider.getUsernameFromJwt(token);

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not present"));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean tryRefreshAccessToken(String refreshToken,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        // 리프레시 토큰 JWT 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return false;
        }

        // DB 리프레시 토큰 확인
        Optional<RefreshToken> storedToken = refreshTokenRepository.findByToken(refreshToken);
        if (storedToken.isEmpty() || storedToken.get().isExpired()) {
            return false;
        }

        // 새 액세스 토큰 발급
        User user = userRepository.findUserByUsername(storedToken.get().getUsername()).orElseThrow();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
        Token newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        // 응답 쿠키에 새 액세스 토큰 설정
        response.addCookie(CookieUtils.createAccessTokenCookie(newAccessToken.getToken()));

        // 인증 처리
        authenticateUser(newAccessToken.getToken(), request);
        return true;
    }
}

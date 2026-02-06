package kr.ac.cbnu.tux.global.utility;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtils {

    public static final String ACCESS_TOKEN_KEY = "accessToken";
    public static final String REFRESH_TOKEN_KEY = "refreshToken";

    private static final int ACCESS_TOKEN_AGE = 30 * 60; // 30분
    private static final int REFRESH_TOKEN_AGE = 168 * 60 * 60; // 7일

    // 액세스 토큰 쿠키 생성
    public static Cookie createAccessTokenCookie(String token) {
        return createCookie(ACCESS_TOKEN_KEY, token, ACCESS_TOKEN_AGE);
    }

    // 리프레시 토큰 쿠키 생성
    public static Cookie createRefreshTokenCookie(String token) {
        return createCookie(REFRESH_TOKEN_KEY, token, REFRESH_TOKEN_AGE);
    }

    // 액세스 토큰 쿠키 삭제
    public static Cookie deleteAccessTokenCookie() {
        return createCookie(ACCESS_TOKEN_KEY, null, 0);
    }

    // 리프레시 토큰 쿠키 삭제
    public static Cookie deleteRefreshTokenCookie() {
        return createCookie(REFRESH_TOKEN_KEY, null, 0);
    }

    // 요청에서 쿠키 값 추출
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // HTTPS에서만 전송
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        return cookie;
    }
}

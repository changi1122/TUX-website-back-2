package kr.ac.cbnu.tux.utility;

import jakarta.servlet.http.Cookie;

public class CookieUtils {

    public static final int TOKEN_AGE = 168 * 60 * 60; // 7일

    public static Cookie createTokenCookie(String token, int age) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(age);
        cookie.setPath("/");
        return cookie;
    }
}

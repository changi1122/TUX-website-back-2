package kr.ac.cbnu.tux.domain.user.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.dto.request.UserDataRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.LoginResponse;
import kr.ac.cbnu.tux.domain.user.dto.response.Token;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.entity.RefreshToken;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.exception.UserErrorCode;
import kr.ac.cbnu.tux.domain.user.exception.UserException;
import kr.ac.cbnu.tux.domain.user.repository.RefreshTokenRepository;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private static final String PASSWORD_RULE = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*#?&]{8,}$";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void createUser(SignupRequest request, OffsetDateTime now) {

        if (userRepository.existsByUsername(request.getUsername()) ||
            "anonymousUser".equals(request.getUsername())) {
            throw new UserException(UserErrorCode.USERNAME_NOT_UNIQUE);
        }

        if (!Pattern.matches(PASSWORD_RULE, request.getPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_RULE_NOT_MATCHED);
        }

        User createdUser = request.toEntity();
        createdUser.initializeUser(now);
        createdUser.updatePassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(createdUser);
    }

    @Transactional
    public void updateUser(Long id, UserDataRequest request) {

        User user = userRepository.findById(id).orElseThrow();
        user.updateUserData(request);

        if (request.getPassword() != null) {
            if (!Pattern.matches(PASSWORD_RULE, request.getPassword())) {
                throw new UserException(UserErrorCode.PASSWORD_RULE_NOT_MATCHED);
            }

            user.updatePassword(passwordEncoder.encode(request.getPassword()));
            refreshTokenRepository.deleteByUsername(user.getUsername());
        }
    }

    @Transactional
    public void deleteUserSoftly(Long id, OffsetDateTime now) {
        User user = userRepository.findById(id).orElseThrow();
        user.deleteUserData(now);
        refreshTokenRepository.deleteByUsername(user.getUsername());
    }

    @Transactional
    public LoginResponse tryLogin(LoginRequest loginRequest) {
        User user = userRepository.findUserByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserException(UserErrorCode.LOGIN_FAILED));

        if (user.isBanned() || user.isLocked() || user.isDeleted())
            throw new UserException(UserErrorCode.LOGIN_FAILED);

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );

            // 액세스 토큰 생성
            Token accessToken = jwtTokenProvider.generateAccessToken(authentication);

            // 리프레시 토큰 생성
            Token refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // 리프레시 토큰 저장
            refreshTokenRepository.save(RefreshToken.builder()
                    .token(refreshToken.getToken())
                    .username(user.getUsername())
                    .expiryDate(OffsetDateTime.now().plusSeconds(
                            jwtTokenProvider.getRefreshExpirationMs() / 1000
                    ))
                    .build());

            return LoginResponse.of(user, accessToken, refreshToken);
        } else {
            throw new UserException(UserErrorCode.LOGIN_FAILED);
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    public void deleteUserHardly(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        refreshTokenRepository.deleteByUsername(user.getUsername());
        userRepository.delete(user);
    }

    @Transactional
    public void ban(Long id, OffsetDateTime now) {
        User user = userRepository.findById(id).orElseThrow();
        user.ban(now);
        refreshTokenRepository.deleteByUsername(user.getUsername());
    }

    @Transactional
    public void changeUserRole(Long id, UserRole role) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(role);
    }

    @Transactional
    public void setTemporalPassword(Long id, String password) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(password));
    }

    public User readUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User readByUsername(String username) {
        return userRepository.findUserByUsername(username).orElseThrow();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not present"));
    }

    public boolean canUseAsUsername(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean canUseAsNickname(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    public List<User> listAllWaitingUser() {
        return userRepository.findByIsDeletedFalseAndRole(UserRole.GUEST);
    }

    public List<User> listAllUserNotGuest() {
        return userRepository.findByIsDeletedFalseAndRoleNot(UserRole.GUEST);
    }
}

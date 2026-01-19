package kr.ac.cbnu.tux.domain.user.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.user.dto.request.SignupRequest;
import kr.ac.cbnu.tux.domain.user.dto.request.UserDataRequest;
import kr.ac.cbnu.tux.domain.user.entity.User;
import kr.ac.cbnu.tux.domain.user.dto.request.LoginRequest;
import kr.ac.cbnu.tux.domain.user.dto.response.UserResponse;
import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.domain.user.repository.UserRepository;
import kr.ac.cbnu.tux.security.JwtTokenProvider;
import kr.ac.cbnu.tux.security.UserAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private static final String PASSWORD_RULE = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*#?&]{8,}$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void createUser(SignupRequest request, OffsetDateTime now) {

        if (userRepository.existsByUsername(request.getUsername()) ||
            "anonymousUser".equals(request.getUsername())) {
            throw new RuntimeException("username is not unique");
        }

        if (!Pattern.matches(PASSWORD_RULE, request.getPassword())) {
            throw new RuntimeException("password rule not matched");
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
                throw new RuntimeException("password rule not matched");
            }

            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteUserSoftly(Long id, OffsetDateTime now) {
        User user = userRepository.findById(id).orElseThrow();
        user.deleteUserData(now);
        userRepository.save(user);
    }

    public void hardDelete(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
    }

    @Transactional
    public void ban(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBanned(true);
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

    public UserResponse tryLogin(LoginRequest loginRequest) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("user not present"));

        if (user.isBanned() || user.isLocked() || user.isDeleted())
            throw new UsernameNotFoundException("user not present");

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            Authentication authentication = new UserAuthentication(
                    user, null, user.getAuthorities()
            );
            return UserResponse.build(
                    user,
                    JwtTokenProvider.generateToken(authentication)
            );
        } else {
            throw new IllegalArgumentException("password not matched");
        }
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

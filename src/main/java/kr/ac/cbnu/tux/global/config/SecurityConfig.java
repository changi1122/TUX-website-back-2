package kr.ac.cbnu.tux.global.config;

import kr.ac.cbnu.tux.domain.user.enums.UserRole;
import kr.ac.cbnu.tux.global.security.JwtAuthenticationEntryPoint;
import kr.ac.cbnu.tux.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> {
                            /* Community */
                    auth.requestMatchers(HttpMethod.POST, "/api/community").authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/community/**").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/community/**").authenticated()

                            /* ReferenceRoom */
                            .requestMatchers(HttpMethod.GET, "/api/referenceroom/{id}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/referenceroom/{id}/file/{filename}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/referenceroom/**").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/referenceroom").authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/referenceroom/**").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/referenceroom/**").authenticated()

                            /* User */
                            .requestMatchers(HttpMethod.GET, "/api/auth").authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/user/**").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/user/**").authenticated()
                            .requestMatchers("/api/admin/**").hasAuthority(UserRole.ADMIN.name())

                            .anyRequest().permitAll();
                })
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

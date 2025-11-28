package lceye.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        // 1. HTTP 요청에 따른 권한 커스텀
        httpSecurity.authorizeHttpRequests(auth -> auth
                // 권한 목록 : ADMIN, MANAGER, WORKER
                .requestMatchers("/api/lci/**").hasAnyRole("ADMIN", "MANAGER", "WORKER")
                .requestMatchers("/api/inout/**").hasAnyRole("ADMIN", "MANAGER", "WORKER")
                .requestMatchers("/api/project/manager").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/project/**").hasAnyRole("ADMIN", "MANAGER", "WORKER")
                .requestMatchers("/api/units/**").hasAnyRole("ADMIN", "MANAGER", "WORKER")
                // 일단 모든 요청에 대한 권한 허용
                .requestMatchers("/**").permitAll()
        );
        // 2. 차단된 csrf 차단 해체
        httpSecurity.csrf(csrf -> csrf.disable());

        // 3. 시큐리티 세션 기반 토큰을 필요시 자동 생성
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        // 4. 내가 만든 토큰으로 교체
        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 5. 내가 만든 CorsConfig 설정
        httpSecurity.cors(Customizer.withDefaults());
        // 6. 커스텀 완료된 객체 반환
        return httpSecurity.build();
    } // func end
} // class end
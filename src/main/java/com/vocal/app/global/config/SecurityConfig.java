package com.vocal.app.global.config;

import com.vocal.app.global.security.*;
import com.vocal.app.global.security.JwtAuthenticationFilter;
import com.vocal.app.global.security.JwtTokenProvider;
import com.vocal.app.global.security.UserDetailsServiceImpl;
import com.vocal.app.global.security.oauth.CustomOAuth2UserService;
import com.vocal.app.global.security.oauth.OAuthLoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;


    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        // 인증 안 된 상태로 보호된 API를 호출하면, 카카오 로그인 페이지로
                        // 리다이렉트하는 대신 그냥 401을 돌려준다. 프론트가 fetch로 이런
                        // API를 부를 때, 브라우저가 리다이렉트를 따라가다가
                        // accounts.kakao.com에서 CORS 에러로 막히는 문제를 막기 위함
                        // (로그인 페이지 리다이렉트는 <a href="..."> 같은 진짜 브라우저
                        // 네비게이션에만 맞는 방식이지 fetch/AJAX에는 안 맞음).
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // bulk-upsert(관리자용 데이터 쓰기)는 계속 인증 필요 — 아래 /chart/** 공개
                        // 규칙보다 먼저 와야 함(더 구체적인 규칙이 먼저 매칭되어야 함).
                        .requestMatchers("/chart/vocal-ranges/bulk-upsert").authenticated()
                        // 분석 워커 전용(대기 목록 조회/실패 보고)은 로그인 필요. 요청/상태 조회는 공개.
                        .requestMatchers("/chart/analysis-requests/pending", "/chart/analysis-requests/fail", "/chart/analysis-requests/mine").authenticated()
                        // 차트 목록/음역대/아티스트 성별 조회는 로그인 없이도 보이는 공개
                        // 데이터라서 허용 (GET으로 조회하는 것도 있고, 곡 목록을 body에
                        // 담아 보내는 배치조회라 POST로 호출하는 것도 있어서 메서드 제한 없이 허용).
                        .requestMatchers("/chart/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")   // 추가
                        .anyRequest().authenticated()).addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class).oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuthLoginSuccessHandler)
                );
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://fitch-fe.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
package com.example.gromit.config;

import com.example.gromit.jwt.JwtAuthenticationFilter;
import com.example.gromit.jwt.JwtExceptionFilter;
import com.example.gromit.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtService jwtService;
    private final JwtExceptionFilter jwtExceptionFilter;


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .cors().configurationSource(corsConfigurationSource()) // 크로스 오리진 정책 안씀 (인증 X) , 시큐리티 필터에 등록 인증 O
                .and()
                // 세션을 사용하지 않기 때문에 STATELESS 로 설정
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManager(),jwtService))
                .authorizeRequests()
                .antMatchers("/login/apple").permitAll()
                .antMatchers("/users/**").permitAll()
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()
                .and()
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", getDefaultCorsConfiguration());

        return source;
    }

    private CorsConfiguration getDefaultCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:8081", "https://gromit.shop",
                        "https://appleid.apple.com"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 header 에 응답을 허용
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 get,post,patch,put,delete 요청 허용
        configuration.setAllowedOrigins(Arrays.asList("*")); // 모든 ip 응답을 허용
        configuration.setAllowCredentials(true); // 내 서버가 응답할 때 json 을 자바스크립트에서 처리할 수 있게 할지를 설정하는 것
        configuration.setMaxAge(3600L);

        return configuration;
    }
}


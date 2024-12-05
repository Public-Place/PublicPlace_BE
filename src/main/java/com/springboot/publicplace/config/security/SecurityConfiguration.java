package com.springboot.publicplace.config.security;

import com.springboot.publicplace.handler.CustomAccessDeniedHandler;
import com.springboot.publicplace.handler.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfiguration(JwtTokenProvider jwtTokenProvider){

        this.jwtTokenProvider = jwtTokenProvider;
    }
    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors()
                .and()
                .httpBasic().disable() // REST API는 UI를 사용하지 않으므로 기본설정을 비활성화

                .csrf().disable() // REST API는 csrf 보안이 필요 없으므로 비활성화

                .sessionManagement()
                .sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS) // JWT Token 인증방식으로 세션은 필요 없으므로 비활성화

                .and()
                .authorizeRequests() // 리퀘스트에 대한 사용권한 체크
                // React 빌드 파일과 정적 리소스에 대한 접근 허용
                .antMatchers("/", "/index.html", "/static/**", "/js/**", "/css/**", "/images/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS,"/**").permitAll() // 프리플라이트 요청를 허용
                .antMatchers("/sign-api/sign-in", "/sign-api/sign-up",("/kakao/callback"),
                        "/sign-api/exception", "/api/files/**","/api/v1/chat/**","/auth/refresh",
                        "/api/v1/feedback").permitAll() // 가입 및 로그인 주소는 허용
                .antMatchers(HttpMethod.GET, "/**").permitAll()

                .antMatchers("**exception**").permitAll()

                .anyRequest().hasRole("ADMIN") // 나머지 요청은 인증된 ADMIN만 접근 가능

                .and()
                .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())

                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class); // JWT Token 필터를 id/password 인증 필터 이전에 추가
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/v2/api-docs",
                "/index.html",
                "/swagger-resources/**",
                "/swagger-ui.html",
                "/webjars/**",
                "/swagger/**",
                "/sign-api/exception",
                "/kakao/**",
                "/kakao_login_large_narrow.png");
    }
}

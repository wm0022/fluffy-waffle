package com.shengwei.tushuguanli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Security 配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/auth/login", "/auth/register").permitAll()
            .antMatchers("/uploads/**", "/druid/**").permitAll()
            .antMatchers(HttpMethod.GET, "/book/**").permitAll()
            .antMatchers("/sysUser/page", "/sysUser/list", "/sysUser/create").hasAuthority("/admin/member")
            .antMatchers(HttpMethod.DELETE, "/sysUser/**").hasAuthority("/admin/member")
            .antMatchers("/role/**", "/menu/**").hasAuthority("/admin/role")
            .antMatchers("/inventory/**").hasAuthority("/admin/inventory")
            .antMatchers("/donor/**", "/donation/person/**").hasAuthority("/admin/donor-manage")
            .antMatchers("/donation/all", "/donation/review", "/donation/update").hasAuthority("/admin/donation-manage")
            .antMatchers("/donation-apply/**", "/donation-accept/**").hasAuthority("/admin/donation-manage")
            .antMatchers("/book-review/page", "/book-review/*/audit", "/book-review/*/reply", "/book-review/*/toggle-visibility", "/book-review/fix-book-id").hasAuthority("/admin/review")
            .antMatchers("/order/all").hasAuthority("/admin/order")
            .antMatchers("/order/refund/handle").hasAuthority("/admin/order")
            .anyRequest().authenticated();

        http.exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

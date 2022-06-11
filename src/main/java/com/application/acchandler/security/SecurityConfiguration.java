package com.application.acchandler.security;

import com.application.acchandler.security.auth.*;
import com.application.acchandler.security.filters.CurrentUserFilter;
import com.application.acchandler.security.filters.CustomExceptionHandlerFilter;
import com.application.acchandler.security.filters.GoogleOneTapAuthFilter;
import com.application.acchandler.security.filters.ResponseHandlerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final AppUserDetailsService userDetailsService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final AuthSuccessHandler authSuccessHandler;
    private final AuthFailureHandler authFailureHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private CurrentUserFilter currentUserFilter;
    private GoogleOneTapAuthFilter googleOneTapAuthFilter;
    private ResponseHandlerFilter responseHandlerFilter;
    private CustomExceptionHandlerFilter customExceptionHandlerFilter;
    private CustomAuthEntryPoint authEntryPoint;

    @Autowired
    public SecurityConfiguration(
            PasswordEncoder passwordEncoder,
            AppUserDetailsService userDetailsService,
            AuthSuccessHandler authSuccessHandler,
            AuthFailureHandler authFailureHandler,
            LogoutSuccessHandler logoutSuccessHandler,
            CurrentUserFilter currentUserFilter,
            GoogleOneTapAuthFilter googleOneTapAuthFilter,
            ResponseHandlerFilter responseHandlerFilter,
            CustomExceptionHandlerFilter customExceptionHandlerFilter,
            CustomAuthEntryPoint authEntryPoint) {
        System.out.println("SecurityConfiguration Constructor Initialized");
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.authFailureHandler = authFailureHandler;
        this.authSuccessHandler = authSuccessHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.currentUserFilter = currentUserFilter;
        this.googleOneTapAuthFilter = googleOneTapAuthFilter;
        this.responseHandlerFilter = responseHandlerFilter;
        this.customExceptionHandlerFilter = customExceptionHandlerFilter;
        this.authEntryPoint = authEntryPoint;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth){
        auth.authenticationProvider(getDaoAuthenticationProvider());
    }

    private DaoAuthenticationProvider getDaoAuthenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://172.17.0.1:8000", "http://192.168.49.2:30000"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PUT","OPTIONS","PATCH", "DELETE"));

        http
                .addFilterBefore(googleOneTapAuthFilter, OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(currentUserFilter, OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(responseHandlerFilter, CurrentUserFilter.class)
                .addFilterBefore(customExceptionHandlerFilter, GoogleOneTapAuthFilter.class)

                .csrf().disable()
                .cors()
                    .configurationSource(corsConfigurationSource())
                .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
                    .antMatchers(HttpMethod.POST, "/send-otp", "/verify-otp", "/login/oauth2/**")
                        .permitAll()
                    .anyRequest()
                    .authenticated()
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .formLogin()
                    .loginPage("http://localhost:3000/login")
                    .loginProcessingUrl("/verify-otp")
                    .usernameParameter("phoneNumber")
                    .passwordParameter("otp")
                        .successHandler(this.authSuccessHandler)
                        .failureHandler(this.authFailureHandler)
                .and()
                .oauth2Login()
                    .permitAll()
                    .defaultSuccessUrl("http://localhost:3000/myinfo")
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(this.logoutSuccessHandler)
                .and()
                .sessionManagement()
                    .maximumSessions(2)
                    .sessionRegistry(sessionRegistry()).and()
                .and()
                .httpBasic();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}

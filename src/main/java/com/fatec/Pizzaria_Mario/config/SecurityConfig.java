package com.fatec.Pizzaria_Mario.config;

import com.fatec.Pizzaria_Mario.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider, CustomUserDetailsService customUserDetailsService) throws Exception {
        http
            .authenticationProvider(daoAuthenticationProvider)
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers( // Rotas públicas
                            "/", "/login", "/registrar", "/esqueci-senha", "/redefinir-senha",
                            "/esqueci-senha-submit", "/redefinir-senha-submit",
                            "/css/**", "/js/**", "/images/**", "/webjars/**",
                            "/cardapio", // Cardápio público
                            "/pedido/montar/**" // Montagem de pizza pública
                    ).permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN") // Rotas de admin
                    .anyRequest().authenticated() // Outras rotas (carrinho, checkout, etc.) exigem autenticação
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/cardapio", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            .rememberMe(rememberMe ->
                rememberMe
                    .key("PizzariaMarioMuitoSecretKeyUltraSecreta") // Mude esta chave!
                    .tokenValiditySeconds(86400) // 1 dia
                    .userDetailsService(customUserDetailsService)
            );
        return http.build();
    }
}

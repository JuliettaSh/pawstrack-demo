package com.example.demoPT.Configuraciones;

import com.example.demoPT.Modelo.ImplUsuarioDetails;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import jakarta.servlet.Filter;
import java.util.Collections;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad implements WebMvcConfigurer {

    private final RepositorioUsuario userRepository;
    // Inyección de dependencias correcta
    public ConfiguracionSeguridad(RepositorioUsuario userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/register/mfa", "/register/mfa/verify").authenticated() // Requiere auth
                .requestMatchers("/login/mfa-verify").authenticated()
                .requestMatchers("/seguimiento").authenticated()
                .requestMatchers("/seguimiento/editar/**").authenticated()
                .requestMatchers("/seguimiento/actualizar/**").authenticated()
                .requestMatchers("/seguimiento/iniciar/**").authenticated() 
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                .successHandler(mfaAuthenticationSuccessHandler()) // Manejador clave
                .failureUrl("/login?error=true")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                )
                .rememberMe(remember -> remember
                .key("uniqueAndSecretKey") // Debe ser único y secreto para tu aplicación
                .tokenValiditySeconds(86400) // 1 día
                .userDetailsService(userDetailsService()) // Asegúrate de inyectar tu UserDetailsService
                .rememberMeParameter("remember-me") // Nombre del parámetro en el formulario
                .rememberMeCookieName("remember-me-cookie")
                )
                .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired")
                .maximumSessions(1)
                .expiredUrl("/login?expired")
                );

//                .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                );
        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("classpath:/static/imagenes/")
                .addResourceLocations("file:./public/imagenes/") // Para desarrollo
                .setCachePeriod(3600);
    }

    @Bean
    public AuthenticationSuccessHandler mfaAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            Usuario usuario = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            if (usuario.isMfaEnabled()) {
                // Si tiene MFA activado, pedir verificación
                response.sendRedirect("/login/mfa-verify?username=" + username);
            } else if (usuario.getMfaSecret() == null) {
                // Si no tiene MFA configurado, redirigir a configuración
                response.sendRedirect("/register/mfa?username=" + username);
            } else {
                // Si no necesita MFA, ir al home
                response.sendRedirect("/home");
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Bean para el UserDetailsService
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(usuario -> {
                    if (!usuario.isEnabled()) {
                        throw new DisabledException("Usuario no está habilitado");
                    }
                    return new ImplUsuarioDetails(usuario);
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}

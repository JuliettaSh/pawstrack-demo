package com.example.demoPT.Configuraciones;

import com.example.demoPT.Modelo.ImplUsuarioDetails;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    //encripta las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/perfil/**").permitAll()
                .requestMatchers("/publicaciones/crear").authenticated() // Requiere autenticación antes de entrar a ese apartado
                .requestMatchers("/publicaciones/editar").authenticated() // Requiere autenticación
                .requestMatchers("/publicaciones/borrar").authenticated() // Requiere autenticación
                .requestMatchers("/seguimiento").authenticated() // Requiere autenticación
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login") // Página de inicio de sesión
                .defaultSuccessUrl("/home", true) // Página a la que se redirige al iniciar sesión correctamente
                .failureUrl("/login?error=true") // Redirige a login con parámetro error si falla la autenticación
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")//cuando se cierra la sesion te redirege al login de nuevo
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }

    //buscamos al usuario logueado con el nombre
    @Bean
    public UserDetailsService userDetailsService(RepositorioUsuario userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(ImplUsuarioDetails::new) // Convertimos User a UserDetailsImpl
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

}

package com.example.demo.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import com.example.demo.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(authorize -> authorize
                        // Rutas públicas
                        .requestMatchers("/auth/**", "/inicio", "/registro", "/login", "/noticias", "/foros", "/torneos", "/juegos", "/torneos/{id}", "/css/**", "/js/**", "/images/**", "/", "/terminos", "/politica-privacidad", "/contacto", "/reset-password", "/forgot-password", "/forgot-password/**", "/juegos/**","/error", "/403", "/404").permitAll()
                        // Rutas específicas que requieren autenticación
                        .requestMatchers("/juegos/agregar", "/foros/crear", "/torneos/crear", "/aceptarSolicitud", "/rechazarSolicitud", "/biblioteca/eliminar/**", "/eliminarAmigo/**", "/perfil/editar", "/perfil/guardar").authenticated()
                        // Rutas públicas generales (menos específicas)
                        .requestMatchers("/torneos/*","/foros/*","/juegos/*").permitAll()
                    // Rutas de administrador
                    .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                    // Cualquier otra solicitud requiere autenticación
                    .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth/**", "/registro", "/login", "/biblioteca/eliminar/**", "/eliminarAmigo/**")
                )

                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(new CustomAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler())
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/403");
                        })
                );

        return http.build();
    }
}
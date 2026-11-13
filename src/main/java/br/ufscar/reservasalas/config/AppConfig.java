package br.ufscar.reservasalas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final AutorizacaoInterceptor autorizacaoInterceptor;

    public AppConfig(AutorizacaoInterceptor autorizacaoInterceptor) {
        this.autorizacaoInterceptor = autorizacaoInterceptor;
    }

    /** RNF008: hash BCrypt para as senhas. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Relogio injetavel, para permitir testes das regras de data/hora. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacaoInterceptor)
                .addPathPatterns("/salas/**", "/reservas/**", "/minhas-reservas/**", "/admin/**");
    }
}

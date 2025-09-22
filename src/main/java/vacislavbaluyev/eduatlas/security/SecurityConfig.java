package vacislavbaluyev.eduatlas.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(cors -> {});
        //disabilito il form di login
        httpSecurity.formLogin(formLogin -> formLogin.disable());
        //disabilito la protezione da CSRF
        httpSecurity.csrf(csrf -> csrf.disable());
        //disabilito le sessioni
        httpSecurity.sessionManagement(sessions ->
                sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


// - personalizzo il comportamento di funzionalità pre-esistenti
        httpSecurity.authorizeHttpRequests(h ->
                h.requestMatchers("/**").permitAll());


        return httpSecurity.build();

    }



    @Bean
    public PasswordEncoder getBCrypt(){ return new BCryptPasswordEncoder(16);}
}




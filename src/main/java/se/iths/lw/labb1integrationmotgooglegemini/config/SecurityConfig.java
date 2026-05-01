package se.iths.lw.labb1integrationmotgooglegemini.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.repository.AppUserRepository;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final AppUserRepository appUserRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(authorizeRequests ->authorizeRequests
                        .requestMatchers("/","/style.css").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .oidcUserService(oidcUserRequest->{
                                    OidcUser oidcUser=
                                            new org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService()
                                                    .loadUser(oidcUserRequest);

                                    String googleId = oidcUser.getSubject();
                                    String email = oidcUser.getEmail();
                                    String avatar = oidcUser.getPicture();
                                    String name= oidcUser.getFullName();

                                    appUserRepository.findByGoogleId(googleId)
                                            .orElseGet(()->{
                                                AppUser appUser= new AppUser();
                                                appUser.setGoogleId(googleId);
                                                appUser.setEmail(email);
                                                appUser.setAvatarUrl(avatar);
                                                appUser.setCreatedAt(LocalDateTime.now());
                                                return appUserRepository.save(appUser);
                                            });
                                    return oidcUser;
                                        })
                        )
                )
                .logout(logout ->logout.logoutSuccessUrl("/"))
                .csrf(csrf->csrf.disable());

        return httpSecurity.build();

    }
}

package se.iths.lw.labb1integrationmotgooglegemini.service;


import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.repository.AppUserRepository;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{

    private final AppUserRepository appUserRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String googleId= oAuth2User.getAttribute("sub");
        String avatarUrl = oAuth2User.getAttribute("picture");

        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseGet(()->{
                        AppUser newUser= AppUser.builder()
                                .googleId(googleId)
                                .email(email)
                                .avatarUrl(avatarUrl)
                                .createdAt(LocalDateTime.now())
                                .build();
                        return appUserRepository.save(newUser);
                });

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes(),
                "email"
        );
    }


}

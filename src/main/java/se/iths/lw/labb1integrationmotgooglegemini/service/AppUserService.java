package se.iths.lw.labb1integrationmotgooglegemini.service;


import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.repository.AppUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;


@AllArgsConstructor
@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUser findByEmail(String email) {
        return appUserRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("The user does not exist."));
    }

    public AppUser save(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    public AppUser loadOrCreateUser(String googleId, String email, String avatarUrl, String refreshToken) {

        // 1. Search app user by GoogleId
        Optional<AppUser> byGoogleId = appUserRepository.findByGoogleId(googleId);
        if (byGoogleId.isPresent()) {
            return byGoogleId.get();
        }

        // 2. Search app user by Email to prevent user duplication.
        Optional<AppUser> byEmail = appUserRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            AppUser existing = byEmail.get();

            // if email exists but googleId is null( Use Google to log in for the first time.)
            if (existing.getGoogleId() == null) {
                existing.setGoogleId(googleId);
            }


            // Update avatars ( it might be updated when user logs in.)
            if (avatarUrl != null) {
                existing.setAvatarUrl(avatarUrl);
            }


            // Update refreshToken if necessary in the future
            if (refreshToken != null) {
                existing.setRefreshToken(refreshToken);
            }

            return appUserRepository.save(existing);
        }


        //. Neither exists-> create a new app user.
        AppUser newUser = new AppUser();
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setRefreshToken(refreshToken);
        newUser.setCreatedAt(LocalDateTime.now());

        return appUserRepository.save(newUser);
    }



}

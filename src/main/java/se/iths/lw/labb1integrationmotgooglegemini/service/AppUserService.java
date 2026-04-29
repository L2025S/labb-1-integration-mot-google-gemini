package se.iths.lw.labb1integrationmotgooglegemini.service;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.repository.AppUserRepository;



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


}

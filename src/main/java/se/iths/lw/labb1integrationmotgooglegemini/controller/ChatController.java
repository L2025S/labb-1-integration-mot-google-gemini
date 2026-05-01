package se.iths.lw.labb1integrationmotgooglegemini.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.service.AppUserService;
import se.iths.lw.labb1integrationmotgooglegemini.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final AppUserService appUserService;

    private AppUser getUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        String googleId;
        String email;
        String avatarUrl;
        String refreshToken = null;

        if (principal instanceof OidcUser oidcUser) {
            googleId = oidcUser.getSubject();
            email = oidcUser.getEmail();
            avatarUrl = oidcUser.getPicture();
        } else if (principal instanceof DefaultOAuth2User oauthUser) {
            googleId = oauthUser.getName();
            email = (String) oauthUser.getAttributes().get("email");
            avatarUrl = (String) oauthUser.getAttributes().get("picture");
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }

        return appUserService.loadOrCreateUser(googleId, email, avatarUrl, refreshToken);
    }







@GetMapping("/")
public String index(Model model, Authentication authentication) {
    model.addAttribute("loggedIn", authentication != null);

    if (authentication != null) {
        try {
            AppUser appUser = getUser(authentication);
            model.addAttribute("history", chatService.getHistory(appUser));
            model.addAttribute("avatar", appUser.getAvatarUrl());

            // 确保这些属性存在，即使为 null
            if (!model.containsAttribute("lastQuestion")) {
                model.addAttribute("lastQuestion", null);
            }
            if (!model.containsAttribute("lastAnswer")) {
                model.addAttribute("lastAnswer", null);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Kunde inte ladda användardata: " + e.getMessage());
        }
    }

    return "index";
}



    @PostMapping("/ask")
    public String ask(@RequestParam String prompt, Authentication authentication, Model model) {
        model.addAttribute("loggedIn", true);

        try {
            AppUser appUser = getUser(authentication);
            var chatRecord = chatService.askAndSave(appUser, prompt);

            model.addAttribute("lastQuestion", chatRecord.getQuestion());
            model.addAttribute("lastAnswer", chatRecord.getAnswer());
            model.addAttribute("history", chatService.getHistory(appUser));
            model.addAttribute("avatar", appUser.getAvatarUrl());

        } catch (Exception e) {
            model.addAttribute("lastQuestion", prompt);
            model.addAttribute("lastAnswer", "Ett fel uppstod: " + e.getMessage());
            model.addAttribute("error", e.getMessage());

            // 即使出错，也尝试加载历史记录
            if (authentication != null) {
                try {
                    AppUser appUser = getUser(authentication);
                    model.addAttribute("history", chatService.getHistory(appUser));
                    model.addAttribute("avatar", appUser.getAvatarUrl());
                } catch (Exception ex) {
                    model.addAttribute("history", java.util.Collections.emptyList());
                }
            }
        }

        return "index";
    }
}




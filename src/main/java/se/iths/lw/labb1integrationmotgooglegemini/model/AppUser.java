package se.iths.lw.labb1integrationmotgooglegemini.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(
        name="app_user",
        schema="lab_1_integration_mot_google_gemini"
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String googleId;
    private String email;
    private String avatarUrl;
    private String refreshToken;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy="appUser", fetch=FetchType.LAZY,cascade= CascadeType.ALL,orphanRemoval=true)
    private List<ChatRecord> chatRecords;

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser)) return false;
        AppUser appUser = (AppUser) o;
        return Objects.equals(email,appUser.email);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(email);
    }
}

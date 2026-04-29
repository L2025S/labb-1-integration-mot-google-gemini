package se.iths.lw.labb1integrationmotgooglegemini.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name="chat_record",
        schema="lab_1_integration_mot_google_gemini")
public class ChatRecord {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="app_user_id")
    private AppUser appUser;



    @Override
    public String toString() {
        return "ChatRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ChatRecord)) return false;
        ChatRecord that = (ChatRecord) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(id);
    }


}

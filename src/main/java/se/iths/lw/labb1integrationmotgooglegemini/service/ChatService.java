
package se.iths.lw.labb1integrationmotgooglegemini.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.model.ChatRecord;
import se.iths.lw.labb1integrationmotgooglegemini.repository.ChatRecordRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final GeminiService geminiService;
    private final ChatRecordRepository chatRecordRepository;

    public ChatRecord askAndSave(AppUser appUser, String question) {
        log.info("Processing question for user: {}", appUser.getEmail());

        // Make sure question is not null
        String safeQuestion = (question == null || question.trim().isEmpty())
                ? "Hej, berätta lite om dig själv"
                : question.trim();

        String answer = geminiService.askGemini(safeQuestion);

        //Make sure answer is not null
        String safeAnswer = (answer == null) ? "❌ Tyvärr, jag kunde inte generera ett svar." : answer;

        ChatRecord chatRecord = new ChatRecord();
        chatRecord.setQuestion(safeQuestion);
        chatRecord.setAnswer(safeAnswer);
        chatRecord.setCreatedAt(LocalDateTime.now());
        chatRecord.setAppUser(appUser);
        chatRecord.setUserId(appUser.getId());

        try {
            return chatRecordRepository.save(chatRecord);
        } catch (Exception e) {
            log.error("Failed to save chat record", e);
            // Even if it fails, it still shows a reason to the user.
            chatRecord.setId(-1L); // temperate id means it is not saved.
            return chatRecord;
        }
    }

    public List<ChatRecord> getHistory(AppUser appUser) {
        try {
            List<ChatRecord> history = chatRecordRepository.findByAppUserOrderByCreatedAtDesc(appUser);
            return history != null ? history : new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to load history for user: {}", appUser.getEmail(), e);
            return new ArrayList<>();
        }
    }
}


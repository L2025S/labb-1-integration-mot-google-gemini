package se.iths.lw.labb1integrationmotgooglegemini.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import se.iths.lw.labb1integrationmotgooglegemini.model.ChatRecord;
import se.iths.lw.labb1integrationmotgooglegemini.repository.ChatRecordRepository;

import java.util.List;


@AllArgsConstructor
@Service
public class ChatRecordService {
    private final ChatRecordRepository chatRecordRepository;


    public List<ChatRecord> findChatRecordsByUserId(Long userId){
        return chatRecordRepository.findAllByUserId(userId);
    }
}

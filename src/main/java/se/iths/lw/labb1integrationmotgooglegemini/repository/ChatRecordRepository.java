package se.iths.lw.labb1integrationmotgooglegemini.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.iths.lw.labb1integrationmotgooglegemini.model.AppUser;
import se.iths.lw.labb1integrationmotgooglegemini.model.ChatRecord;
import java.util.List;


@Repository
public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {
  List<ChatRecord> findByAppUserOrderByCreatedAtDesc(AppUser appUser);

}

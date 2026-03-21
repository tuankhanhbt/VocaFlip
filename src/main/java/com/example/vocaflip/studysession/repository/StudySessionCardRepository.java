package com.example.vocaflip.studysession.repository;

import com.example.vocaflip.studysession.entity.StudySessionCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionCardRepository extends JpaRepository<StudySessionCard, Long> {

    List<StudySessionCard> findByStudySessionIdOrderByOrderIndexAsc(Long studySessionId);
}

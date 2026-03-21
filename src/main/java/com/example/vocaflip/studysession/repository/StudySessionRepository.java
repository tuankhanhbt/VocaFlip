package com.example.vocaflip.studysession.repository;

import com.example.vocaflip.studysession.entity.StudySession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUserIdOrderByStartedAtDesc(Long userId);
}

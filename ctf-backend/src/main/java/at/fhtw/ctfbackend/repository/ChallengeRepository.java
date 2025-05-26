package at.fhtw.ctfbackend.repository;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, String> { }



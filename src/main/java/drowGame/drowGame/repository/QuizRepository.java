package drowGame.drowGame.repository;

import drowGame.drowGame.entity.QuizEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class QuizRepository {
    @PersistenceContext
    EntityManager em;


    public QuizEntity getQuizEntity(int randomQuizNumber) {
        return em.find(QuizEntity.class, randomQuizNumber);
    }
}

package drowGame.drowGame.repository;

import drowGame.drowGame.entity.ChattingEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ChattingRepository {
    @PersistenceContext
    EntityManager em;

    public ChattingEntity chatContentSave(ChattingEntity chattingEntity) {
        em.persist(chattingEntity);
        return chattingEntity;
    }

    public List<ChattingEntity> getChattingData(String myId) {
        return em.createQuery("select c from ChattingEntity as c where c.sender = :myId or c.receiver = :myId", ChattingEntity.class)
                .setParameter("myId", myId)
                .getResultList();

    }
}

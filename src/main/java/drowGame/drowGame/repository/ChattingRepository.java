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

    public List<ChattingEntity> getChatting(String myId, String member_id){
        return em.createQuery("select c from ChattingEntity as c where (c.sender=:myId and c.receiver=:member_id) or (c.sender=:member_id and c.receiver=:myId)", ChattingEntity.class)
                .setParameter("myId", myId)
                .setParameter("member_id", member_id)
                .getResultList();
    }
}

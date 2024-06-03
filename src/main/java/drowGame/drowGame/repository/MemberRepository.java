package drowGame.drowGame.repository;

import drowGame.drowGame.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {
    @PersistenceContext
    EntityManager em;

    public MemberEntity findById(String id) {
        return em.find(MemberEntity.class, id);
    }

    public void memberSave(MemberEntity memberEntity) {
        em.persist(memberEntity);
    }
}

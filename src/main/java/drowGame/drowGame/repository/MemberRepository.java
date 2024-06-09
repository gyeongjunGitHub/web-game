package drowGame.drowGame.repository;

import drowGame.drowGame.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MemberRepository {
    @PersistenceContext
    EntityManager em;

    public Optional<MemberEntity> findById(String id) {
        MemberEntity memberEntity = em.find(MemberEntity.class, id);
        return Optional.ofNullable(memberEntity);
    }

    public void memberSave(MemberEntity memberEntity) {
        em.persist(memberEntity);
    }
}

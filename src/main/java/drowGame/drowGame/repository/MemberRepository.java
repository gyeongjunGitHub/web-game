package drowGame.drowGame.repository;

import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {
    @PersistenceContext
    EntityManager em;

    public Optional<MemberEntity> findById(String id) {
        MemberEntity memberEntity = em.find(MemberEntity.class, id);
        return Optional.ofNullable(memberEntity);
    }
    public List<MemberEntity> findByNickName(String nick_name){
        return em.createQuery("select m from MemberEntity m where m.nick_name=:nick_name", MemberEntity.class)
                .setParameter("nick_name", nick_name)
                .getResultList();

    }

    public String memberSave(MemberEntity memberEntity) {
        em.persist(memberEntity);
        return memberEntity.getId();
    }
}

package drowGame.drowGame.repository;

import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
    public MemberEntity findByNickName(String nick_name){
        try {
            return em.createQuery("select m from MemberEntity m where m.nick_name=:nick_name", MemberEntity.class)
                    .setParameter("nick_name", nick_name)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }
    public String memberSave(MemberEntity memberEntity) {
        em.persist(memberEntity);
        return memberEntity.getId();
    }
    public List<MyItemsEntity> getMyItems(String myId) {
        return em.createQuery("select m from MyItemsEntity m where m.member_id=:member_id", MyItemsEntity.class)
                .setParameter("member_id", myId)
                .getResultList();
    }

    public List<MemberEntity> findAll() {
        return em.createQuery("select m from MemberEntity m", MemberEntity.class)
                .getResultList();
    }
}

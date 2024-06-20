package drowGame.drowGame.repository;

import drowGame.drowGame.entity.FriendEntity;
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

    public String memberSave(MemberEntity memberEntity) {
        em.persist(memberEntity);
        return memberEntity.getId();
    }

    public void findFriendList(String myId) {
        FriendEntity myId1 = em.createQuery("select f from FriendEntity as f where f.id.request_member_id=:myId", FriendEntity.class)
                .setParameter("myId", myId)
                .getSingleResult();
//        System.out.println(myId1.getId().getRequest_member_id());
//        System.out.println(myId1.getId().getRequested_member_id());
//        System.out.println(myId1.isAcceptance_status());
    }
}

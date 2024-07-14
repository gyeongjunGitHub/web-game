package drowGame.drowGame.repository;

import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.FriendId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FriendRepository {
    @PersistenceContext
    EntityManager em;

    public void addFriend(FriendEntity friendEntity, FriendEntity friendEntity1) {
        em.persist(friendEntity);
        em.persist(friendEntity1);
    }

    public List<FriendEntity> findFriendList(String myId) {
        return em.createQuery("select f from FriendEntity as f where f.id.member_id = :myId", FriendEntity.class)
                .setParameter("myId", myId)
                .getResultList();
    }

    public Optional<FriendEntity> findById(FriendId friendId) {
        FriendEntity friendEntity = em.find(FriendEntity.class, friendId);
        return Optional.ofNullable(friendEntity);
    }

    public List<FriendEntity> findByFriendId(String myId) {
        return em.createQuery("select f from FriendEntity f where f.id.friend_id=:myId", FriendEntity.class)
                .setParameter("myId", myId)
                .getResultList();
    }
}

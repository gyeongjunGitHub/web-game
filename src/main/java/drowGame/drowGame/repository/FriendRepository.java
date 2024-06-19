package drowGame.drowGame.repository;

import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.FriendId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class FriendRepository {
    @PersistenceContext
    EntityManager em;
//    public void test() {
//        FriendEntity friendEntity = new FriendEntity();
//        FriendId friendId = new FriendId();
//        friendId.setRequest_member_id("admin");
//        friendId.setRequested_member_id("123");
//        friendEntity.setId(friendId);
//        em.persist(friendEntity);
//    }
}

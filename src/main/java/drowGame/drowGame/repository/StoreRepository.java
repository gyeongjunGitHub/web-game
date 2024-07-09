package drowGame.drowGame.repository;

import drowGame.drowGame.entity.ItemEntity;
import drowGame.drowGame.entity.MyItemsEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StoreRepository {

    @PersistenceContext
    EntityManager em;
    public void itemRegistration(ItemEntity itemEntity) {
        em.persist(itemEntity);
    }
    public List<ItemEntity> getItems() {
        return em.createQuery("select i from ItemEntity as i", ItemEntity.class)
                .getResultList();
    }

    public List<MyItemsEntity> getMyItems(String member_id) {
        return em.createQuery("select m from MyItemsEntity m where m.member_id=:member_id", MyItemsEntity.class)
                .setParameter("member_id", member_id)
                .getResultList();
    }
}

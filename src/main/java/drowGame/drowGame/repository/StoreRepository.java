package drowGame.drowGame.repository;

import drowGame.drowGame.entity.ItemEntity;
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
}

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
    public boolean itemRegistration(ItemEntity itemEntity) {
        try {
            em.persist(itemEntity);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public List<ItemEntity> getItems() {
        return em.createQuery("select i from ItemEntity as i", ItemEntity.class)
                .getResultList();
    }

    public boolean buy(MyItemsEntity myItemsEntity) {
        try {
            em.persist(myItemsEntity);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean removeItem(Long id) {
        try {
            ItemEntity itemEntity = em.find(ItemEntity.class, id);
            em.remove(itemEntity);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}

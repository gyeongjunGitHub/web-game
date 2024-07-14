package drowGame.drowGame.repository;

import drowGame.drowGame.entity.ProfilePictureEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class ProfilePictureRepository {
    @PersistenceContext
    EntityManager em;

    public boolean saveProfilePicture(ProfilePictureEntity profilePictureEntity) {
        try {
            em.persist(profilePictureEntity);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public ProfilePictureEntity findById(String id) {
        try {
            return em.createQuery("select p from ProfilePictureEntity as p where p.member_id=:id", ProfilePictureEntity.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }
    }
}

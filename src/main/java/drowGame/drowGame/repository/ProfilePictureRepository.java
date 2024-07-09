package drowGame.drowGame.repository;

import drowGame.drowGame.entity.ProfilePictureEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class ProfilePictureRepository {
    @PersistenceContext
    EntityManager em;

    public void saveProfilePicture(ProfilePictureEntity profilePictureEntity) {
        em.persist(profilePictureEntity);
    }

    public ProfilePictureEntity findById(String id) {
        return em.createQuery("select p from ProfilePictureEntity as p where p.member_id=:id", ProfilePictureEntity.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}

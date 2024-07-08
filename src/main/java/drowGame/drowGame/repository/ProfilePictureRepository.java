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
}

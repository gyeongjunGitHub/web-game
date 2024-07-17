package drowGame.drowGame.repository;

import drowGame.drowGame.entity.GameSetting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GameSettingRepository {
    @PersistenceContext
    EntityManager em;

    public List<GameSetting> getSetting() {
        return em.createQuery("select g from GameSetting g", GameSetting.class)
                .getResultList();
    }

    public GameSetting findByName(String name) {
        return em.find(GameSetting.class, name);
    }
}

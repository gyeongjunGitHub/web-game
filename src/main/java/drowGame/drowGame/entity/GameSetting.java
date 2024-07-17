package drowGame.drowGame.entity;

import drowGame.drowGame.dto.GameSettingDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "game_setting")
public class GameSetting {
    @Id
    @Column(name = "name")
    private String name;

    @Column
    private int value;

    public GameSetting(){}
    public GameSetting(GameSettingDTO gameSettingDTO){
        this.name = gameSettingDTO.getName();
        this.value = gameSettingDTO.getValue();
    }
}

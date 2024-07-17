package drowGame.drowGame.dto;

import drowGame.drowGame.entity.GameSetting;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSettingDTO {
    private String name;
    private int value;

    public GameSettingDTO(){}
    public GameSettingDTO(GameSetting gameSetting){
        this.name = gameSetting.getName();
        this.value = gameSetting.getValue();
    }
}

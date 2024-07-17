package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.GameSettingDTO;
import drowGame.drowGame.dto.ItemDTO;
import drowGame.drowGame.entity.GameSetting;
import drowGame.drowGame.repository.GameSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameSettingService {
    private final GameSettingRepository gameSettingRepository;


    public List<GameSettingDTO> getSetting() {
        List<GameSettingDTO> gameSettingDTOList = new ArrayList<>();
        List<GameSetting> setting = gameSettingRepository.getSetting();
        for(GameSetting g : setting){
            GameSettingDTO gameSettingDTO = new GameSettingDTO(g);
            gameSettingDTOList.add(gameSettingDTO);
        }
        return gameSettingDTOList;
    }

    @Transactional
    public boolean updateSetting(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        GameSettingDTO gameSettingDTO = new GameSettingDTO();
        try {
            gameSettingDTO = objectMapper.readValue(data, GameSettingDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            GameSetting byName = gameSettingRepository.findByName(gameSettingDTO.getName());
            byName.setValue(gameSettingDTO.getValue());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}

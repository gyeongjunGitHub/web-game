package drowGame.drowGame.controller;

import drowGame.drowGame.dto.GameSettingDTO;
import drowGame.drowGame.entity.GameSetting;
import drowGame.drowGame.service.GameSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@RestController
public class GameSettingController {
    private final GameSettingService gameSettingService;

    @GetMapping("/getSetting")
    public ResponseEntity<List<GameSettingDTO>> getSetting(){
        return ResponseEntity.ok(gameSettingService.getSetting());
    }
    @PostMapping("/updateSetting")
    public boolean updateSetting(@RequestBody String data){
        return gameSettingService.updateSetting(data);
    }
}

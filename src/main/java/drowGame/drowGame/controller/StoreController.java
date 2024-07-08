package drowGame.drowGame.controller;

import drowGame.drowGame.dto.ItemDTO;
import drowGame.drowGame.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    @PostMapping("/itemRegistration")
    public void itemRegistration(@RequestBody String data){
        storeService.itemRegistration(data);
    }

    @GetMapping("/getItems")
    public ResponseEntity<List<ItemDTO>> getItems(){
        return ResponseEntity.ok(storeService.getItems());
    }
}

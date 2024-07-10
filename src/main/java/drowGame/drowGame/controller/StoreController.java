package drowGame.drowGame.controller;

import drowGame.drowGame.dto.ItemDTO;
import drowGame.drowGame.dto.MyItemsDTO;
import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.service.StoreService;
import jakarta.servlet.http.HttpSession;
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
    private final MemberSessionService memberSessionService;
    @PostMapping("/itemRegistration")
    public void itemRegistration(@RequestBody String data){
        storeService.itemRegistration(data);
    }

    @GetMapping("/getItems")
    public ResponseEntity<List<ItemDTO>> getItems(){
        return ResponseEntity.ok(storeService.getItems());
    }
    @GetMapping("/getMyItems")
    public ResponseEntity<List<MyItemsDTO>> getMyItems(HttpSession httpSession){
        return ResponseEntity.ok(storeService.getMyItems(httpSession));
    }

    @PostMapping("/buy")
    public int buy(@RequestBody String item, HttpSession httpSession){
        storeService.buy(item, httpSession);
        return 0;
    }
}

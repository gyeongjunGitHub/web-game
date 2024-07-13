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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @PostMapping("/itemRegistration")
    public void itemRegistration(@RequestParam(name = "itemPicture") MultipartFile itemPicture,
                                 @RequestParam(name = "name") String name,
                                 @RequestParam(name = "price") int price) throws IOException {
        storeService.itemRegistration(itemPicture, name, price);
    }

    @GetMapping("/getItems")
    public ResponseEntity<List<ItemDTO>> getItems(){
        return ResponseEntity.ok(storeService.getItems());
    }

    @PostMapping("/buy")
    public int buy(@RequestBody String item, HttpSession httpSession){
        storeService.buy(item, httpSession);
        return 1;
    }
}

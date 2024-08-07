package drowGame.drowGame.controller;

import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.repository.MemberRepository;
import drowGame.drowGame.repository.ProfilePictureRepository;
import drowGame.drowGame.service.MemberSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberSessionService memberSessionService;
    @GetMapping("/")
    public String home(HttpSession httpSession) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        if(myId == null){
            return "loginForm";
        }
        return "main";
    }

    @GetMapping("/main")
    public String goMain(HttpSession session) {
        String myId = memberSessionService.getMemberId(session.getId());
        if(myId == null){
            return "loginForm";
        }
        return "main";
    }

    @GetMapping("/drowGame")
    public String goDrowGame(){
        return "drowGame";
    }

    @GetMapping("/myPage")
    public String myPage(HttpSession httpSession){
        String role = memberSessionService.getMemberRole(httpSession.getId());
        if(role == null){
            return "loginForm";
        }
        if (role.equals("ROLE_ADMIN")){
            return "adminPage";
        }
        return "myPage";
    }
    @GetMapping("/store")
    public String store(HttpSession httpSession){
        String myId = memberSessionService.getMemberId(httpSession.getId());
        if(myId == null){
            return "loginForm";
        }
        return "store";
    }
    @GetMapping("/myItems")
    public String myItems(HttpSession httpSession){
        String myId = memberSessionService.getMemberId(httpSession.getId());
        if(myId == null){
            return "loginForm";
        }
        return "myItems";
    }
}

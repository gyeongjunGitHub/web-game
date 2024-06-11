package drowGame.drowGame.controller;

import drowGame.drowGame.service.MemberSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberSessionService memberSessionService;
    @GetMapping("/")
    public String home() {
        return "loginForm";
    }

    @GetMapping("/main")
    public String goMain(HttpSession session) {
        String myId = memberSessionService.getMemberId(session.getId());
        if(myId == null){
            return "loginForm";
        }
        return "main";
    }
}

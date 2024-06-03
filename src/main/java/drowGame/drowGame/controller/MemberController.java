package drowGame.drowGame.controller;

import drowGame.drowGame.SocketHandler;
import drowGame.drowGame.dto.MemberDTO;
import drowGame.drowGame.service.MemberService;
import drowGame.drowGame.service.MemberSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.WebSocketSession;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/loginProc")
    public String loginProc(@ModelAttribute MemberDTO memberDTO, HttpSession httpSession){
        memberService.loginProc(memberDTO, httpSession);
        return "main";
    }
    @GetMapping("/join")
    public String goJoinForm(){
        return "joinForm";
    }
    @PostMapping("/joinProc")
    public String joinProc(@ModelAttribute MemberDTO memberDTO){
        memberService.joinProc(memberDTO);
        return "loginForm";
    }

}

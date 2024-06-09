package drowGame.drowGame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import drowGame.drowGame.dto.MemberDTO;
import drowGame.drowGame.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/logout")
    public String logoutProc(HttpSession httpSession){
        memberService.logoutProc(httpSession);
        return "loginForm";
    }

    @PostMapping("/duplicateCheck")
    @ResponseBody
    public ResponseEntity<Void> duplicateCheck(@RequestBody String data) throws JsonProcessingException {
        memberService.duplicateCheck(data);
        return ResponseEntity.ok().build();
    }

}

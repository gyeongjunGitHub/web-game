package drowGame.drowGame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import drowGame.drowGame.dto.*;
import drowGame.drowGame.service.MemberService;
import drowGame.drowGame.service.MemberSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/loginProc")
    @ResponseBody
    public ResponseEntity<ResultDTO> loginProc(@RequestBody MemberDTO memberDTO, HttpSession httpSession) {
        ResultDTO resultDTO = memberService.loginProc(memberDTO, httpSession);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/join")
    public String goJoinForm() {
        return "joinForm";
    }

    @PostMapping("/joinProc")
    @ResponseBody
    public ResponseEntity<ResultDTO> joinProc(@RequestBody MemberDTO memberDTO) {
        ResultDTO resultDTO = memberService.joinProc(memberDTO);
        memberService.setBasicProfile(memberDTO);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/logout")
    public String logoutProc(HttpSession httpSession) {
        memberService.logoutProc(httpSession);
        return "loginForm";
    }

    @PostMapping("/idDuplicateCheck")
    @ResponseBody
    public ResponseEntity<ResultDTO> duplicateCheck(@RequestBody String data) throws JsonProcessingException {
        ResultDTO resultDTO = memberService.idDuplicateCheck(data);
        return ResponseEntity.ok(resultDTO);
    }
    @PostMapping("/nickNameDuplicateCheck")
    @ResponseBody
    public ResponseEntity<ResultDTO> nickNameDuplicateCheck(@RequestBody String data) throws JsonProcessingException {
        ResultDTO resultDTO = memberService.nickNameDuplicateCheck(data);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/searchMember")
    @ResponseBody
    public MemberDTO searchMember(@RequestParam(name = "nick_name") String nick_name) {
        return memberService.findByNickName(nick_name);
    }

    @GetMapping("/isAlreadyFriend")
    @ResponseBody
    public ResultDTO alreadyFriendCheck(@RequestParam(name = "nick_name") String nick_name, @RequestParam(name = "myId") String myId) {
        return memberService.alreadyFriendCheck(nick_name, myId);
    }

    @GetMapping("/getMemberInfo")
    @ResponseBody
    public MemberDTO getMemberInfo(HttpSession httpSession) {
        return memberService.getMemberInfo(httpSession);
    }

    @GetMapping("/getProfile")
    @ResponseBody
    public ProfilePictureDTO getProfilePicture(@RequestParam(name = "id")String id) {
        return memberService.getProfilePicture(id);
    }

    @PostMapping("/selectBasicProfile")
    @ResponseBody
    public boolean setBasicProfileProfile(@RequestParam(name = "file") MultipartFile file, HttpSession session) throws IOException {
        return memberService.selectBasicProfile(file, session);
    }
    @GetMapping("/getMyGamePoint")
    @ResponseBody
    public int getMyGamePoint(HttpSession httpSession){
        return memberService.getMyGamePoint(httpSession);
    }

    @PostMapping("/updateProfilePicture")
    @ResponseBody
    public int updateProfilePicture(@RequestParam(name = "picture") MultipartFile multipartFile, HttpSession httpSession) throws IOException {
        memberService.updateProfilePicture(httpSession, multipartFile);
        return 1;
    }

    @GetMapping("/getMyItems")
    public ResponseEntity<List<MyItemsDTO>> getMyItems(HttpSession httpSession){
        return ResponseEntity.ok(memberService.getMyItems(httpSession));
    }
    @PostMapping("/updateNickName")
    @ResponseBody
    public boolean updateNickName(@RequestParam(name = "nick_name")String nick_name, HttpSession httpSession){
        return memberService.updateNickName(httpSession, nick_name);
    }

    @GetMapping("/getChatting")
    @ResponseBody
    public ResponseEntity<List<ChattingDTO>> getChatting(@RequestParam(name = "member_id")String member_id, HttpSession httpSession) {

        return ResponseEntity.ok(memberService.getChatting(httpSession, member_id));
    }

    @GetMapping("/setIsRead")
    @ResponseBody
    public void setIsRead(@RequestParam(name = "member_id")String member_id, HttpSession httpSession){
        memberService.setIsRead(httpSession, member_id);
    }

    @GetMapping("/getAllMember")
    @ResponseBody
    public ResponseEntity<List<MemberDTO>> getAllMember(){
        return ResponseEntity.ok(memberService.getAllMember());
    }

    @PostMapping("/updateMemberInfo")
    @ResponseBody
    public boolean updateMemberInfo(@RequestBody MemberDTO memberDTO){
        return memberService.updateMemberInfo(memberDTO);
    }
}

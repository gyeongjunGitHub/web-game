package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.*;
import drowGame.drowGame.entity.*;
import drowGame.drowGame.repository.ChattingRepository;
import drowGame.drowGame.repository.FriendRepository;
import drowGame.drowGame.repository.MemberRepository;
import drowGame.drowGame.repository.ProfilePictureRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberSessionService memberSessionService;
    private final FriendRepository friendRepository;
    private final ProfilePictureRepository profilePictureRepository;
    private final ChattingRepository chattingRepository;

    public ResultDTO loginProc(MemberDTO memberDTO, HttpSession httpSession) {

        // 유효성 검사
        Optional<MemberEntity> OptionalById = memberRepository.findById(memberDTO.getId());
        ResultDTO resultDTO = new ResultDTO();

        // 아이디가 존재
        if (OptionalById.isPresent()) {
            MemberDTO byId = new MemberDTO(OptionalById.get());
            // 비밀번호 일치
            if (byId.getPassword().equals(memberDTO.getPassword())) {
                //세션에 추가
                if(byId == null){
                    System.out.println(byId + "널;;");
                }
                memberSessionService.addSession(httpSession.getId(), byId);

                resultDTO.setResult(1);
                return resultDTO;
            }
            // 비밀번호 불일치
            else {
                resultDTO.setResult(0);
                return resultDTO;
            }
        }
        //아이디 존재 X
        else {
            resultDTO.setResult(0);
            return resultDTO;
        }
    }
    @Transactional
    public ResultDTO joinProc(MemberDTO memberDTO) {
        // 유효성 및 중복 검사
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setPassword(memberDTO.getPassword());
        memberEntity.setNick_name(memberDTO.getNick_name());
        memberEntity.setName(memberDTO.getName());
        memberEntity.setGender(memberDTO.getGender());
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setGame_point(0);
        memberEntity.setRole("ROLE_USER");

        String result = memberRepository.memberSave(memberEntity);
        ResultDTO resultDTO = new ResultDTO();
        if (result == null) {
            resultDTO.setResult(0);
        } else {
            resultDTO.setResult(1);
        }
        return resultDTO;
    }
    public void logoutProc(HttpSession httpSession) {
        memberSessionService.removeSession(httpSession.getId());
    }
    public ResultDTO idDuplicateCheck(String data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MemberDTO memberDTO = objectMapper.readValue(data, MemberDTO.class);

        Optional<MemberEntity> byId = memberRepository.findById(memberDTO.getId());

        ResultDTO resultDTO = new ResultDTO();

        if (byId.isPresent()) {
            resultDTO.setResult(0);
        } else {
            resultDTO.setResult(1);
        }
        return resultDTO;
    }
    public ResultDTO nickNameDuplicateCheck(String data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MemberDTO memberDTO = objectMapper.readValue(data, MemberDTO.class);

        List<MemberEntity> byNickName = memberRepository.findByNickName(memberDTO.getNick_name());

        ResultDTO resultDTO = new ResultDTO();

        if (!byNickName.isEmpty()) {
            resultDTO.setResult(0); // 사용 불가능
        } else {
            resultDTO.setResult(1); // 사용가능
        }
        return resultDTO;
    }
    public MemberDTO findById(String id) {
        Optional<MemberEntity> byId = memberRepository.findById(id);
        if(byId.isPresent()){
            MemberDTO memberDTO = new MemberDTO(byId.get());
            return memberDTO;
        }else {
            MemberDTO memberDTO = new MemberDTO();
            return memberDTO;
        }
    }
    public ResultDTO alreadyFriendCheck(String id, String myId) {
        FriendId friendId = new FriendId();
        ResultDTO resultDTO = new ResultDTO();
        friendId.setMember_id(myId);
        friendId.setFriend_id(id);
        Optional<FriendEntity> byId = friendRepository.findById(friendId);
        if(byId.isPresent()){
            resultDTO.setResult(1); //친구 존재
        }else {
            resultDTO.setResult(0); // 존재하지 않음
        }
        return resultDTO;
    }
    public String goMyPage(HttpSession httpSession, Model model) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        if (myId == null){
            return "loginForm";
        }else{
            Optional<MemberEntity> byId = memberRepository.findById(myId);
            if(byId.isPresent()){
                MemberEntity memberEntity = byId.get();
                MemberDTO memberDTO = new MemberDTO(memberEntity);
                model.addAttribute("myInfo", memberDTO);
            }
            return "myPage";
        }
    }
    public MemberDTO getMemberInfo(HttpSession httpSession) {
        String memberId = memberSessionService.getMemberId(httpSession.getId());
        if(memberId != null){
            Optional<MemberEntity> byId = memberRepository.findById(memberId);
            if(byId.isPresent()){
                MemberDTO memberDTO = new MemberDTO(byId.get());
                ProfilePictureEntity byId1 = profilePictureRepository.findById(memberId);
                ProfilePictureDTO profilePictureDTO = new ProfilePictureDTO(byId1);
                memberDTO.setProfilePictureDTO(profilePictureDTO);
                return memberDTO;
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
    @Transactional
    public void selectBasicProfile(MultipartFile file, HttpSession session) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + "_" + originalFileName;

        String windowSavePath = "C:/images/" + storedFileName;
        String ec2SavePath = "/home/images/" + storedFileName;

        File saveFile = new File(windowSavePath);

        //폴더가 없을 경우 폴더 생성!
        if(!saveFile.exists()){
            saveFile.mkdirs();
        }

        //file 저장
        file.transferTo(saveFile);

        ProfilePictureEntity profilePictureEntity = new ProfilePictureEntity();
        profilePictureEntity.setOriginal_file_name(originalFileName);
        profilePictureEntity.setStored_file_name(storedFileName);
        profilePictureEntity.setMember_id(memberSessionService.getMemberId(session.getId()));
        profilePictureRepository.saveProfilePicture(profilePictureEntity);
    }
    @Transactional
    public void updateProfilePicture(HttpSession httpSession, MultipartFile file) throws IOException {
        String myId = memberSessionService.getMemberId(httpSession.getId());

        String originalFileName = file.getOriginalFilename();
        String storedFileName = System.currentTimeMillis() + "_" + originalFileName;

        String windowSavePath = "C:/images/" + storedFileName;
        String ec2SavePath = "/home/images/" + storedFileName;

        File saveFile = new File(windowSavePath);

        //폴더가 없을 경우 폴더 생성!
        if(!saveFile.exists()){
            saveFile.mkdirs();
        }

        //file 저장
        file.transferTo(saveFile);

        ProfilePictureEntity byId = profilePictureRepository.findById(myId);
        byId.setOriginal_file_name(originalFileName);
        byId.setStored_file_name(storedFileName);

        List<MyItemsEntity> myItems = memberRepository.getMyItems(myId);
        for (MyItemsEntity m : myItems){
            if (m.getName().equals("프로필 사진 변경권")){
                m.setCount(m.getCount()-1);
            }

        }


    }
    @Transactional
    public void setBasicProfile(MemberDTO memberDTO){
        Optional<MemberEntity> byId = memberRepository.findById(memberDTO.getId());
        if (byId.isPresent()){
            MemberEntity memberEntity = byId.get();
            ProfilePictureEntity basicFile = ProfilePictureEntity.getBasicFile(memberEntity);
            profilePictureRepository.saveProfilePicture(basicFile);
        }
    }
    public int profileCheck(HttpSession session) {
        ProfilePictureEntity byId = profilePictureRepository.findById(memberSessionService.getMemberId(session.getId()));
        if(byId == null){
            return 1; // 기본 프로필 사진이 없음
        }
        return 0; // 기본 프로필 사진이 이미 있음
    }

    public ProfilePictureDTO getProfilePicture(String id) {
        ProfilePictureEntity byId = profilePictureRepository.findById(id);
        return new ProfilePictureDTO(byId);
    }

    public int getMyGamePoint(HttpSession httpSession) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        int myGamePoint = 0;
        Optional<MemberEntity> byIdOptional = memberRepository.findById(myId);
        if (byIdOptional.isPresent()){
            MemberEntity byId = byIdOptional.get();
            myGamePoint = byId.getGame_point();
            return myGamePoint;
        }
        return -1; // 오류
    }
    public List<MyItemsDTO> getMyItems(HttpSession httpSession) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        List<MyItemsEntity> myItemsEntityList = memberRepository.getMyItems(myId);
        List<MyItemsDTO> myItems = new ArrayList<>();
        for(MyItemsEntity m : myItemsEntityList){
            MyItemsDTO myItemsDTO = new MyItemsDTO(m);
            myItems.add(myItemsDTO);
        }
        return myItems;
    }

    @Transactional
    public void updateNickName(HttpSession httpSession, String nick_name) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        Optional<MemberEntity> byIdOptional = memberRepository.findById(myId);
        if (byIdOptional.isPresent()){
            MemberEntity byId = byIdOptional.get();
            byId.setNick_name(nick_name);
        }
        List<MyItemsEntity> myItems = memberRepository.getMyItems(myId);
        for (MyItemsEntity m : myItems){
            if(m.getName().equals("닉네임 변경권")){
                m.setCount(m.getCount() - 1);
            }
        }
    }

    public List<ChattingDTO> getChatting(HttpSession httpSession, String memberId) {
        String myId = memberSessionService.getMemberId(httpSession.getId());
        List<ChattingEntity> chatting = chattingRepository.getChatting(myId, memberId);
        List<ChattingDTO> chattingDTOList = new ArrayList<>();
        for (ChattingEntity c : chatting){
            ChattingDTO chattingDTO = new ChattingDTO(c);
            chattingDTOList.add(chattingDTO);
        }
        return chattingDTOList;
    }
}

package drowGame.drowGame.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.MemberDTO;
import drowGame.drowGame.dto.ResultDTO;
import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.repository.FriendRepository;
import drowGame.drowGame.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberSessionService memberSessionService;

    public ResultDTO loginProc(MemberDTO memberDTO, HttpSession httpSession) {
        // 유효성 검사

        Optional<MemberEntity> OptionalById = memberRepository.findById(memberDTO.getId());
        ResultDTO resultDTO = new ResultDTO();

        if (OptionalById.isPresent()) {
            MemberEntity byId = OptionalById.get();
            if (byId.getPassword().equals(memberDTO.getPassword())) {
                memberSessionService.addSession(httpSession.getId(), byId.getId());
                resultDTO.setResult(1);
                return resultDTO;
            } else {
                resultDTO.setResult(0);
                return resultDTO;
            }
        } else {
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
        memberEntity.setName(memberDTO.getName());
        memberEntity.setGender(memberDTO.getGender());
        memberEntity.setEmail(memberDTO.getEmail());
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

    public ResultDTO duplicateCheck(String data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MemberDTO memberDTO = objectMapper.readValue(data, MemberDTO.class);

        Optional<MemberEntity> byId = memberRepository.findById(memberDTO.getId());

        ResultDTO resultDTO = new ResultDTO();

        if (byId.isPresent()) {
            System.out.println("아이디가 존재합니다.");
            resultDTO.setResult(0);
        } else {
            resultDTO.setResult(1);
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
}

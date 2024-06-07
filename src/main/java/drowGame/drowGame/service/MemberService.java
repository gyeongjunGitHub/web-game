package drowGame.drowGame.service;

import drowGame.drowGame.dto.MemberDTO;
import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberSessionService memberSessionService;

    public void loginProc(MemberDTO memberDTO, HttpSession httpSession) {
        // 유효성 검사

        MemberEntity byId = memberRepository.findById(memberDTO.getId());
        if (byId.getPassword().equals(memberDTO.getPassword())) {
            memberSessionService.addSession(httpSession.getId(), byId.getId());
        }
    }

    @Transactional
    public void joinProc(MemberDTO memberDTO) {

        // 유효성 및 중복 검사

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setPassword(memberDTO.getPassword());
        memberEntity.setName(memberDTO.getName());
        memberEntity.setGender(memberDTO.getGender());
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setRole("ROLE_USER");
        memberRepository.memberSave(memberEntity);
    }

    public void logoutProc(HttpSession httpSession) {
        memberSessionService.removeSession(httpSession.getId());
    }
}

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
    public void loginProc(MemberDTO memberDTO, HttpSession httpSession) {
        MemberEntity byId = memberRepository.findById(memberDTO.getId());
        if(byId.getPassword().equals(memberDTO.getPassword())){
            System.out.println("로그인 성공");
        }
    }

    @Transactional
    public void joinProc(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberDTO.getId());
        memberEntity.setPassword(memberDTO.getPassword());
        memberEntity.setName(memberDTO.getName());
        memberEntity.setGender(memberDTO.getGender());
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setRole("ROLE_USER");
        memberRepository.memberSave(memberEntity);
    }
}

package drowGame.drowGame.service;

import drowGame.drowGame.dto.MemberDTO;
import drowGame.drowGame.entity.MemberEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemberSessionService {
    private final ConcurrentHashMap<String, MemberDTO> memberSession = new ConcurrentHashMap<>();

    public void addSession(String httpSessionId, MemberDTO memberDTO) {
        //이미 세션에 등록되어 있으면 제거 후 다시 추가
        for(String s : memberSession.keySet()){
            if (memberSession.get(s).getId().equals(memberDTO.getId())){
                memberSession.remove(s);
            }
        }
        memberSession.put(httpSessionId, memberDTO);
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("로그인 -> session ID : "+httpSessionId+", member ID : "+memberDTO.getId()+", nick name : "+memberDTO.getNick_name());
        System.out.println("-----------------------------------------------------------------------");
    }
    public String getMemberId(String httpSessionId) {
        if(memberSession.get(httpSessionId) != null){
            return memberSession.get(httpSessionId).getId();
        }
        return null;
    }
    public String getMemberIdByNickName(String nick_name){
        for(String key : memberSession.keySet()){
            MemberDTO memberDTO = memberSession.get(key);
            System.out.println(memberDTO.getNick_name());
            if(memberDTO.getNick_name().equals(nick_name)){
                return memberDTO.getId();
            }
        }
        return null;
    }
    public String getMemberNickName(String httpSessionId){
        if(memberSession.get(httpSessionId) != null){
            return memberSession.get(httpSessionId).getNick_name();
        }
        return null;
    }
    public String getMemberRole(String httpSessionId){
        if(memberSession.get(httpSessionId) != null){
            return memberSession.get(httpSessionId).getRole();
        }
        return null;
    }

    public void removeSession(String httpSessionId) {
        if(memberSession.get(httpSessionId) != null){
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("로그아웃 -> session ID : " + httpSessionId + ", member ID : " + memberSession.get(httpSessionId).getId());
            System.out.println("-----------------------------------------------------------------------");
        }
        memberSession.remove(httpSessionId);
    }


}

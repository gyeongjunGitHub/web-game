package drowGame.drowGame.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemberSessionService {
    private final ConcurrentHashMap<String, String> memberIdSession = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> memberNickNameSession = new ConcurrentHashMap<>();

    public void addSession(String httpSessionId, String memberId, String nick_name) {
        //이미 세션에 등록되어 있으면 제거 후 다시 추가
        for(String s : memberIdSession.keySet()){
            if(memberIdSession.get(s).equals(memberId)){
                memberIdSession.remove(s);
            }
        }
        for(String s : memberNickNameSession.keySet()){
            if(memberNickNameSession.get(s).equals(nick_name)){
                memberNickNameSession.remove(s);
            }
        }
        memberIdSession.put(httpSessionId, memberId);
        memberNickNameSession.put(httpSessionId, nick_name);
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("로그인 -> session ID : "+httpSessionId+", member ID : "+memberId+", nick name : "+nick_name);
        System.out.println("-----------------------------------------------------------------------");
    }
    public String getMemberId(String httpSessionId) {
        return memberIdSession.get(httpSessionId);
    }
    public String getMemberNickName(String httpSessionId){
        return memberNickNameSession.get(httpSessionId);
    }

    public void removeSession(String httpSessionId) {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("로그아웃 -> session ID : " + httpSessionId + ", member ID : " + memberIdSession.get(httpSessionId));
        System.out.println("-----------------------------------------------------------------------");

        memberIdSession.remove(httpSessionId);
        memberNickNameSession.remove(httpSessionId);
    }
}

package drowGame.drowGame.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemberSessionService {
    private final ConcurrentHashMap<String, String> memberSession = new ConcurrentHashMap<>();

    public void addSession(String httpSessionId, String memberId) {
        //이미 세션에 등록되어 있으면 제거 후 다시 추가
        for(String s : memberSession.keySet()){
            if(memberSession.get(s).equals(memberId)){
                memberSession.remove(s);
            }
        }
        memberSession.put(httpSessionId, memberId);
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("로그인 -> session ID : "+httpSessionId+", member ID : "+memberId);
        System.out.println("-----------------------------------------------------------------------");
    }

    public String getMemberId(String httpSessionId) {
        return memberSession.get(httpSessionId);
    }

    public void removeSession(String httpSessionId) {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("로그아웃 -> session ID : " + httpSessionId + ", member ID : " + memberSession.get(httpSessionId));
        System.out.println("-----------------------------------------------------------------------");

        memberSession.remove(httpSessionId);

    }
}

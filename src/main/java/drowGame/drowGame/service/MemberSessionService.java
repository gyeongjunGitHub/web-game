package drowGame.drowGame.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemberSessionService {
    private final ConcurrentHashMap<String, String> memberSession = new ConcurrentHashMap<>();

    public void addSession(String httpSessionId, String memberId) {
        memberSession.put(httpSessionId, memberId);
    }

    public String getMemberId(String httpSessionId) {
        return memberSession.get(httpSessionId);
    }

    public void removeSession(String httpSessionId) {
        memberSession.remove(httpSessionId);
    }
}

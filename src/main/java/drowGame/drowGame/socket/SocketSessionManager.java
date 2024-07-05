package drowGame.drowGame.socket;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketSessionManager {
    //웹 소켓 세션 담을 맵
    @Getter
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    //웹 소켓 세션 Id, Member Id
    @Getter
    private final ConcurrentHashMap<String, String> memberIdMap = new ConcurrentHashMap<>();

    public void addSessionMap(String sessionId, WebSocketSession session){
        this.sessionMap.put(sessionId, session);
    }
    public void removeSessionMap(WebSocketSession session){
        this.sessionMap.remove(session.getId());
    }
    public void addMemberIdMap(String sessionId, String memberId){
        this.memberIdMap.put(sessionId, memberId);
    }
    public void removeMemberIdMap(WebSocketSession session){
        this.memberIdMap.remove(session.getId());
    }
    public String getMyId(WebSocketSession session){
        return this.memberIdMap.get(session.getId());
    }
    public String getMemberId(String sessionId){
        return this.memberIdMap.get(sessionId);
    }
}

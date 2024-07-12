package drowGame.drowGame.socket.manager;

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

    @Getter final ConcurrentHashMap<String, String> memberNickNameMap = new ConcurrentHashMap<>();

    public void addSessionMap(String sessionId, WebSocketSession session){
        this.sessionMap.put(sessionId, session);
    }
    public void addMemberIdMap(String sessionId, String memberId){
        this.memberIdMap.put(sessionId, memberId);
    }
    public void addMemberNickNameMap(String sessionId, String nick_name){
        this.memberNickNameMap.put(sessionId, nick_name);
    }
    public void removeSessionMap(WebSocketSession session){
        this.sessionMap.remove(session.getId());
    }
    public void removeMemberIdMap(WebSocketSession session){
        this.memberIdMap.remove(session.getId());
    }
    public void removeMemberNickNameMap(WebSocketSession session){
        this.memberNickNameMap.remove(session.getId());
    }
    public String getMyId(WebSocketSession session){
        return this.memberIdMap.get(session.getId());
    }
    public String getMemberId(String sessionId){
        return this.memberIdMap.get(sessionId);
    }
    public String getMemberNickName(String sessionId){
        return this.memberNickNameMap.get(sessionId);
    }
    public String findByIdMemberNickName(String member_id){
        String memberNickName = null;
        for(String sessionId : memberIdMap.keySet()){
            if(memberIdMap.get(sessionId).equals(member_id)){
                memberNickName = memberNickNameMap.get(sessionId);
                break;
            }
        }
        return memberNickName;
    }
}

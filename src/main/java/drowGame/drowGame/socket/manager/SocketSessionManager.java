package drowGame.drowGame.socket.manager;

import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.socket.TypeProc.Message.MessageProc;
import drowGame.drowGame.socket.data.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SocketSessionManager {

    private final MemberSessionService memberSessionService;
    private final MessageProc messageProc;
    //웹 소켓 세션 담을 맵
    @Getter
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<String, String> memberIdMap = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<String, String> memberNickNameMap = new ConcurrentHashMap<>();

    public void addSessionMap(String sessionId, WebSocketSession session) {
        this.sessionMap.put(sessionId, session);
    }

    public void addMemberIdMap(String sessionId, String memberId) {
        this.memberIdMap.put(sessionId, memberId);
    }

    public void addMemberNickNameMap(String sessionId, String nick_name) {
        this.memberNickNameMap.put(sessionId, nick_name);
    }

    public void removeSessionMap(WebSocketSession session) {
        this.sessionMap.remove(session.getId());
    }

    public void removeMemberIdMap(WebSocketSession session) {
        this.memberIdMap.remove(session.getId());
    }

    public void removeMemberNickNameMap(WebSocketSession session) {
        this.memberNickNameMap.remove(session.getId());
    }

    public String getMyId(WebSocketSession session) {
        return this.memberIdMap.get(session.getId());
    }

    public String getMemberId(String sessionId) {
        return this.memberIdMap.get(sessionId);
    }

    public String getMemberNickName(String sessionId) {
        return this.memberNickNameMap.get(sessionId);
    }

    public String findByIdMemberNickName(String member_id) {
        String memberNickName = null;
        for (String sessionId : memberIdMap.keySet()) {
            if (memberIdMap.get(sessionId).equals(member_id)) {
                memberNickName = memberNickNameMap.get(sessionId);
                break;
            }
        }
        return memberNickName;
    }

    public WebSocketSession findReceiverSession(String receiver) {
        for (String key : getSessionMap().keySet()) {
            String memberId = getMemberIdMap().get(key);
            if (receiver.equals(memberId)) {
                return getSessionMap().get(key);
            }
        }
        return null;
    }

    public WebSocketSession duplicateLoginCheck(WebSocketSession session) {
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        for (String s : getMemberIdMap().keySet()) {
            if (myId.equals(getMemberIdMap().get(s))) {
                Data data = new Data();
                data.setType("/member/duplicateLogin");
                messageProc.sendMessage(getSessionMap().get(s), messageProc.dtoToJson(data));
                return getSessionMap().get(s); //중복 로그인
            }
        }
        return null;
    }
}

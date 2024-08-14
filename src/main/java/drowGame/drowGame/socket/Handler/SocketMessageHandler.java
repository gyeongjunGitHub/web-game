package drowGame.drowGame.socket.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.socket.TypeProc.Chatting.ChattingController;
import drowGame.drowGame.socket.TypeProc.Game.GameController;
import drowGame.drowGame.socket.TypeProc.Matching.MatchingController;
import drowGame.drowGame.socket.TypeProc.Member.SocketMemberController;
import drowGame.drowGame.socket.data.SocketRequest;
import drowGame.drowGame.socket.manager.SocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class SocketMessageHandler extends TextWebSocketHandler {
    private final SocketSessionManager sm;
    private final MatchingController matchingController;
    private final GameController gameController;
    private final ChattingController chattingController;
    private final SocketMemberController socketMemberController;


    public SocketRequest socketRequestMapping(String msg){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SocketRequest socketRequest = new SocketRequest();
            return objectMapper.readValue(msg, SocketRequest.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        String msg = message.getPayload();
        SocketRequest socketRequest = socketRequestMapping(msg);
        String type = socketRequest.getType().split("/")[1];
        String request = socketRequest.getType().split("/")[2];

        switch (type){
            case "matching":
                String data = socketRequest.getType().split("/")[3];
                matchingController.controller(session, request, data);
                break;
            case "game":
                gameController.controller(session, request, socketRequest);
                break;
            case "chatting":
                chattingController.controller(session, request, socketRequest);
                break;
            case "member":
                socketMemberController.controller(session, request, socketRequest);
                break;
            default:
                break;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //중복 로그인 체크
        WebSocketSession isDuplicateLogin = sm.duplicateLoginCheck(session);
        if (isDuplicateLogin != null){
            CloseStatus status = new CloseStatus(1001);
            afterConnectionClosed(isDuplicateLogin, status);
        }
        socketMemberController.controller(session, "addSession", null);
        socketMemberController.controller(session, "sendLoginMemberList", null);
        socketMemberController.controller(session, "sendFriendInfo", null);
    }


    //소켓 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        socketMemberController.controller(session, "sendLogoutMember", null);

        //매칭 큐 에서 정보 삭제
        matchingController.controller(session, "cancel", "2");
        matchingController.controller(session, "cancel", "3");

        socketMemberController.controller(session, "removeSessionInfo", null);
        super.afterConnectionClosed(session, status);
    }
}
package drowGame.drowGame.socket;

import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.socket.data.Request1;
import drowGame.drowGame.socket.data.SocketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Timer;
import java.util.TimerTask;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {
    private final SocketService socketService;
    private final MemberSessionService memberSessionService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        String msg = message.getPayload();
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        SocketRequest socketRequest = socketService.socketRequestMapping(msg);
        String requestName = socketRequest.getType();

        switch (requestName){
            case "gameOver":
                socketService.setRequest1(socketRequest, myId, session);
                break;

            case "rollBack":
            case "clear":
            case "push":
            case "sendCoordinate":
                socketService.sendMessageSameRoom(1,session, socketRequest);
                break;

            case "answer":
                Request1 result = socketRequest.typeRequest1(socketRequest);
                result.setSender(myId);
                socketRequest.setData(result);
                socketService.sendMessageSameRoom(0, session, socketRequest);
                socketService.answerCheck(session, result);
                break;

            case "startRound":
                socketService.startRound(session);
                break;

            case "start":
                socketService.gameStart(session);
                break;

            case "sendMessage":
                socketService.sendChatting(session, socketRequest);
                break;

            case "ttabong":
                socketService.ttabong(session, socketRequest.getData().toString());
                break;

            case "addFriendRequest":
                socketService.addFriendRequest(session, socketRequest);
                break;

            case "addFriendResponse":
                socketService.addFriend(socketRequest, session);
                break;

            case "matchingStartDrowGame":
                socketService.startMatching(session, socketRequest, 2);
                break;

            case "matchingCancleDrowGame":
                socketService.removeMatchingQueue(session, 2);
                break;

            case "matchingStartDrowGame3":
                socketService.startMatching(session, socketRequest, 3);
                break;

            case "matchingCancleDrowGame3":
                socketService.removeMatchingQueue(session, 3);
                break;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        //중복 로그인 체크
        WebSocketSession isDuplicateLogin = socketService.duplicateLoginCheck(session);
        if (isDuplicateLogin != null){
            CloseStatus status = new CloseStatus(1001);
            afterConnectionClosed(isDuplicateLogin, status);
        }
        socketService.addSessionInfo(session);
        socketService.sendLoginMemberList(session);
        socketService.sendFriendInfo(session);
    }


    //소켓 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        socketService.sendLogoutMember(session);

        //매칭 큐 에서 정보 삭제
        socketService.removeMatchingQueue(session, 2);
        socketService.removeMatchingQueue(session, 3);

        socketService.removeSessionInfo(session);
        super.afterConnectionClosed(session, status);
    }
}
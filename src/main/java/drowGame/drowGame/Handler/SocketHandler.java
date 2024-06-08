package drowGame.drowGame.Handler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.RequestDTO;
import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.service.SocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class SocketHandler extends TextWebSocketHandler {
    private final SocketService socketService;
    private final MemberSessionService memberSessionService;
    HashMap<String, WebSocketSession> sessionMap = new HashMap<>(); //웹소켓 세션을 담아둘 맵
    ConcurrentHashMap<String, String> socketSessionAndMemberID = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        //메시지 발송
        String msg = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        RequestDTO requestDTO = new RequestDTO();
        requestDTO = objectMapper.readValue(msg, RequestDTO.class);
        System.out.println(requestDTO.getDate());

        if(requestDTO.getRequest().equals("sendMessage")){
            WebSocketSession receiverSession = socketService.findReceiverSession(requestDTO, sessionMap, socketSessionAndMemberID);
            socketService.sendMessage(receiverSession, socketService.dtoToJson(requestDTO));
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //소켓 연결
        super.afterConnectionEstablished(session);

        //소켓에 연결된 member 의 socket session ID 를 sessionMap 에 추가
        sessionMap.put(session.getId(), session);

        //소켓에 연결된 member 의 아이디를 httpSession 에서 가져와 SocketSessionAndMemberID 에 추가
        socketSessionAndMemberID.put(session.getId(), memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));

        System.out.println("-----------------------------------------------------------------------");
        System.out.println("http session ID -> "+session.getAttributes().get("httpSessionId"));
        System.out.println("member ID -> "+memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));
        System.out.println("-----------------------------------------------------------------------");

        //소캣 연결 시 현재 소캣에 연결되어 있는 member의 목록을 전송
        socketService.sendLoginMemberList(session, sessionMap, socketSessionAndMemberID, memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //소켓 종료
        sessionMap.remove(session.getId());
        socketSessionAndMemberID.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }
}
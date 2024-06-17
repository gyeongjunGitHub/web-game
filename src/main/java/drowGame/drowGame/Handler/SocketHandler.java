package drowGame.drowGame.Handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.ChattingDTO;
import drowGame.drowGame.dto.RequestDTO;
import drowGame.drowGame.entity.ChattingEntity;
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

    //웹소켓 세션을 담아둘 맵
    HashMap<String, WebSocketSession> sessionMap = new HashMap<>();

    //웹소켓 세션Id와 MemberId를 담아둘 맵
    ConcurrentHashMap<String, String> socketSessionAndMemberID = new ConcurrentHashMap<>();

    //매칭 대기열 que
    ConcurrentLinkedQueue<String> drowGameMatchingInfo = new ConcurrentLinkedQueue<>();

    //drow game room Info

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        //메시지 발송
        String msg = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        RequestDTO requestDTO = new RequestDTO();
        requestDTO = objectMapper.readValue(msg, RequestDTO.class);
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));

        if(requestDTO.getRequest().equals("matchingStartDrowGame")){
            drowGameMatchingInfo.add(myId);
            socketService.matching(drowGameMatchingInfo);
        }
        if(requestDTO.getRequest().equals("matchingCancleDrowGame")){
            drowGameMatchingInfo.remove(myId);
            socketService.matching(drowGameMatchingInfo);
        }
        
        if (requestDTO.getRequest().equals("sendMessage")) {
            ChattingDTO chattingDTO = new ChattingDTO();
            chattingDTO.setSender(myId);
            chattingDTO.setReceiver(requestDTO.getReceiver());
            chattingDTO.setContent(requestDTO.getData());
            ChattingDTO result = socketService.chatContentSave(chattingDTO);
            result.setRequest(requestDTO.getRequest());

            WebSocketSession receiverSession = socketService.findReceiverSession(result.getReceiver(), sessionMap, socketSessionAndMemberID);
            WebSocketSession senderSession = socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID);
            socketService.sendMessage(receiverSession, socketService.dtoToJson(result));
            socketService.sendMessage(senderSession, socketService.dtoToJson(result));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
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
        socketService.sendLoginMemberList(session, sessionMap, socketSessionAndMemberID, myId);

        //채팅 데이터 전송
        List<ChattingDTO> result = socketService.getChattingData(myId);
        List<String> chattingData = new ArrayList<>();
        for(ChattingDTO c : result){
            //request : getChattingData 로 세팅
            chattingData.add(socketService.dtoToJson(c));
        }
        socketService.sendMessage(socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID), chattingData.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        socketService.sendLogoutMember(session, sessionMap, socketSessionAndMemberID, myId);
        //소켓 종료
        sessionMap.remove(session.getId());
        socketSessionAndMemberID.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }
}
package drowGame.drowGame.Handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.ChattingDTO;
import drowGame.drowGame.dto.FriendDTO;
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

    //gameRoom 정보를 담을 Map
    private final ConcurrentHashMap<WebSocketSession, Integer> gameRooms = new ConcurrentHashMap<WebSocketSession, Integer>();

    //room id
    private final AtomicInteger roomIdGenerator = new AtomicInteger();

    public void createGameRoom(List<WebSocketSession> player_session) {
        // roomId 생성
        int roomId = roomIdGenerator.incrementAndGet();

        for (WebSocketSession ws : player_session){
            gameRooms.put(ws, roomId);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        //메시지 발송
        String msg = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        RequestDTO requestDTO = new RequestDTO();
        requestDTO = objectMapper.readValue(msg, RequestDTO.class);
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));


        if(requestDTO.getRequest().equals("rollBack")){
            //자기 자신 제외 같은 roomId를 가진 유저에게 전송
            int myRoomId = gameRooms.get(session);
            for (WebSocketSession wss : gameRooms.keySet()){
                //roomId는 같고 자기 자신 제외
                if(gameRooms.get(wss) == myRoomId && !wss.equals(session)){
                    socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                }
            }
        }
        if(requestDTO.getRequest().equals("push")){
            //자기 자신 제외 같은 roomId를 가진 유저에게 전송
            int myRoomId = gameRooms.get(session);
            for (WebSocketSession wss : gameRooms.keySet()){
                //roomId는 같고 자기 자신 제외
                if(gameRooms.get(wss) == myRoomId && !wss.equals(session)){
                    socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                }
            }
        }
        if(requestDTO.getRequest().equals("clear")){
            //자기 자신 제외 같은 roomId를 가진 유저에게 전송
            int myRoomId = gameRooms.get(session);
            for (WebSocketSession wss : gameRooms.keySet()){
                //roomId는 같고 자기 자신 제외
                if(gameRooms.get(wss) == myRoomId && !wss.equals(session)){
                    socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                }
            }
        }
        if(requestDTO.getRequest().equals("sendCoordinate")){
            //자기 자신 제외 같은 roomId를 가진 유저에게 전송
            int myRoomId = gameRooms.get(session);
            for (WebSocketSession wss : gameRooms.keySet()){
                //roomId는 같고 자기 자신 제외
                if(gameRooms.get(wss) == myRoomId && !wss.equals(session)){
                    socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                }
            }
        }


        if(requestDTO.getRequest().equals("addFriendRequest")){
            System.out.println(requestDTO.getReceiver());

            requestDTO.setSender(myId);
            WebSocketSession receiverSession = socketService.findReceiverSession(requestDTO.getReceiver(), sessionMap, socketSessionAndMemberID);
            socketService.sendMessage(receiverSession, socketService.dtoToJson(requestDTO));
        }
        if (requestDTO.getRequest().equals("addFriendResponse")){
            socketService.addFriend(requestDTO, myId);
        }
        if(requestDTO.getRequest().equals("matchingStartDrowGame")){
            //매칭 대기열에 추가
            // 소캣 연결 종료시 제거 필요
            drowGameMatchingInfo.add(myId);

            //매치 인원 충족
            if(drowGameMatchingInfo.size()==2){
                List<WebSocketSession> player_session = new ArrayList<>();

                for(int i = 0; i<2; i++){
                    String player = drowGameMatchingInfo.poll();

                    for(String sessionId : socketSessionAndMemberID.keySet()){
                        if(socketSessionAndMemberID.get(sessionId).equals(player)){
                            player_session.add(sessionMap.get(sessionId));
                        }
                    }
                }

                //roomId와 session 저장
                createGameRoom(player_session);
                requestDTO.setResponse("success");

                for(WebSocketSession wss : player_session){
                    socketService.sendMessage(wss, socketService.dtoToJson(requestDTO));
                }
                System.out.println("========= gameRooms Info =========");
                for(WebSocketSession wss : gameRooms.keySet()){
                    System.out.println(socketSessionAndMemberID.get(wss.getId()) + " : " + gameRooms.get(wss));
                }
            }
        }
        if(requestDTO.getRequest().equals("matchingCancleDrowGame")){
            drowGameMatchingInfo.remove(myId);
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
        //안씀 나중에 정리 시 제거
        socketService.sendLoginMemberList(session, sessionMap, socketSessionAndMemberID, myId);

        //접속 시 자기 자신 제외 접속중인 친구들에게 login 정보 알리기

        //소캣 연결 시 친구 목록 전송
        List<FriendDTO> friendDTOList = socketService.getFriendList(myId);
        List<String> friendList = new ArrayList<>();

        //친구 리스트 순회
        for (FriendDTO f : friendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(socketSessionAndMemberID.values().contains(f.getFriend_id())){
                f.setStatus("online");
            }
            friendList.add(socketService.dtoToJson(f));
        }
        socketService.sendMessage(socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID), friendList.toString());

        //채팅 데이터 전송
        List<ChattingDTO> result = socketService.getChattingData(myId);
        List<String> chattingData = new ArrayList<>();
        for(ChattingDTO c : result){
            chattingData.add(socketService.dtoToJson(c));
        }
        socketService.sendMessage(socketService.findReceiverSession(myId, sessionMap, socketSessionAndMemberID), chattingData.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //httpSession이 이미 제거되었기 때문에 memberSessionService 사용 불가
        String myId = socketSessionAndMemberID.get(session.getId());
        socketService.sendLogoutMember(session, sessionMap, socketSessionAndMemberID, myId);
        //소켓 종료
        sessionMap.remove(session.getId());
        socketSessionAndMemberID.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }
}
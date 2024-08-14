package drowGame.drowGame.socket.TypeProc.Matching;

import drowGame.drowGame.socket.TypeProc.Message.MessageProc;
import drowGame.drowGame.socket.data.MatchingInfo;
import drowGame.drowGame.socket.data.SocketRequest;
import drowGame.drowGame.socket.manager.GameManager;
import drowGame.drowGame.socket.manager.SocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchingProc {
    private final GameManager gm;
    private final SocketSessionManager sm;
    private final MessageProc messageProc;

    public void startMatching(WebSocketSession session, int inGameMemberSize) {
        String myId = sm.getMyId(session);

        if (inGameMemberSize == 2)
        {
            // 매칭 큐 인원 충족 시
            if (gm.addMatchingQueue(myId, inGameMemberSize)) {
                SocketRequest socketRequest = new SocketRequest();

                List<WebSocketSession> player_session = new ArrayList<>();
                List<String> members = new ArrayList<>();
                List<String> memebersNickName = new ArrayList<>();

                for (int i = 0; i < inGameMemberSize; i++) {
                    //memberId
                    String player = gm.pollMatchingQueue2Member();
                    members.add(player);

                    //memberSession, memberNickName
                    for (String sessionId : sm.getMemberIdMap().keySet()) {
                        if (sm.getMemberIdMap().get(sessionId).equals(player)) {
                            memebersNickName.add(sm.getMemberNickName(sessionId));
                            player_session.add(sm.getSessionMap().get(sessionId));
                        }
                    }
                }

                //game room 생성
                gm.createGameRoom(player_session, memebersNickName);

                socketRequest.setType("/matching/success");
                MatchingInfo matchingInfo = new MatchingInfo();
                matchingInfo.setRoomUsers(members);
                matchingInfo.setRoomUsersNickName(memebersNickName);

                //매칭 성공 메시지 전송
                int roomId = gm.getGameRoomId(session);
                int count = gm.gameRoomMemberCount(roomId);
                for (int i = 0; i < count; i++) {
                    WebSocketSession wss = gm.getPlayerSession(roomId).get(i);
                    int turn = gm.getTurnList(roomId).get(i);
                    matchingInfo.setYourTurn(turn);
                    socketRequest.setData(matchingInfo);
                    messageProc.sendMessage(wss, messageProc.dtoToJson(socketRequest));
                }
                gm.startBeforeGameTimer(session);
            }
            else
            {
                SocketRequest socketRequest = new SocketRequest();
                List<String> queueMember = gm.getQueueSize(inGameMemberSize);

                int count = queueMember.size();
                for (String s : queueMember) {
                    socketRequest.setType("/matching/userCount_2");
                    socketRequest.setData(count);
                    WebSocketSession receiverSession = sm.findReceiverSession(s);
                    messageProc.sendMessage(receiverSession, messageProc.dtoToJson(socketRequest));
                }
            }
        }
        if (inGameMemberSize == 3) {
            // 매칭 큐 인원 충족 시
            if (gm.addMatchingQueue(myId, inGameMemberSize))
            {
                SocketRequest socketRequest = new SocketRequest();
                List<WebSocketSession> player_session = new ArrayList<>();
                List<String> members = new ArrayList<>();
                List<String> memebersNickName = new ArrayList<>();

                for (int i = 0; i < inGameMemberSize; i++) {
                    //memberId
                    String player = gm.pollMatchingQueue3Member();
                    members.add(player);

                    //memberSession, memberNickName
                    for (String sessionId : sm.getMemberIdMap().keySet()) {
                        if (sm.getMemberIdMap().get(sessionId).equals(player)) {
                            memebersNickName.add(sm.getMemberNickName(sessionId));
                            player_session.add(sm.getSessionMap().get(sessionId));
                        }
                    }
                }

                //game room 생성
                gm.createGameRoom(player_session, memebersNickName);

                socketRequest.setType("m/matching/success");
                MatchingInfo matchingInfo = new MatchingInfo();
                matchingInfo.setRoomUsers(members);
                matchingInfo.setRoomUsersNickName(memebersNickName);

                //매칭 성공 메시지 전송
                int roomId = gm.getGameRoomId(session);
                int count = gm.gameRoomMemberCount(roomId);
                for (int i = 0; i < count; i++) {
                    WebSocketSession wss = gm.getPlayerSession(roomId).get(i);
                    int turn = gm.getTurnList(roomId).get(i);
                    matchingInfo.setYourTurn(turn);
                    socketRequest.setData(matchingInfo);
                    messageProc.sendMessage(wss, messageProc.dtoToJson(socketRequest));
                }
                gm.startBeforeGameTimer(session);
            }
            else
            {
                SocketRequest socketRequest = new SocketRequest();
                List<String> queueMember = gm.getQueueSize(inGameMemberSize);
                int count = queueMember.size();
                for (String s : queueMember) {
                    socketRequest.setType("/matching/userCount_3");
                    socketRequest.setData(count);
                    WebSocketSession receiverSession = sm.findReceiverSession(s);
                    messageProc.sendMessage(receiverSession, messageProc.dtoToJson(socketRequest));
                }
            }
        }
    }
    public void removeMatchingQueue(WebSocketSession session, int inGameMemberSize) {
        String myId = sm.getMyId(session);
        SocketRequest socketRequest = new SocketRequest();

        if(inGameMemberSize == 2){
            gm.removeMatchingQueue(myId, inGameMemberSize);
            List<String> queueMember = gm.getQueueSize(inGameMemberSize);
            int count = queueMember.size();
            for(String s : queueMember){
                socketRequest.setType("/matching/userCount_2");
                socketRequest.setData(count);
                WebSocketSession receiverSession = sm.findReceiverSession(s);
                messageProc.sendMessage(receiverSession, messageProc.dtoToJson(socketRequest));
            }
        }
        if(inGameMemberSize == 3){
            gm.removeMatchingQueue(myId, inGameMemberSize);
            List<String> queueMember = gm.getQueueSize(inGameMemberSize);
            int count = queueMember.size();
            for(String s : queueMember){
                socketRequest.setType("/matching/userCount_3");
                socketRequest.setData(count);
                WebSocketSession receiverSession = sm.findReceiverSession(s);
                messageProc.sendMessage(receiverSession, messageProc.dtoToJson(socketRequest));
            }
        }
    }
}

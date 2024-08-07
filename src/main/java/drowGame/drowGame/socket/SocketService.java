package drowGame.drowGame.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.*;
import drowGame.drowGame.entity.ChattingEntity;
import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.FriendId;
import drowGame.drowGame.entity.QuizEntity;
import drowGame.drowGame.repository.ChattingRepository;
import drowGame.drowGame.repository.FriendRepository;
import drowGame.drowGame.repository.QuizRepository;
import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.socket.data.*;
import drowGame.drowGame.socket.manager.GameManager;
import drowGame.drowGame.socket.manager.SocketSessionManager;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SocketService {
    
    //나중에 정리 필요
    private final SocketSessionManager sm;
    private final GameManager gm;
    private final MemberSessionService memberSessionService;
    private final FriendRepository friendRepository;
    private final ChattingRepository chattingRepository;

    public void addSessionInfo(WebSocketSession session) {
        sm.addSessionMap(session.getId(), session);
        sm.addMemberIdMap(session.getId(), memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId")));
        sm.addMemberNickNameMap(session.getId(), memberSessionService.getMemberNickName((String) session.getAttributes().get("httpSessionId")));
    }
    public void removeSessionInfo(WebSocketSession session) {
        sm.removeSessionMap(session);
        sm.removeMemberIdMap(session);
        sm.removeMemberNickNameMap(session);
    }
    public WebSocketSession duplicateLoginCheck(WebSocketSession session){
        String myId = memberSessionService.getMemberId((String) session.getAttributes().get("httpSessionId"));
        for(String s : sm.getMemberIdMap().keySet()){
            if(myId.equals(sm.getMemberIdMap().get(s))){
                Data data = new Data();
                data.setType("duplicateLogin");
                sendMessage(sm.getSessionMap().get(s), dtoToJson(data));
                return sm.getSessionMap().get(s); //중복 로그인
            }
        }
        return null;
    }
    //친구 정보 전송
    public void sendFriendInfo(WebSocketSession session){
        String myId = sm.getMyId(session);

        //friend list 획득
        List<FriendDTO> friendDTOList = getFriendList(myId);

        List<String> dataList = new ArrayList<>();
        for(FriendDTO friend : friendDTOList){
            Data data = new Data();
            if(sm.getMemberIdMap().containsValue(friend.getFriend_id())){
                friend.setStatus("online");
            }
            data.setType("friend");
            data.setData(friend);
            dataList.add(dtoToJson(data));
        }
        if(!dataList.isEmpty()){
            sendMessage(findReceiverSession(myId), dataList.toString());
        }
    }
    public WebSocketSession findReceiverSession(String receiver){
        for(String key : sm.getSessionMap().keySet()){
            String memberId = sm.getMemberIdMap().get(key);
            if(receiver.equals(memberId)){
                return sm.getSessionMap().get(key);
            }
        }
        return null;
    }
    public void sendLogoutMember(WebSocketSession session) {
        String myId = sm.getMyId(session);
        String nick_name = sm.getMemberNickName(session.getId());

        // socket sessionMap 순회
        for(String memberKey : sm.getSessionMap().keySet()){
            WebSocketSession wss = sm.getSessionMap().get(memberKey);

            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            String memberId = sm.getMemberId(memberKey);
            if(!myId.equals(memberId)){
                Data data = new Data();
                data.setType("logout");
                data.setData(myId);
                sendMessage(wss, dtoToJson(data));
            }
        }

        //게임중 일 경우
        if (gm.isDuringGame(session)){
            SocketRequest sr = new SocketRequest();
            sr.setType("leaveMember");
            sr.setData(nick_name);
            sendMessageSameRoom(1, session, sr);
            gm.leaveGameProc(session);
        }
    }
    public void sendLoginMemberList(WebSocketSession session) {
        String myId = sm.getMyId(session);

        // socket sessionMap 순회
        for(String loginMemberKey : sm.getSessionMap().keySet()){
            WebSocketSession wss = sm.getSessionMap().get(loginMemberKey);

            // socket sessionMap 에 등록 된 member 중 자기 자신 제외
            if(!myId.equals(sm.getMemberId(loginMemberKey))){

                // socket sessionMap 순회
                for (String membersKey : sm.getSessionMap().keySet()) {

                    // ex) ID : 123 인 사람에게 123의 아이디를 제외하고 전송
                    // List 로 바꿔 보내기
                    if (!sm.getMemberId(loginMemberKey).equals(sm.getMemberId(membersKey))) {
                        Data data = new Data();
                        data.setType("login");
                        data.setData(sm.getMemberId(membersKey));
                        sendMessage(wss, dtoToJson(data));
                    }
                }
            }else{ // 자기 자신에게 전송
                for(String membersKey : sm.getSessionMap().keySet()){
                    // 자기 자신 ID 제외 전송
                    // List 로 바꿔 보내기
                    if (!myId.equals(sm.getMemberId(membersKey))){
                        Data data = new Data();
                        data.setType("login");
                        data.setData(sm.getMemberId(membersKey));
                        sendMessage(wss, dtoToJson(data));
                    }else{
                        Data data = new Data();
                        data.setType("myId");
                        data.setData(myId);
                        sendMessage(wss, dtoToJson(data));
                        data.setType("myNickName");
                        data.setData(sm.getMemberNickName(membersKey));
                        sendMessage(wss, dtoToJson(data));
                    }
                }
            }
        }
    }
    public List<FriendDTO> getFriendList(String myId) {
        List<FriendEntity> friendList = friendRepository.findFriendList(myId);
        List<FriendDTO> friendDTOList = new ArrayList<>();
        for (FriendEntity f : friendList){
            FriendDTO friendDTO = new FriendDTO();
            friendDTO.setMember_id(f.getId().getMember_id());
            friendDTO.setFriend_id(f.getId().getFriend_id());
            friendDTO.setFriend_nick_name(f.getFriend_nick_name());
            friendDTOList.add(friendDTO);
        }
        return friendDTOList;
    }
    public void sendMessage(WebSocketSession wss, String message){
        try{
            if(wss != null){
                wss.sendMessage(new TextMessage(message));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Transactional
    public void sendChatting(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        ChattingDTO chatting = socketRequest.typeChatting(socketRequest);
        chatting.setSender(myId);
        ChattingEntity chattingEntity = new ChattingEntity(chatting);
        ChattingDTO saveResult = new ChattingDTO(chattingRepository.chatContentSave(chattingEntity));
        saveResult.setType("sendMessage");

        WebSocketSession[] sessions = new WebSocketSession[2];
        sessions[0] = findReceiverSession(saveResult.getReceiver());
        sessions[1] = findReceiverSession(myId);
        for(WebSocketSession wss : sessions){
            if(wss != null){
                sendMessage(wss, dtoToJson(saveResult));
            }
        }
    }
    @Transactional
    public void addFriend(SocketRequest socketRequest, WebSocketSession session) {
        String myId = sm.getMyId(session);

        AddFriendResponse addFriendResponse = socketRequest.typeAddFriendResponse(socketRequest);

        WebSocketSession receiverSession = findReceiverSession(addFriendResponse.getReceiver());

        if(addFriendResponse.isResponse()){
            FriendId friendId = new FriendId();
            FriendId friendId1 = new FriendId();
            FriendEntity friendEntity = new FriendEntity();
            FriendEntity friendEntity1 = new FriendEntity();

            friendId.setMember_id(myId);
            friendId.setFriend_id(addFriendResponse.getReceiver());
            friendEntity.setId(friendId);
            friendEntity.setFriend_nick_name(sm.findByIdMemberNickName(addFriendResponse.getReceiver()));

            friendId1.setMember_id(addFriendResponse.getReceiver());
            friendId1.setFriend_id(myId);
            friendEntity1.setId(friendId1);
            friendEntity1.setFriend_nick_name(sm.findByIdMemberNickName(myId));

            friendRepository.addFriend(friendEntity, friendEntity1);

            sendFriendInfo(session);
            sendFriendInfo(receiverSession);
        }
    }
    public String dtoToJson(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
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
    public void addFriendRequest(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);

        AddFriend addFriend = socketRequest.typeAddFriend(socketRequest);
        addFriend.setSender(myId);
        socketRequest.setData(addFriend);

        WebSocketSession receiverSession = findReceiverSession(addFriend.getReceiver());
        sendMessage(receiverSession, dtoToJson(socketRequest));
    }
    public void startMatching(WebSocketSession session, SocketRequest socketRequest, int inGameMemberSize){
        String myId = sm.getMyId(session);

        if(inGameMemberSize == 2){
            // 매칭 큐 인원 충족 시
            if(gm.addMatchingQueue(myId, inGameMemberSize)){
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

                MatchingInfo matchingInfo = new MatchingInfo();
                matchingInfo.setRoomUsers(members);
                matchingInfo.setRoomUsersNickName(memebersNickName);
                matchingInfo.setResponse("success");

                //매칭 성공 메시지 전송
                int roomId = gm.getGameRoomId(session);
                int count = gm.gameRoomMemberCount(roomId);
                for (int i = 0; i<count; i++){
                    WebSocketSession wss = gm.getPlayerSession(roomId).get(i);
                    int turn = gm.getTurnList(roomId).get(i);
                    matchingInfo.setYourTurn(turn);
                    socketRequest.setData(matchingInfo);
                    sendMessage(wss, dtoToJson(socketRequest));
                }
                gm.startBeforeGameTimer(session);
            }else {
                List<String> queueMember = gm.getQueueSize(inGameMemberSize);
                int count = queueMember.size();
                for(String s : queueMember){
                    WebSocketSession receiverSession = findReceiverSession(s);
                    String matchingUserCount = "{\"matchingUserCount_2\" : \"" + count + "\"}";
                    sendMessage(receiverSession, matchingUserCount);
                }
            }
        }
        ////////////////////////////////////여기 수정중////////////////////////////////
        if (inGameMemberSize == 3){
            // 매칭 큐 인원 충족 시
            if(gm.addMatchingQueue(myId, inGameMemberSize)) {

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

                MatchingInfo matchingInfo = new MatchingInfo();
                matchingInfo.setRoomUsers(members);
                matchingInfo.setRoomUsersNickName(memebersNickName);
                matchingInfo.setResponse("success");

                //매칭 성공 메시지 전송
                int roomId = gm.getGameRoomId(session);
                int count = gm.gameRoomMemberCount(roomId);
                for (int i = 0; i<count; i++){
                    WebSocketSession wss = gm.getPlayerSession(roomId).get(i);
                    int turn = gm.getTurnList(roomId).get(i);
                    matchingInfo.setYourTurn(turn);
                    socketRequest.setData(matchingInfo);
                    sendMessage(wss, dtoToJson(socketRequest));
                }
                gm.startBeforeGameTimer(session);
            }else {
                List<String> queueMember = gm.getQueueSize(inGameMemberSize);
                int count = queueMember.size();
                for(String s : queueMember){
                    WebSocketSession receiverSession = findReceiverSession(s);
                    String matchingUserCount = "{\"matchingUserCount_3\" : \"" + count + "\"}";
                    sendMessage(receiverSession, matchingUserCount);
                }
            }
        }
    }
    public void gameStart(WebSocketSession session){
        gm.startGameRoundTimer(session);
    }
    public void startRound(WebSocketSession session) {
        gm.startGameRoundTimer(session);
    }
    public void sendMessageSameRoom(int num, WebSocketSession session, SocketRequest socketRequest) {
        int roomId = gm.getGameRoomId(session);
        if (num == 0) {
            for (WebSocketSession wss : gm.getPlayerSession(roomId)) {
                sendMessage(wss, dtoToJson(socketRequest));
            }
        }
        if (num == 1) {
            for (WebSocketSession wss : gm.getPlayerSession(roomId)){
                if(!session.equals(wss)){
                    sendMessage(wss, dtoToJson(socketRequest));
                }
            }
        }
    }
    public void removeMatchingQueue(WebSocketSession session, int inGameMemberSize) {
        String myId = sm.getMyId(session);
        if(inGameMemberSize == 2){
            gm.removeMatchingQueue(myId, inGameMemberSize);
            List<String> queueMember = gm.getQueueSize(inGameMemberSize);
            int count = queueMember.size();
            for(String s : queueMember){
                WebSocketSession receiverSession = findReceiverSession(s);
                String matchingUserCount = "{\"matchingUserCount_2\" : \"" + count + "\"}";
                sendMessage(receiverSession, matchingUserCount);
            }
        }
        if(inGameMemberSize == 3){
            gm.removeMatchingQueue(myId, inGameMemberSize);
            List<String> queueMember = gm.getQueueSize(inGameMemberSize);
            int count = queueMember.size();
            for(String s : queueMember){
                WebSocketSession receiverSession = findReceiverSession(s);
                String matchingUserCount = "{\"matchingUserCount_3\" : \"" + count + "\"}";
                sendMessage(receiverSession, matchingUserCount);
            }
        }

    }

    public void setRequest1(SocketRequest socketRequest, String myId, WebSocketSession session) {
        Request1 result = socketRequest.typeRequest1(socketRequest);
        result.setSender(myId);
        socketRequest.setData(result);
        sendMessageSameRoom(0, session, socketRequest);
    }


    public void answerCheck(WebSocketSession session, Request1 result) {
        gm.answerCheck(session, result.getAnswer(), result.getTimeCount());
    }

    public void ttabong(WebSocketSession session, String nick_name) {
        gm.ttabong(session, nick_name);
    }
}

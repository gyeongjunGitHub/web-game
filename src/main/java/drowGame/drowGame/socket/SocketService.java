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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
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
    private final QuizRepository quizRepository;

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
    public void sendChattingData(WebSocketSession session){
        String myId = sm.getMyId(session);
        List<ChattingDTO> result = getChattingData(myId);
        List<String> chattingData = new ArrayList<>();
        for(ChattingDTO c : result){
            chattingData.add(dtoToJson(c));
        }
        if(!chattingData.isEmpty()){
            sendMessage(findReceiverSession(myId), chattingData.toString());
        }
    }
    public void sendLogoutMember(WebSocketSession session) {
        String myId = sm.getMyId(session);
        // socket sessionMap 순회
        for(String memberKey : sm.getSessionMap().keySet()){
            WebSocketSession wss = sm.getSessionMap().get(memberKey);
            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            String memberId = sm.getMemberId(memberKey);
            if(!myId.equals(memberId)){
                for (String membersKey : sm.getSessionMap().keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
                    if (!memberId.equals(sm.getMemberId(membersKey))) {
                        Data data = new Data();
                        data.setType("logout");
                        data.setData(sm.getMemberId(membersKey));
                        sendMessage(wss, dtoToJson(data));
                    }
                }
            }
        }
    }
    public void sendLoginMemberList(WebSocketSession session) {
        String myId = sm.getMyId(session);
        // socket sessionMap 순회
        for(String loginMemberKey : sm.getSessionMap().keySet()){
            WebSocketSession wss = sm.getSessionMap().get(loginMemberKey);

            // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
            if(!myId.equals(sm.getMemberId(loginMemberKey))){
                for (String membersKey : sm.getSessionMap().keySet()) {
                    // 메시지를 받는 사람 ID와 보내려는 member ID가 동일하지 않으면 전송
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
            wss.sendMessage(new TextMessage(message));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Transactional
    public void sendChatting(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        ChattingDTO chattingDTO = new ChattingDTO();
        chattingDTO.setSender(myId);
        chattingDTO.setReceiver(socketRequest.getReceiver());
        chattingDTO.setContent(socketRequest.getData());
        ChattingEntity chattingEntity = new ChattingEntity(chattingDTO);

        ChattingDTO saveResult = new ChattingDTO(chattingRepository.chatContentSave(chattingEntity));
        saveResult.setRequest("sendMessage");

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
    public void addFriend(SocketRequest socketRequest, String myId) {
        if(Boolean.parseBoolean(socketRequest.getData())){
            FriendId friendId = new FriendId();
            FriendId friendId1 = new FriendId();
            FriendEntity friendEntity = new FriendEntity();
            FriendEntity friendEntity1 = new FriendEntity();

            friendId.setMember_id(myId);
            friendId.setFriend_id(socketRequest.getReceiver());
            friendEntity.setId(friendId);
            friendEntity.setFriend_nick_name(sm.findByIdMemberNickName(socketRequest.getReceiver()));

            friendId1.setMember_id(socketRequest.getReceiver());
            friendId1.setFriend_id(myId);
            friendEntity1.setId(friendId1);
            friendEntity1.setFriend_nick_name(sm.findByIdMemberNickName(myId));

            friendRepository.addFriend(friendEntity, friendEntity1);
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
    public List<ChattingDTO> getChattingData(String myId) {
        List<ChattingEntity> chattingData = chattingRepository.getChattingData(myId);
        List<ChattingDTO> chattingDTOList = new ArrayList<>();
        for(ChattingEntity c : chattingData){
            ChattingDTO chattingDTO = new ChattingDTO(c);
            chattingDTOList.add(chattingDTO);
        }
        return chattingDTOList;
    }
    public QuizDTO getQuiz() {
        int min = 1;
        int max = 7955;
        int randomQuizNumber = ThreadLocalRandom.current().nextInt(min, max + 1);

        QuizEntity quiz = quizRepository.getQuiz(randomQuizNumber);

        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setNum(quiz.getNum());
        quizDTO.setQuiz(quiz.getQuiz());
        quizDTO.setAnswer(quiz.getAnswer());
        return quizDTO;
    }
    public SocketRequest socketRequestMapping(String msg) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SocketRequest socketRequest = new SocketRequest();
        return objectMapper.readValue(msg, SocketRequest.class);
    }
    public void addFriendRequest(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        socketRequest.setSender(myId);
        WebSocketSession receiverSession = findReceiverSession(socketRequest.getReceiver());
        sendMessage(receiverSession, dtoToJson(socketRequest));
    }
    public void sendFriendList(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        //친구 목록 전송
        List<FriendDTO> friendDTOList = getFriendList(myId);
        List<FriendDTO> receiverFriendDTOList = getFriendList(socketRequest.getReceiver());
        List<String> friendList = new ArrayList<>();
        List<String> receiverFriendList = new ArrayList<>();
        //친구 리스트 순회
        for (FriendDTO f : friendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(sm.getMemberIdMap().containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            friendList.add(dtoToJson(f));
        }
        for (FriendDTO f : receiverFriendDTOList) {
            //친구 ID가 socketSessionAndMemberID안에 존재한다면
            if(sm.getMemberIdMap().containsValue(f.getFriend_id())){
                f.setStatus("online");
            }
            receiverFriendList.add(dtoToJson(f));
        }
        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(myId), friendList.toString());
        }
        if(!friendList.isEmpty()){
            sendMessage(findReceiverSession(socketRequest.getReceiver()), receiverFriendList.toString());
        }
    }
    public void startMatching(WebSocketSession session, SocketRequest socketRequest){
        String myId = sm.getMyId(session);
        int inGameMemberSize = 2;

        // 매칭 큐 인원 충족 시
        if(gm.addMatchingQueue(myId)){
            List<WebSocketSession> player_session = new ArrayList<>();
            List<String> members = new ArrayList<>();
            List<String> memebersNickName = new ArrayList<>();

            for (int i = 0; i < inGameMemberSize; i++) {
                //memberId
                String player = gm.pollMatchingQueue();
                members.add(player);

                //memberSession
                for (String sessionId : sm.getMemberIdMap().keySet()) {
                    if (sm.getMemberIdMap().get(sessionId).equals(player)) {
                        memebersNickName.add(sm.getMemberNickName(sessionId));
                        player_session.add(sm.getSessionMap().get(sessionId));
                    }
                }
            }
            gm.createGameRoom(player_session);
            socketRequest.setRoomUsers(members);
            socketRequest.setRoomUsersNickName(memebersNickName);
            socketRequest.setResponse("success");

            for(WebSocketSession wss : player_session){
                socketRequest.setYourTurn(gm.getTurn(wss));
                sendMessage(wss, dtoToJson(socketRequest));
            }
        }
    }
    public void nextTurn(WebSocketSession session, SocketRequest socketRequest){
        int turn = Integer.parseInt(socketRequest.getData()) + 1;
        if(turn > 2){ turn = 1; } //사이클 종료
        QuizDTO quiz = getQuiz();
        socketRequest.setAnswer(quiz.getAnswer());
        socketRequest.setQuiz(quiz.getQuiz());
        socketRequest.setYourTurn(turn);
        sendMessageSameRoom(0, session, socketRequest);
    }
    public void gameStart(WebSocketSession session, SocketRequest socketRequest){
        if(gm.getTurn(session) == 1){
            QuizDTO quiz = getQuiz();
            socketRequest.setAnswer(quiz.getAnswer());
            socketRequest.setQuiz(quiz.getQuiz());
            socketRequest.setYourTurn(1);
            sendMessageSameRoom(0, session, socketRequest);
        }
    }
    public void sendMessageSameRoom(int num, WebSocketSession session, SocketRequest socketRequest) {
        if (num == 0) {
            for (WebSocketSession wss : gm.getSameRoomMemberSession(session)) {
                sendMessage(wss, dtoToJson(socketRequest));
            }
        }
        if (num == 1) {
            for (WebSocketSession wss : gm.getSameRoomMemberSession(session)){
                if(!session.equals(wss)){
                    sendMessage(wss, dtoToJson(socketRequest));
                }
            }
        }
    }
    public String getMyId(WebSocketSession session){
        return sm.getMyId(session);
    }
    public void removeMatchingQueue(WebSocketSession session) {
        String myId = sm.getMyId(session);
        gm.removeMatchingQueue(myId);
    }
    public void removeGameRoom(WebSocketSession session) {

        //게임중 일 경우
        if (gm.isDuringGame(session)){
            int roomId = gm.getGameRoomId(session);
            gm.removeSessionGameRoom(session);
            if (gm.getRoomMemberCount(roomId) == 1) {
                SocketRequest socketRequest = new SocketRequest();
                socketRequest.setRequest("leaveOtherMember");
                for (WebSocketSession wss : gm.getSameRoomMemberSession(roomId)) {
                    sendMessage(wss, dtoToJson(socketRequest));
                    gm.removeSessionGameRoom(wss);
                }
            }
        }
    }
}

package drowGame.drowGame.socket.TypeProc.Member;

import drowGame.drowGame.dto.FriendDTO;
import drowGame.drowGame.entity.FriendEntity;
import drowGame.drowGame.entity.FriendId;
import drowGame.drowGame.repository.FriendRepository;
import drowGame.drowGame.service.MemberSessionService;
import drowGame.drowGame.socket.TypeProc.Message.MessageProc;
import drowGame.drowGame.socket.data.AddFriend;
import drowGame.drowGame.socket.data.AddFriendResponse;
import drowGame.drowGame.socket.data.Data;
import drowGame.drowGame.socket.data.SocketRequest;
import drowGame.drowGame.socket.manager.GameManager;
import drowGame.drowGame.socket.manager.SocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SocketMemberProc {
    private final SocketSessionManager sm;
    private final MessageProc messageProc;
    private final FriendRepository friendRepository;
    private final MemberSessionService memberSessionService;
    private final GameManager gm;
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
                        data.setType("/member/login");
                        data.setData(sm.getMemberId(membersKey));
                        messageProc.sendMessage(wss, messageProc.dtoToJson(data));
                    }
                }
            }else{ // 자기 자신에게 전송
                for(String membersKey : sm.getSessionMap().keySet()){
                    // 자기 자신 ID 제외 전송
                    // List 로 바꿔 보내기
                    if (!myId.equals(sm.getMemberId(membersKey))){
                        Data data = new Data();
                        data.setType("/member/login");
                        data.setData(sm.getMemberId(membersKey));
                        messageProc.sendMessage(wss, messageProc.dtoToJson(data));
                    }else{
                        Data data = new Data();
                        data.setType("/member/myId");
                        data.setData(myId);
                        messageProc.sendMessage(wss, messageProc.dtoToJson(data));
                        data.setType("/member/myNickName");
                        data.setData(sm.getMemberNickName(membersKey));
                        messageProc.sendMessage(wss, messageProc.dtoToJson(data));
                    }
                }
            }
        }
    }
    public void sendLogoutMember(WebSocketSession session) {
        String myId = sm.getMyId(session);
        String nick_name = sm.getMemberNickName(session.getId());

        if(myId != null){
            // socket sessionMap 순회
            for(String memberKey : sm.getSessionMap().keySet()){
                WebSocketSession wss = sm.getSessionMap().get(memberKey);

                // 소캣에 등록된 Member 아이디와 myId가 같지 않으면 (자기 자신 제외)
                String memberId = sm.getMemberId(memberKey);
                if(!myId.equals(memberId)){
                    Data data = new Data();
                    data.setType("/member/logout");
                    data.setData(myId);
                    messageProc.sendMessage(wss, messageProc.dtoToJson(data));
                }
            }
        }

        //게임중 일 경우
        if (gm.isDuringGame(session)){
            SocketRequest sr = new SocketRequest();
            sr.setType("/game/leaveMember");
            sr.setData(nick_name);
            sendMessageSameRoom(1, session, sr);
            gm.leaveGameProc(session);
        }
    }
    public void sendMessageSameRoom(int num, WebSocketSession session, SocketRequest socketRequest) {
        int roomId = gm.getGameRoomId(session);

        //자신 포함
        if (num == 0) {
            for (WebSocketSession wss : gm.getPlayerSession(roomId)) {
                messageProc.sendMessage(wss, messageProc.dtoToJson(socketRequest));
            }
        }
        if (num == 1) {
            for (WebSocketSession wss : gm.getPlayerSession(roomId)){
                if(!session.equals(wss)){
                    messageProc.sendMessage(wss, messageProc.dtoToJson(socketRequest));
                }
            }
        }
    }
    public void addFriendRequest(WebSocketSession session, SocketRequest socketRequest) {
        String myId = sm.getMyId(session);

        AddFriend addFriend = socketRequest.typeAddFriend(socketRequest);
        addFriend.setSender(myId);
        socketRequest.setData(addFriend);

        WebSocketSession receiverSession = sm.findReceiverSession(addFriend.getReceiver());
        messageProc.sendMessage(receiverSession, messageProc.dtoToJson(socketRequest));
    }
    @Transactional
    public void addFriend(SocketRequest socketRequest, WebSocketSession session) {
        String myId = sm.getMyId(session);

        AddFriendResponse addFriendResponse = socketRequest.typeAddFriendResponse(socketRequest);

        WebSocketSession receiverSession = sm.findReceiverSession(addFriendResponse.getReceiver());

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
            data.setType("/friend");
            data.setData(friend);
            dataList.add(messageProc.dtoToJson(data));
        }
        if(!dataList.isEmpty()){
            messageProc.sendMessage(sm.findReceiverSession(myId), dataList.toString());
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
}

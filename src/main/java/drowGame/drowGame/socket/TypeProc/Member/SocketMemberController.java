package drowGame.drowGame.socket.TypeProc.Member;

import drowGame.drowGame.socket.data.SocketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class SocketMemberController {
    private final SocketMemberProc socketMemberProc;

    public void controller(WebSocketSession session, String request, SocketRequest socketRequest) {
        switch (request){
            case "addFriendRequest":
                socketMemberProc.addFriendRequest(session, socketRequest);
                break;
            case "addFriendResponse":
                socketMemberProc.addFriend(socketRequest, session);
                break;
            case "addSession":
                socketMemberProc.addSessionInfo(session);
                break;
            case "sendLoginMemberList":
                socketMemberProc.sendLoginMemberList(session);
                break;
            case "sendFriendInfo":
                socketMemberProc.sendFriendInfo(session);
                break;
            case "sendLogoutMember":
                socketMemberProc.sendLogoutMember(session);
                break;
            case "removeSessionInfo":
                socketMemberProc.removeSessionInfo(session);
                break;
        }
    }
}

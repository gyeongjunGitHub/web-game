package drowGame.drowGame.dto;

import drowGame.drowGame.entity.MemberEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private String id;
    private String password;
    private String nick_name;
    private String name;
    private String gender;
    private String email;
    private int ranking_point;
    private int game_point;
    private String role;
    private ProfilePictureDTO profilePictureDTO;
    public MemberDTO(){}
    public MemberDTO(MemberEntity memberEntity){
        this.id = memberEntity.getId();
        this.password = memberEntity.getPassword();
        this.nick_name = memberEntity.getNick_name();
        this.name = memberEntity.getName();
        this.gender = memberEntity.getGender();
        this.email = memberEntity.getEmail();
        this.ranking_point = memberEntity.getRanking_point();
        this.game_point = memberEntity.getGame_point();
        this.role = memberEntity.getRole();
    }
}

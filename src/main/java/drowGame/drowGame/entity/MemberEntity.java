package drowGame.drowGame.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
public class MemberEntity {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "password")
    private String password;
    @Column(name = "nick_name")
    private String nick_name;
    @Column(name = "name")
    private String name;
    @Column(name = "gender")
    private String gender;
    @Column(name = "email")
    private String email;
    @Column(name = "ranking_point")
    private int ranking_point;
    @Column(name = "game_point")
    private int game_point;
    @Column(name = "role")
    private String role;

}

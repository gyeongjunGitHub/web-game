package drowGame.drowGame.entity;

import drowGame.drowGame.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

@Entity
@Getter
@Setter
@Table(name = "profile_picture")
public class ProfilePictureEntity extends Time{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long file_id;

    @Column(name = "original_file_name")
    private String original_file_name;

    @Column(name = "stored_file_name")
    private String stored_file_name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private MemberEntity memberEntity;
}

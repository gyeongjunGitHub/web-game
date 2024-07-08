package drowGame.drowGame.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    public static ProfilePictureEntity getBasicFile(MemberEntity memberEntity){
        ProfilePictureEntity profilePictureEntity = new ProfilePictureEntity();
        profilePictureEntity.setOriginal_file_name("기본.png");
        profilePictureEntity.setStored_file_name("1720427482598_기본.png");
        profilePictureEntity.setMemberEntity(memberEntity);
        return profilePictureEntity;
    }
}

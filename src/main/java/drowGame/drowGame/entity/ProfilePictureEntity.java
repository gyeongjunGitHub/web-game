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

    @Column(name = "member_id")
    private String member_id;

}

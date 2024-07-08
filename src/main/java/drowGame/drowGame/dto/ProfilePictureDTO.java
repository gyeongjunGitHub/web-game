package drowGame.drowGame.dto;

import drowGame.drowGame.entity.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfilePictureDTO {
    private Long file_id;
    private String original_file_name;
    private String stored_file_name;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private MemberEntity memberEntity;
}
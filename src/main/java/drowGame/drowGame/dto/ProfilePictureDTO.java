package drowGame.drowGame.dto;

import drowGame.drowGame.entity.MemberEntity;
import drowGame.drowGame.entity.ProfilePictureEntity;
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
    private MemberDTO memberDTO;

    public ProfilePictureDTO(){}

    public ProfilePictureDTO(ProfilePictureEntity profilePictureEntity) {
        this.file_id = profilePictureEntity.getFile_id();
        this.original_file_name = profilePictureEntity.getOriginal_file_name();
        this.stored_file_name = profilePictureEntity.getStored_file_name();
        this.created_time = profilePictureEntity.getCreated_time();
        this.updated_time = profilePictureEntity.getUpdated_time();
    }

}

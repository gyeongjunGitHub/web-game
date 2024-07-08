package drowGame.drowGame.dto;

import lombok.Getter;

@Getter
public class BasicFileName {
    private String original_file_name = "기본.png";
    private String stored_file_name = "1720427482598_기본.png";

    public static BasicFileName getBasicFileName(){
        return new BasicFileName();
    }
}

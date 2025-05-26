package at.fhtw.ctfbackend.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitFlagRequest {

    private String challengeId;
    private String flag;

}

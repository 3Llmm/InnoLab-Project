package at.fhtw.ctfbackend.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceBody {
    private ConfluenceView view;
}

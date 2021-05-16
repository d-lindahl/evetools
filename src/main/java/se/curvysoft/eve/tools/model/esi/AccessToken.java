package se.curvysoft.eve.tools.model.esi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AccessToken implements Serializable {
    private static final long serialVersionUID = 6617779786802703321L;

    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("token_type")
    String tokenType;
    @JsonProperty("expires_in")
    String expiresIn;
    @JsonProperty("refresh_token")
    String refreshToken;
}

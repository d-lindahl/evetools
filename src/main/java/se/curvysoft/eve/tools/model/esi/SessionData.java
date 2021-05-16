package se.curvysoft.eve.tools.model.esi;

import lombok.Data;

import java.io.Serializable;

@Data
public class SessionData implements Serializable {
    private static final long serialVersionUID = 8622942122354243636L;

    AccessToken accessToken;
    CharacterData characterData;
}

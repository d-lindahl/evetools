package se.curvysoft.eve.tools.model.esi;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CharacterData implements Serializable {
    private static final long serialVersionUID = 1764844803292063040L;

    String characterName;
    Integer characterId;
    LocalDateTime expiryDate;
}

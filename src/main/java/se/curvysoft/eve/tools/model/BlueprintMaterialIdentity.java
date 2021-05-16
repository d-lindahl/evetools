package se.curvysoft.eve.tools.model;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;

@Data
public class BlueprintMaterialIdentity implements Serializable {
    @Column(name = "typeID")
    private int itemId;
    @Column(name = "materialTypeID")
    private int materialId;
    @Column(name = "activityID")
    private int activityId;
}

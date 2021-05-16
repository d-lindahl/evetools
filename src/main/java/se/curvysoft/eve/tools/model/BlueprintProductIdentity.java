package se.curvysoft.eve.tools.model;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;

@Data
public class BlueprintProductIdentity implements Serializable {
    @Column(name = "typeID")
    private int itemId;
    @Column(name = "productTypeID")
    private int productId;
    @Column(name = "activityID")
    private int activityId;
}

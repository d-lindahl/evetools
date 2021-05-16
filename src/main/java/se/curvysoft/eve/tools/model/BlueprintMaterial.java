package se.curvysoft.eve.tools.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Data
@Entity
@Table(name = "industryActivityMaterials")
public class BlueprintMaterial {
    @EmbeddedId
    private BlueprintMaterialIdentity blueprintMaterialIdentity;
    @ManyToOne
    @JoinColumn(name = "typeID", updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item blueprint;
    @ManyToOne
    @JoinColumn(name = "materialTypeID", updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item material;
    private Integer quantity;
    private Integer activityId;
}

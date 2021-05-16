package se.curvysoft.eve.tools.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Data
@Entity
@Table(name = "industryActivityProducts")
public class BlueprintProduct {
    @EmbeddedId
    private BlueprintProductIdentity blueprintProductIdentity;
    @ManyToOne
    @JoinColumn(name = "typeID", updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item blueprint;
    @ManyToOne
    @JoinColumn(name = "productTypeID", updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item product;
    private Integer quantity;
    private Integer activityId;
}

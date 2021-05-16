package se.curvysoft.eve.tools.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Data
@Entity
@Table(name = "invTypeMaterials")
public class ItemMaterial {
    @EmbeddedId
    private MaterialIdentity materialIdentity;
    @ManyToOne
    @JoinColumn(name = "typeID", updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item item;
    @ManyToOne
    @JoinColumn(name = "materialTypeID", updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item material;
    private Integer quantity;
}

package se.curvysoft.eve.tools.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "invTypes")
public class Item {
    @Id
    private int typeID;
    private String typeName;
    @Type(type = "text")
    private String description;
    private Double volume;
    private Integer portionSize;
    private Integer iconID;
    private Integer graphicID;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ItemMaterial> materials;

    public String getIconUrl(int size) {
        if (typeName.endsWith("Blueprint")) {
            return String.format("https://images.evetech.net/types/%s/bpc?tenant=tranquility&size=%d", getTypeID(), size);
        } else {
            return String.format("https://images.evetech.net/types/%s/icon?tenant=tranquility&size=%d", getTypeID(), size);
        }
    }
}

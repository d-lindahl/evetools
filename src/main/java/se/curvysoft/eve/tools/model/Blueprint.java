package se.curvysoft.eve.tools.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "industryBlueprints")
public class Blueprint {
    @Id
    private int typeID;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "typeID")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Item item;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "blueprint")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<BlueprintProduct> products;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "blueprint")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<BlueprintMaterial> allMaterials;

    public BlueprintProduct getProduct() {
        return products.stream().filter(blueprintProduct -> blueprintProduct.getActivityId().equals(1)).findAny().orElse(null);
    }

    public Set<BlueprintMaterial> getMaterials() {
        return allMaterials.stream().filter(blueprintMaterial -> blueprintMaterial.getActivityId().equals(1)).collect(Collectors.toSet());
    }

    public String getIconUrl(boolean bpc, int size) {
        return String.format("https://images.evetech.net/types/%s/bp%s?tenant=tranquility&size=%d",
                getTypeID(), bpc ? "c" : "", size);
    }
}

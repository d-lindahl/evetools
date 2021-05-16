
package se.curvysoft.eve.tools.model.pricecheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceItem {
    @JsonProperty("name")
    public String name;
    @JsonProperty("typeID")
    public Integer typeID;
    @JsonProperty("typeName")
    public String typeName;
    @JsonProperty("typeVolume")
    public Integer typeVolume;
    @JsonProperty("quantity")
    public Integer quantity;
    @JsonProperty("prices")
    public Prices prices;
}

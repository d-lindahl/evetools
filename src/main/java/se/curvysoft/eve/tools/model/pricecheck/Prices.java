
package se.curvysoft.eve.tools.model.pricecheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Prices {
    @JsonProperty("all")
    public Price all;
    @JsonProperty("buy")
    public Price buy;
    @JsonProperty("sell")
    public Price sell;
    @JsonProperty("updated")
    public String updated;
    @JsonProperty("strategy")
    public String strategy;
}

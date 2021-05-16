
package se.curvysoft.eve.tools.model.pricecheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Totals {
    @JsonProperty("buy")
    public Double buy;
    @JsonProperty("sell")
    public Double sell;
    @JsonProperty("volume")
    public Long volume;
}

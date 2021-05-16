
package se.curvysoft.eve.tools.model.pricecheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Price {
    @JsonProperty("avg")
    public Double avg;
    @JsonProperty("max")
    public Double max;
    @JsonProperty("median")
    public Double median;
    @JsonProperty("min")
    public Double min;
    @JsonProperty("percentile")
    public Double percentile;
    @JsonProperty("stddev")
    public Double stddev;
    @JsonProperty("volume")
    public Long volume;
    @JsonProperty("order_count")
    public Long orderCount;
}

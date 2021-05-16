
package se.curvysoft.eve.tools.model.pricecheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Appraisal {
    @JsonProperty("created")
    public Integer created;
    @JsonProperty("kind")
    public String kind;
    @JsonProperty("market_name")
    public String marketName;
    @JsonProperty("totals")
    public Totals totals;
    @JsonProperty("items")
    public List<PriceItem> items = null;
    @JsonProperty("private")
    public Boolean _private;
    @JsonProperty("price_percentage")
    public Integer pricePercentage;
    @JsonProperty("live")
    public Boolean live;
    @JsonProperty("expire_minutes")
    public Integer expireMinutes;
}

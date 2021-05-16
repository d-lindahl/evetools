
package se.curvysoft.eve.tools.model.pricecheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceCheck {
    @JsonProperty("appraisal")
    public Appraisal appraisal;
}

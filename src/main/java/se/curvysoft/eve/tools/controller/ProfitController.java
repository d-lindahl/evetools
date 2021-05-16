package se.curvysoft.eve.tools.controller;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import se.curvysoft.eve.tools.db.ItemRepository;
import se.curvysoft.eve.tools.model.Item;
import se.curvysoft.eve.tools.model.pricecheck.Appraisal;
import se.curvysoft.eve.tools.model.pricecheck.PriceItem;
import se.curvysoft.eve.tools.service.PriceCheckService;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Controller
@RequestMapping("/profit")
public class ProfitController {
    private final PriceCheckService priceCheckService;
    private final ItemRepository itemRepository;
    private final DecimalFormat formatPretty;
    private final DecimalFormat format;

    public ProfitController(PriceCheckService priceCheckService, ItemRepository itemRepository) {
        this.priceCheckService = priceCheckService;
        this.itemRepository = itemRepository;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.UK);
        formatPretty = new DecimalFormat("#,###,###,###,###,##0.00", symbols);
        formatPretty.setRoundingMode(RoundingMode.HALF_UP);
        format = new DecimalFormat("0.00", symbols);
        formatPretty.setRoundingMode(RoundingMode.DOWN);
    }

    @RequestMapping(value = "", method = {RequestMethod.POST, RequestMethod.GET})
    public String calculate(@RequestParam(required = false) String items, @RequestParam(required = false) String market, Model model) {
        if (items != null && items.length() > 0) {
            Appraisal appraisal = priceCheckService.get(items, market);
            List<ProfitControllerResult> results = new ArrayList<>();
            StringBuilder rawData = new StringBuilder();
            for (PriceItem priceItem : appraisal.items) {
                ProfitControllerResult res = new ProfitControllerResult();
                Item item = itemRepository.findFirstByTypeIDEquals(priceItem.typeID);
                res.setItemIconUrl(item.getIconUrl(32));
                res.setItemName(priceItem.getTypeName());
                rawData.append(priceItem.getTypeName()).append("\t");
                res.setItemCount(""+priceItem.getQuantity());
                rawData.append(priceItem.getQuantity()).append("\t");
                double amount = Math.round(priceItem.getPrices().getBuy().getMax() * 100) / 100.0;
                if (priceItem.getTypeName().endsWith("Blueprint")) {
                    amount = 0;
                }
                res.setItemPrice(formatPretty.format(amount));
                rawData.append(format.format(amount)).append("\r\n");
                amount = Math.round(priceItem.getPrices().getBuy().getMax()*priceItem.getQuantity()*100)/100.0;
                if (priceItem.getTypeName().endsWith("Blueprint")) {
                    amount = 0;
                }
                res.setItemTotal(formatPretty.format(amount));
                results.add(res);
            }

            System.out.println(appraisal.items.size() + " items found");
            model.addAttribute("items", items);
            model.addAttribute("results", results);
            model.addAttribute("data", rawData.toString());
            model.addAttribute("totals", formatPretty.format(appraisal.getTotals().getBuy()));
        } else {
            model.addAttribute("greeting", "Let's get started!");
        }
        return "profit";
    }

    @Data
    static private class ProfitControllerResult {
        private String itemIconUrl;
        private String itemName;
        private String itemCount;
        private String itemPrice;
        private String itemTotal;
    }
}
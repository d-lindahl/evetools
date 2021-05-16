package se.curvysoft.eve.tools.controller;

import io.swagger.client.api.CharacterApi;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import se.curvysoft.eve.tools.db.BlueprintRepository;
import se.curvysoft.eve.tools.model.*;
import se.curvysoft.eve.tools.model.esi.GetCharactersCharacterIdBlueprints200Ok;
import se.curvysoft.eve.tools.model.pricecheck.PriceItem;
import se.curvysoft.eve.tools.service.EsiService;
import se.curvysoft.eve.tools.service.PriceCheckService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bpprofit")
@Scope(scopeName = "")
public class BlueprintProfitabilityController {
    private final BlueprintRepository blueprintRepository;
    private final PriceCheckService priceCheckService;
    private final EsiService esiService;

    public BlueprintProfitabilityController(BlueprintRepository blueprintRepository,
                                            PriceCheckService priceCheckService,
                                            EsiService esiService) {
        this.blueprintRepository = blueprintRepository;
        this.priceCheckService = priceCheckService;
        this.esiService = esiService;
    }

    @RequestMapping(value = "", method = {RequestMethod.POST, RequestMethod.GET})
    public String calculate(@RequestParam(required = false) String blueprints, @RequestParam(required = false) String market, Model model) {
        if (esiService.isLoggedIn() && market != null) {
            ApiClient client = esiService.getApiClient();
            CharacterApi characterApi = new CharacterApi(client);
            List<GetCharactersCharacterIdBlueprints200Ok> blueprintAssets =
                    characterApi.getCharactersCharacterIdBlueprints(esiService.getCharacterId(),
                            null, null, null, null);


            Set<Integer> typeIds = blueprintAssets.stream().map(GetCharactersCharacterIdBlueprints200Ok::getTypeId).collect(Collectors.toSet());
            Set<Blueprint> blueprintObjects = blueprintRepository.findAllByTypeIDIn(typeIds);
            Map<Integer, Blueprint> blueprintMap = new HashMap<>();
            for (Blueprint blueprintObject : blueprintObjects) {
                blueprintMap.put(blueprintObject.getTypeID(), blueprintObject);
            }

            Set<Item> itemsToPriceCheck = new HashSet<>();
            for (Blueprint blueprint : blueprintObjects) {
                if (blueprint != null) {
                    BlueprintProduct product = blueprint.getProduct();

                    if (product != null) {
                        itemsToPriceCheck.add(product.getProduct());
                        for (BlueprintMaterial material : blueprint.getMaterials()) {
                            itemsToPriceCheck.add(material.getMaterial());
                        }
                    } else if (blueprint.getProducts() != null && blueprint.getProducts().size() > 0) {
                        product = blueprint.getProducts().get(0);
                        itemsToPriceCheck.add(product.getProduct());
                        for (BlueprintMaterial material : blueprint.getAllMaterials().stream()
                                .filter(blueprintMaterial -> blueprintMaterial.getActivityId().equals(11))
                                .collect(Collectors.toSet())) {
                            itemsToPriceCheck.add(material.getMaterial());
                        }
                    }
                }
            }
            Map<Item, PriceItem> priceMap = priceCheckService.get(itemsToPriceCheck, market);
            List<BlueprintControllerResult> results = new ArrayList<>();
            for (GetCharactersCharacterIdBlueprints200Ok apiBp : blueprintAssets) {
                Blueprint bp = blueprintMap.get(apiBp.getTypeId());
                BlueprintControllerResult res = new BlueprintControllerResult();
                res.setBlueprintId(""+bp.getTypeID());
                res.setBlueprintName(bp.getItem().getTypeName());
                res.setCopy(apiBp.getRuns() > 0);
                res.setMe("" + apiBp.getMaterialEfficiency());
                res.setTe("" + apiBp.getTimeEfficiency());

                BlueprintControllerResult existingRes = results.stream()
                        .filter(result -> result.equals(res))
                        .findAny().orElse(null);

                if (existingRes != null) {
                    if (existingRes.getCopy()) {
                        existingRes.setRuns(existingRes.getRuns() + apiBp.getRuns());
                    }
                    continue;
                }

                res.setRuns(apiBp.getRuns());

                double materialSellPrice = 0d;

                BlueprintProduct product = bp.getProduct();
                Set<BlueprintMaterial> materials = bp.getMaterials();
                if (product == null) {
                    product = bp.getProducts().get(0);
                    materials = bp.getAllMaterials().stream().filter(bpm -> bpm.getActivityId().equals(11)).collect(Collectors.toSet());
                }

                for (BlueprintMaterial material : materials) {
                    materialSellPrice += priceMap.get(material.getMaterial()).getPrices().getBuy().getMax() *
                            Math.ceil(material.getQuantity() * (1 - apiBp.getMaterialEfficiency()/100.0));
                }
                res.setMaterialsSellPrice(String.format(Locale.US, "%,.2f", materialSellPrice));

                res.setBlueprintIconUrl(bp.getIconUrl(apiBp.getQuantity() == -2, 32));
                res.setProductName(product.getProduct().getTypeName());
                res.setProductQuantity(String.valueOf(product.getQuantity()));
                double maxBuyOrder = priceMap.get(product.getProduct()).getPrices().getBuy().getMax() *
                        product.getQuantity();
                res.setProductDirectSellPrice(String.format(Locale.US, "%,.2f", maxBuyOrder));
                res.setProductDirectSellDiff(String.format(Locale.US, "%,.2f", maxBuyOrder - materialSellPrice));
                res.setProductDirectSellDiffPercentage(String.format(Locale.US, "%.0f%%", (maxBuyOrder - materialSellPrice) / materialSellPrice * 100));
                res.setPositiveDirectSellProfit(maxBuyOrder > materialSellPrice);
                double minSellOrder = priceMap.get(product.getProduct()).getPrices().getSell().getMin() *
                        product.getQuantity();
                res.setProductSellOrderPrice(String.format(Locale.US, "%,.2f", minSellOrder));
                res.setProductSellOrderDiff(String.format(Locale.US, "%,.2f", minSellOrder - materialSellPrice));
                res.setProductSellOrderDiffPercentage(String.format(Locale.US, "%.0f%%", (minSellOrder - materialSellPrice) / materialSellPrice * 100));
                res.setPositiveSellOrderProfit(minSellOrder > materialSellPrice);
                results.add(res);
            }

            System.out.println(blueprintObjects.size() + " blueprints found");
            model.addAttribute("blueprints", blueprints);
            model.addAttribute("results", results);
        } else {
            model.addAttribute("greeting", "Let's get started!");
        }
        return "blueprint-profitability";
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static private class BlueprintControllerResult {
        @EqualsAndHashCode.Include
        private String blueprintId;
        private String blueprintName;
        @EqualsAndHashCode.Include
        private Boolean copy;
        private Integer runs;
        @EqualsAndHashCode.Include
        private String me;
        @EqualsAndHashCode.Include
        private String te;
        private String materialsSellPrice;
        private String blueprintIconUrl;
        private String productName;
        private String productQuantity;
        private String productDirectSellPrice;
        private String productDirectSellDiff;
        private String productDirectSellDiffPercentage;
        private Boolean positiveDirectSellProfit;
        private String productSellOrderPrice;
        private String productSellOrderDiff;
        private String productSellOrderDiffPercentage;
        private Boolean positiveSellOrderProfit;


    }
}

package se.curvysoft.eve.tools.controller;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import se.curvysoft.eve.tools.db.ItemRepository;
import se.curvysoft.eve.tools.model.Item;
import se.curvysoft.eve.tools.model.ItemMaterial;
import se.curvysoft.eve.tools.model.pricecheck.PriceItem;
import se.curvysoft.eve.tools.service.PriceCheckService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sell")
public class SellCompressOrReprocessController {
    private final ItemRepository itemRepository;
    private final PriceCheckService priceCheckService;

    public SellCompressOrReprocessController(ItemRepository itemRepository, PriceCheckService priceCheckService) {
        this.itemRepository = itemRepository;
        this.priceCheckService = priceCheckService;
    }

    @RequestMapping(value = "", method = {RequestMethod.POST, RequestMethod.GET})
    public String calculate(@RequestParam(required = false) String rawMaterials, @RequestParam(required = false) Integer efficiency, @RequestParam(required = false) String market, Model model) {
        List<String> skills = new ArrayList<>();
        skills.add("Reprocessing");
        skills.add("Reprocessing Efficiency");
        skills.add("Arkonor Processing");
        skills.add("Bistot Processing");
        skills.add("Crokite Processing");
        skills.add("Dark Ochre Processing");
        skills.add("Gneiss Processing");
        skills.add("Hedbergite Processing");
        skills.add("Hemorphite Processing");
        skills.add("Jaspet Processing");
        skills.add("Kernite Processing");
        skills.add("Mercoxit Processing");
        skills.add("Omber Processing");
        skills.add("Plagioclase Processing");
        skills.add("Plagioclase Processing");
        skills.add("Pyroxers Processing");
        skills.add("Scordite Processing");
        skills.add("Spodumain Processing");
        skills.add("Veldspar Processing");
        skills.add("Scrapmetal Processing");
        skills.add("Ubiquitous Moon Ore Processing");
        skills.add("Common Moon Ore Processing");
        skills.add("Uncommon Moon Ore Processing");
        skills.add("Rare Moon Ore Processing");
        skills.add("Exceptional Moon Ore Processing");
        skills.add("Bezdnacine Processing");
        skills.add("Talassonite Processing");
        skills.add("Rakovene Processing");
        model.addAttribute("skills", skills);
        if (rawMaterials != null && rawMaterials.length() > 0) {
            String[] materials = rawMaterials.split("\n");
            Map<String, Integer> materialCountMap = new LinkedHashMap<>();
            for (String material : materials) {
                String[] parts = material.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                String materialName = parts[0].trim();
                Integer materialCount = Integer.parseInt(parts[1].trim().replaceAll("[\\u00a0\\s]", ""));
                Integer oldCount = materialCountMap.get(materialName);
                if (oldCount == null) {
                    oldCount = 0;
                }
                oldCount += materialCount;
                materialCountMap.put(materialName, oldCount);
            }
            Set<Item> items = itemRepository.findAllByTypeNameIn(materialCountMap.keySet());
            Set<Item> itemsToPriceCheck = new HashSet<>();
            double sumVolume = 0;
            double sumBase = 0;
            double sumCompressed = 0;
            double sumRefined = 0;
            for (Item item : items) {
                if (item.getMaterials() != null && item.getMaterials().size() > 0) {
                    itemsToPriceCheck.add(item);
                    itemsToPriceCheck.addAll(item.getMaterials()
                            .stream()
                            .map(ItemMaterial::getMaterial)
                            .collect(Collectors.toList()));
                    if (!item.getTypeName().toLowerCase().startsWith("compressed ")) {
                        Item compressedItem = itemRepository.findFirstByTypeNameEquals("Compressed " + item.getTypeName());
                        if (compressedItem != null) {
                            itemsToPriceCheck.add(compressedItem);
                        }
                    }
                }
            }
            Map<String, Item> itemToItemNameMap = itemsToPriceCheck.stream().collect(Collectors.toMap(Item::getTypeName, item -> item));
            Map<Item, PriceItem> priceMap = priceCheckService.get(itemsToPriceCheck, market);
            Set<SellCompressOrReprocessResult> results = new HashSet<>();
            for (Map.Entry<String, Integer> entry : materialCountMap.entrySet()) {
                String action = "sell";
                Item item = itemToItemNameMap.get(entry.getKey());
                if (item == null || !itemsToPriceCheck.contains(item)) {
                    continue;
                }
                Integer portionSize = item.getPortionSize();
                SellCompressOrReprocessResult result = new SellCompressOrReprocessResult();
                result.setIcon(item.getIconUrl(32));
                result.setName(item.getTypeName());
                Integer count = entry.getValue();
                result.setCount(count.toString());
                double volume = entry.getValue() * item.getVolume();
                result.setVolume(String.format("%,.1f m3", volume));
                double price;
                double basePrice = 0;
                double compressedPrice;
                boolean isCompressed = item.getTypeName().startsWith("Compressed ");
                if (!isCompressed) {
                    basePrice = priceMap.get(item).getPrices().getBuy().getMax();
                    price = basePrice * count;
                    result.setSellRaw(String.format("%,.2f ISK", price));
                    Item compressedItem = itemToItemNameMap.get("Compressed " + item.getTypeName());
                    PriceItem compressedPriceItem = priceMap.get(compressedItem);
                    compressedPrice = compressedPriceItem.getPrices().getBuy().getMax() *
                            Math.floor(count / (double)item.getPortionSize());
                } else {
                    compressedPrice = priceMap.get(item).getPrices().getBuy().getMax() * count;
                    action = "compress";
                }

                if (compressedPrice > 0) {
                    if (compressedPrice > basePrice) {
                        action = "compress";
                    }
                    price = compressedPrice + ((count % item.getPortionSize()) * basePrice);
                } else {
                    price = 0;
                }
                result.setSellCompressed(String.format("%,.2f ISK", price));
                double finalBasePrice = basePrice;
                System.out.println(item.getTypeName());
                price = item.getMaterials().stream().map(itemMaterial ->
                        ((efficiency / 100.0) *
                                priceMap.get(itemMaterial.getMaterial()).getPrices().getBuy().getMax() *
                                itemMaterial.getQuantity()) * Math.floor(count / (double) portionSize) + ((count % portionSize) *
                                finalBasePrice)).reduce(0d, Double::sum);
                result.setSellRefined(String.format("%,.2f ISK", price));
                if (price > compressedPrice && price > basePrice) {
                    action = "refine";
                }
                result.setAction(action);
                results.add(result);
            }
            model.addAttribute("rawMaterials", rawMaterials);
            model.addAttribute("results", results);
        }
        return "sellcompressorreprocess";
    }

    @Data
    static private class SellCompressOrReprocessResult {
        private String icon;
        private String name;
        private String count;
        private String volume;
        private String sellRaw;
        private String sellCompressed;
        private String sellRefined;
        private String action;
    }
}

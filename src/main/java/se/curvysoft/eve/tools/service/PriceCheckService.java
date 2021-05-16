package se.curvysoft.eve.tools.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import se.curvysoft.eve.tools.model.Item;
import se.curvysoft.eve.tools.model.pricecheck.Appraisal;
import se.curvysoft.eve.tools.model.pricecheck.PriceCheck;
import se.curvysoft.eve.tools.model.pricecheck.PriceItem;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceCheckService {
    public Appraisal get(String rawItemList, String market) {
        try {
            HttpURLConnection conn = getAppraisalConnection(rawItemList, market);
            BufferedReader br;
            if (200 == conn.getResponseCode()) {
                PriceCheck priceCheck = getPriceCheck(conn);
                return priceCheck.getAppraisal();
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                System.out.println(br.lines().collect(Collectors.joining("\r\n")));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Item, PriceItem> get(Collection<Item> itemsToCheck, String market) {
        try {
            String data = itemsToCheck.stream().map(Item::getTypeName).collect(Collectors.joining("\r\n"));
            HttpURLConnection conn = getAppraisalConnection(data, market);
            BufferedReader br;
            if (200 == conn.getResponseCode()) {
                PriceCheck priceCheck = getPriceCheck(conn);
                return getItemPriceItemMap(itemsToCheck, priceCheck);
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                System.out.println(br.lines().collect(Collectors.joining("\r\n")));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private PriceCheck getPriceCheck(HttpURLConnection conn) throws IOException {
        BufferedReader br;
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String json = br.lines().collect(Collectors.joining("\r\n"));
        System.out.println(json);
        return mapper.readValue(json, PriceCheck.class);
    }

    private HttpURLConnection getAppraisalConnection(String data, String market) throws IOException {
        URL url = new URL(String.format("https://evepraisal.com/appraisal.json?market=%s&persist=no", market));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
        conn.setRequestProperty("User-Agent", "Eve Tools WIP (https://eve.curvysoft.se)");
        OutputStream os = conn.getOutputStream();
        os.write(data.getBytes());
        return conn;
    }

    private Map<Item, PriceItem> getItemPriceItemMap(Collection<Item> itemsToCheck, PriceCheck priceCheck) {
        Map<Integer, Item> itemIdMap = itemsToCheck.stream()
                .collect(Collectors.toMap(Item::getTypeID, item -> item));
        Map<Item, PriceItem> mappedPrices = new HashMap<>();
        for (PriceItem priceItem : priceCheck.getAppraisal().items) {
            mappedPrices.put(itemIdMap.get(priceItem.getTypeID()), priceItem);
        }
        return mappedPrices;
    }
}

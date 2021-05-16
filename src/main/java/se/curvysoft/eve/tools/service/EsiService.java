package se.curvysoft.eve.tools.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.api.AssetsApi;
import io.swagger.client.api.LocationApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.curvysoft.eve.tools.model.ApiClient;
import se.curvysoft.eve.tools.model.esi.CharacterData;
import se.curvysoft.eve.tools.model.esi.*;

import javax.annotation.PostConstruct;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EsiService {
    @Value("${curvysoft.esi.base}")
    private String oauthUrl;
    @Value("${curvysoft.esi.callback}")
    private String callbackUrl;
    @Value("${curvysoft.esi.client}")
    private String client;
    @Value("${curvysoft.esi.secret}")
    private String secret;
    @Value("${curvysoft.esi.user-agent}")
    private String userAgent;

    private String authEndpoint;
    private String tokenEndpoint;
    private String jwksUri;
    private String revocationEndpoint;
    private String issuer;

    private final SessionData sessionData;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            URL url = new URL(oauthUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Map authData = mapper.readValue(conn.getInputStream(), Map.class);

            authEndpoint = (String) authData.get("authorization_endpoint");
            tokenEndpoint = (String) authData.get("token_endpoint");
            revocationEndpoint = (String) authData.get("revocation_endpoint");
            jwksUri = (String) authData.get("jwks_uri");
            issuer = (String) authData.get("issuer");
        } catch (Exception ignore) {}
    }

    private List<String> scopes = Arrays.asList(
            "esi-characters.read_blueprints.v1"
    );

    public EsiService(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    public String getAuthRedirectUrl(String state) {
        return "redirect:" + authEndpoint
                + "?response_type=code"
                + "&redirect_uri=" + callbackUrl
                + "&client_id=" + client
                + "&scope=" + String.join(" ", scopes)
                + "&state=" + state;
    }

    public void requestToken(String code, boolean refresh) throws Exception {
        Map<String, String> data = new HashMap<>();
        if (refresh) {
            data.put("grant_type", "refresh_token");
            data.put("refresh_token", sessionData.getAccessToken().getRefreshToken());
        } else {
            data.put("grant_type", "authorization_code");
            data.put("code", code);
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(data);
        URL url = new URL(tokenEndpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setRequestProperty("Content-Type", "application/json");
        String auth = Base64.getEncoder().encodeToString((client + ":" + secret).getBytes());
        conn.setRequestProperty("Authorization", "Basic " + auth);
        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        AccessToken accessToken = mapper.readValue(conn.getInputStream(), AccessToken.class);

        DecodedJWT jwt = JWT.decode(accessToken.getAccessToken());
        JwkProvider provider = new UrlJwkProvider(new URL(jwksUri));
        Jwk jwk = provider.get(jwt.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
        algorithm.verify(jwt);

        if (!issuer.equals(jwt.getIssuer())) {
            throw new SignatureVerificationException(algorithm);
        }

        CharacterData characterData =
                new CharacterData(jwt.getClaims().get("name").asString(),
                        Integer.parseInt(jwt.getSubject().split(":")[2]),
                        jwt.getExpiresAt().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());

        sessionData.setAccessToken(accessToken);
        sessionData.setCharacterData(characterData);
    }

    private void refreshToken() {

    }

    public void signOut() {
        if (!isLoggedIn()) {
            return;
        }
        try {
            String refreshTokenToRevoke = sessionData.getAccessToken().getRefreshToken();
            sessionData.setAccessToken(null);
            sessionData.setCharacterData(null);

            Map<String, String> data = new HashMap<>();
            data.put("token_type_hint", "refresh_token");
            data.put("token", refreshTokenToRevoke);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            URL url = new URL(revocationEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("Content-Type", "application/json");
            String auth = Base64.getEncoder().encodeToString((client + ":" + secret).getBytes());
            conn.setRequestProperty("Authorization", "Basic " + auth);
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
        } catch (Exception ignore) {
        }
    }

    public boolean isLoggedIn() {
        return sessionData.getAccessToken() != null && sessionData.getCharacterData() != null;
        //&& sessionData.getCharacterData().getExpiryDate().isAfter(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(20));
    }

    public Integer getCharacterId() {
        if (isLoggedIn()) {
            return sessionData.getCharacterData().getCharacterId();

        }
        return null;
    }

    public String getCharacterName() {
        if (isLoggedIn()) {
            return sessionData.getCharacterData().getCharacterName();

        }
        return "NotLoggedin";
    }

    public String getCharacterPortraitUri() {
        if (isLoggedIn()) {
            return String.format("https://images.evetech.net/characters/%d/portrait?size=32", sessionData.getCharacterData().getCharacterId());
        }
        return "https://image.eveonline.com/Character/1_32.jpg";
    }

    public void getCurrentShip() {
        ApiClient client = getApiClient();
        GetCharactersCharacterIdShipOk ship = new LocationApi(client).getCharactersCharacterIdShip(getCharacterId(), null, null, null);
        if (ship != null && ship.getShipItemId() != null) {
            long shipId = ship.getShipItemId();
            AssetsApi assetsApi = new AssetsApi(client);
            List<GetCharactersCharacterIdAssets200Ok> assets = assetsApi.getCharactersCharacterIdAssets(getCharacterId(), null, null, null, null);
            GetCharactersCharacterIdAssets200Ok shipAsset = assets.stream().filter(asset -> asset.getItemId().equals(shipId)).findAny().orElse(null);
            Set<GetCharactersCharacterIdAssets200Ok> assetsInShip = assets.stream().filter(asset -> asset.getLocationId().equals(shipId)).collect(Collectors.toSet());
            System.out.println();
        }
    }

    public ApiClient getApiClient() {
        if (!isLoggedIn()) {
            return null;
        }

        if (sessionData.getCharacterData().getExpiryDate().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            try {
                requestToken(null, true);
            } catch (Exception ignore) {
                return null;
            }
        }

        ApiClient client = new ApiClient();
        client.setUserAgent(userAgent);
        client.setAccessToken(sessionData.getAccessToken().getAccessToken());
        return client;
    }
}

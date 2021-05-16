package se.curvysoft.eve.tools.controller;

import io.swagger.client.api.CharacterApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.curvysoft.eve.tools.model.ApiClient;
import se.curvysoft.eve.tools.model.esi.GetCharactersCharacterIdBlueprints200Ok;
import se.curvysoft.eve.tools.model.esi.SessionData;
import se.curvysoft.eve.tools.service.EsiService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class EsiController {
    private final EsiService esiService;
    private final SessionData sessionData;

    public EsiController(EsiService esiService, SessionData sessionData) {
        this.esiService = esiService;
        this.sessionData = sessionData;
    }

    @RequestMapping("esi-auth")
    public String launchSignOn(@RequestParam String from, @RequestParam(defaultValue = "false") Boolean force) {
        return esiService.getAuthRedirectUrl(from);
    }

    @RequestMapping("esi-callback")
    public String catchCallback(RedirectAttributes attrs, @RequestParam String code, @RequestParam String state) {
        try {
            esiService.requestToken(code, false);
        } catch (Exception e) {
            attrs.addFlashAttribute("error", "Authentication failed");
        }
        return "redirect:" + state;
    }

    @RequestMapping("esi-signout")
    public String signOut(RedirectAttributes attrs, @RequestParam String from) {
        try {
            esiService.signOut();
        } catch (Exception ignore) {}
        return "redirect:" + from;
    }

    @RequestMapping("esi-check")
    public String esiTest(Model model) {
        esiService.getCurrentShip();
        return "oauthTest";
    }
}

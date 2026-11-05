package pl.easyrynekdiscord;

import net.fabricmc.api.ClientModInitializer;

public class EasyRynekDiscord implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DiscordNotifier.loadConfig();
    }
}

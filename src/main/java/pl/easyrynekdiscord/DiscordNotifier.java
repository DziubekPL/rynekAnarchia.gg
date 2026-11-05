package pl.easyrynekdiscord;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DiscordNotifier {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "EasyRynekDiscord");
        t.setDaemon(true);
        return t;
    });

    private static String webhookUrl = "";
    private static String username = "EasyRynek";
    private static boolean enabled = true;

    private DiscordNotifier() {}

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("easyrynekdiscord.json");
    }

    public static void loadConfig() {
        try {
            Path p = configPath();
            if (!Files.exists(p)) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", true);
                o.addProperty("webhookUrl", "");
                o.addProperty("username", "EasyRynek");
                Files.createDirectories(p.getParent());
                Files.writeString(p, GSON.toJson(o));
                return;
            }
            JsonObject o = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            if (o.has("enabled")) enabled = o.get("enabled").getAsBoolean();
            if (o.has("webhookUrl")) webhookUrl = o.get("webhookUrl").getAsString().trim();
            if (o.has("username")) username = o.get("username").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- odczyt pol obiektu Zakupy$Zakup refleksja (nie trzeba miec jara EasyRynek przy kompilacji) ---
    private static Object f(Object o, String name) {
        try {
            Field fl = o.getClass().getField(name);
            return fl.get(o);
        } catch (Exception e) {
            return null;
        }
    }
    private static String s(Object o, String n, String def) { Object v = f(o, n); return v == null ? def : v.toString(); }
    private static long l(Object o, String n) { Object v = f(o, n); return v instanceof Number x ? x.longValue() : 0L; }
    private static int i(Object o, String n) { Object v = f(o, n); return v instanceof Number x ? x.intValue() : 0; }
    private static double d(Object o, String n) { Object v = f(o, n); return v instanceof Number x ? x.doubleValue() : 0.0; }

    private static String fmt(long n) { return String.format(Locale.US, "%,d", n).replace(',', ' '); }

    public static void zakup(Object z) {
        if (!enabled || webhookUrl.isEmpty() || z == null) return;

        String nazwa = s(z, "nazwa", s(z, "item", "?"));
        int ilosc = i(z, "ilosc");
        long cena = l(z, "cena");
        double rynkowa = d(z, "rynkowa");
        double zysk = d(z, "zysk");
        String grupa = s(z, "grupa", "-");
        boolean auto = Boolean.TRUE.equals(f(z, "auto"));

        JsonArray fields = new JsonArray();
        fields.add(field("Ilość", String.valueOf(ilosc), true));
        fields.add(field("Cena", fmt(cena), true));
        fields.add(field("Wartość rynkowa", String.format(Locale.US, "%,.0f", rynkowa).replace(',', ' '), true));
        fields.add(field("Zysk", String.format(Locale.US, "%,.0f", zysk).replace(',', ' '), true));
        fields.add(field("Grupa", grupa, true));
        fields.add(field("Tryb", auto ? "auto" : "ręcznie", true));

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "🛒 Zakup: " + nazwa);
        embed.addProperty("color", zysk >= 0 ? 0x2ECC71 : 0xE74C3C);
        embed.add("fields", fields);
        embed.addProperty("timestamp", Instant.now().toString());

        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        body.add("embeds", embeds);
        body.add("allowed_mentions", GSON.toJsonTree(java.util.Map.of("parse", new String[0])));

        send(GSON.toJson(body));
    }

    private static JsonObject field(String name, String value, boolean inline) {
        JsonObject o = new JsonObject();
        o.addProperty("name", name);
        o.addProperty("value", value.isEmpty() ? "-" : value);
        o.addProperty("inline", inline);
        return o;
    }

    private static void send(String json) {
        EXEC.execute(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(webhookUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 429) {          // rate limit - jedno ponowienie
                    Thread.sleep(2000);
                    HTTP.send(req, HttpResponse.BodyHandlers.discarding());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

# EasyRynek Discord

Dodatek do moda EasyRynek (RynekKlikacz, Fabric 1.21.4). Przy każdym zakupie wysyła embed na Discorda.

## Instalacja
1. Wrzuć `EasyRynekDiscord-1.0.0.jar` do `mods` (obok EasyRynek i Fabric API).
2. Uruchom grę raz - powstanie `config/easyrynekdiscord.json`.
3. Wklej do `webhookUrl` adres webhooka (Discord: ustawienia kanału > Integracje > Webhooki).
4. Uruchom ponownie grę.

## Budowanie
`gradle build` (JDK 21), wynik w `build/libs/`. Albo wrzuć na GitHuba - Actions zbuduje jar.

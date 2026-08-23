# MysticosSkywars

Plugin de SkyWars para Spigot, Paper y Purpur. Incluye múltiples arenas, modos SOLO y TEAMS, menús GUI, kits, tiendas, votaciones, fiestas, estadísticas, logros, leaderboards y soporte para arenas normales o basadas en esquemas.

## Características

- Mensajes, menús, scoreboard y nombres de ítems con MiniMessage.
- Idioma predeterminado: Español (`es_es`).
- PlaceholderAPI, Vault, DecentHolograms y HolographicDisplays.
- ItemsAdder para ítems de lobby, kits, tiendas y recompensas.
- LuckyBlock configurable con múltiples bloques y recompensas ponderadas.
- BungeeCord y Velocity mediante el canal compatible de BungeeCord.
- Persistencia SQLite, MySQL, MariaDB y otros motores soportados por ORMLite.
- Soporte de WorldEdit y FAWE.

## Dependencias

Dependencia obligatoria:

- WorldEdit o FastAsyncWorldEdit.

Dependencias opcionales:

- PlaceholderAPI
- ItemsAdder
- Vault
- DecentHolograms
- HolographicDisplays
- Multiverse-Core o My_Worlds
- RealPermissions

LuckyBlock e ItemsAdder se detectan de forma opcional; el plugin no requiere sus APIs para iniciar.

## Compilación

Desde la raíz del proyecto:

```bash
mvn -pl mysticosskywars-plugin -am clean package
```

El plugin compilado se genera en `mysticosskywars-plugin/target/`.

## Comandos principales

El comando principal es `/msw`; también existe `/mysticosskywars`.

| Comando | Descripción |
|---|---|
| `/msw` | Abre el menú principal o de mapas. |
| `/msw list` | Lista las arenas. |
| `/msw kits` | Abre el menú de kits. |
| `/msw shop` | Abre la tienda. |
| `/msw play` | Busca una partida disponible. |
| `/msw leave` | Abandona la partida actual. |
| `/msw forcestart` | Fuerza el inicio de la partida. |
| `/msw create <nombre> <tipo> <jugadores>` | Crea una arena SOLO. |
| `/msw register <nombre>` | Registra una arena. |
| `/msw edit <nombre>` | Edita una arena. |
| `/msw finish` | Guarda la configuración de la arena. |
| `/msw setspectator` | Define la ubicación de espectadores. |
| `/msw reload` | Recarga la configuración. |
| `/party create` | Crea una fiesta. |

La mayoría de funciones administrativas requieren `msw.admin`.

## Crear una arena

1. Ejecuta `/msw create nombre default 8` o `/msw create nombre schematic 8`.
2. Configura la arena desde el menú y guarda los cambios.
3. Coloca las jaulas y los cofres usando los objetos de administración.
4. Define la ubicación de espectadores con `/msw setspectator`.
5. Define los límites con WorldEdit (`//pos1`, `//pos2` y `//expand vert`).
6. Ejecuta `/msw finish`.

Para una arena basada en esquema, coloca el archivo `.schem` en la carpeta `MysticosSkywars/maps` del servidor.

## LuckyBlock

La configuración está en `plugins/MysticosSkywars/config.yml`, dentro de `Config.LuckyBlock`.

LuckyBlock solo se procesa dentro de una partida activa:

```yaml
LuckyBlock:
  Enabled: true
  Break-In-Match-Only: true
  Blocks:
    - SPONGE
    - GOLD_BLOCK
    - "ITEMSADDER:luckyblocks:lucky_block"
  Rewards-Per-Break: 3
```

Tipos de recompensa disponibles:

```yaml
Rewards:
  - "WEIGHT=10|ITEM:MATERIAL=DIAMOND;AMOUNT=2;NAME=<aqua>Diamantes"
  - "WEIGHT=5|EFFECT:SPEED;DURATION=240;AMPLIFIER=2"
  - "WEIGHT=3|COMMAND:give %player% firework_rocket 16"
  - "WEIGHT=1|EXPLOSION:2.0"
  - "WEIGHT=6|ITEM:MATERIAL=ITEMSADDER:luckyblocks:coin;AMOUNT=1;NAME=<yellow>Moneda"
```

`WEIGHT` controla la probabilidad relativa. `Rewards-Per-Break: 0` abre todas las recompensas configuradas; no existe un límite fijo de recompensas.

## ItemsAdder

Los ítems personalizados se pueden usar en configuraciones que acepten `MATERIAL`:

```yaml
MATERIAL: "ITEMSADDER:namespace:id"
```

También pueden utilizarse como bloques LuckyBlock mediante la lista `Config.LuckyBlock.Blocks`.

## MiniMessage

Los textos aceptan MiniMessage:

```yaml
Prefix: "<white>Mysticos<aqua>Skywars <dark_gray>» <reset>"
```

Los colores antiguos con `&` siguen siendo compatibles para facilitar la migración de configuraciones existentes.

## BungeeCord y Velocity

Activa el modo proxy en `config.yml`:

```yaml
Bungeecord:
  Enabled: true
  Proxy-Type: AUTO
  Lobby-Server: lobby
```

En Velocity debe estar habilitado el canal de mensajes compatible con BungeeCord. El servidor lobby debe coincidir con `Lobby-Server`.

## API

La API se obtiene con:

```java
MysticosSkywarsAPI api = MysticosSkywarsAPI.getInstance();
```

Los managers principales incluyen mapas, jugadores, kits, tiendas, fiestas, estadísticas, base de datos, idiomas y leaderboards.

## Datos existentes

Las tablas SQL históricas se mantienen para evitar perder estadísticas y compras al actualizar desde versiones anteriores.

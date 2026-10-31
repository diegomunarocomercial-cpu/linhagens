# Linhagens — mod de RPG para Minecraft Java (Fabric 1.21.1)

Escolha sua raça num menu e ganhe aparência, passivas, habilidades ativas, mana e níveis próprios.
Raças: **Humano, Demônio, Anjo, Piglin** (fácil de adicionar mais em `race/Race.java`).

## Como compilar
1. Instale o **JDK 21** e o **IntelliJ IDEA** (ou use o Gradle direto).
2. Abra esta pasta no IntelliJ (ele baixa tudo sozinho), ou rode `gradle wrapper` e depois `./gradlew build`.
   (Se não tiver o Gradle: copie a pasta `gradle/` + `gradlew` + `gradlew.bat` de qualquer mod Fabric de exemplo.)
3. O `.jar` sai em `build/libs/linhagens-0.1.0.jar`. Coloque em `.minecraft/mods` com **Fabric Loader 0.16+** e **Fabric API**.
4. Para testar direto: `./gradlew runClient`.

Se a versão do Fabric API do `gradle.properties` não existir, pegue a mais recente para 1.21.1 em modrinth.com/mod/fabric-api.

## Como jogar
- **R**: abre o menu de raça (abre sozinho no primeiro login).
- **Z / X**: habilidades 1 e 2 (mudar em Controles > Linhagens).
- HUD no canto superior esquerdo: nível, XP, mana e recargas.
- Matar criaturas dá XP de linhagem (nível máx. 20): mais mana e habilidades mais fortes.
- Trocar de raça tem recarga de 5 min (`RaceManager.CHANGE_COOLDOWN_TICKS`) e reseta o nível.
- Admin: `/linhagem set <jogador> <raca>` e `/linhagem reset <jogador>`.

## Estrutura
- `race/` Race (definições), RaceData (nível/XP salvos)
- `server/` RaceManager (passivas, mana, troca), Abilities (habilidades), eventos, comando
- `net/` pacotes cliente <-> servidor
- `client/` menu, HUD, renderização da skin (RaceModel + RaceLayer)
- `tools/gerar_texturas.py` gera os PNGs 128x128; você também pode pintá-los à mão (layout no topo do script)

## Novidades (v2)
- **Vila Aurora**: vila média com praça, fonte e o *Núcleo da Aurora* no centro (a aurora roxa dança no céu sobre ela), 6 casas
  (biblioteca, ferraria...), torre de observação, fazenda, aldeões e Guardiões da Aurora. Aparece em planícies, florestas, taigas e prados.
  Para achar: `/locate structure linhagens:vila_aurora` (ou `/place structure linhagens:vila_aurora` para criar uma na sua frente).
- **Armas** (equilibradas): Katana (5 dano, 2.2 vel; golpes dão Velocidade; botão direito = avanço cortante),
  Machado de Lenhador (8 dano, 1.0 vel; derruba árvores inteiras; resiste a empurrão),
  Machado do Desastre (11 dano, 0.9 vel; golpes racham em área; botão direito = Cataclismo, custa 2 corações).
- **Armadura da Aurora** (4 peças, feita com Fragmentos da Aurora): bônus de conjunto de velocidade, regeneração e visão noturna à noite.
- **Mobs**: Imp Infernal (Nether), Espadachim Sombrio (escuro do Overworld, usa a Katana), Guardião da Aurora (golem da vila).
- **Aba criativa "Linhagens"** com tudo (inclusive ovos de spawn).
- Regerar arte/estrutura: `python3 tools/gerar_itens.py` e `python3 tools/gerar_vila.py`.

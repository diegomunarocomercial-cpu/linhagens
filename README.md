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

## Novidades (v3)
- **Árvore de habilidades** (tecla **K**): 1 ponto por nível (até o 20), 3 ramos (Ataque, Defesa, Maestria) por raça, cada um com um poder final exclusivo.
- **Vila Aurora** (reformulada): vila normal com 6 casas simples, 5 médias, 1 casa grande (chefe), poço com o **Braço do Poço** (entregue uma esmeralda!)
  e 5 plantações. O Núcleo da Aurora fica enterrado sob o poço e faz uma aurora boreal enorme dançar no céu.
- **Castelo de Gelo**: muralhas, 4 torres, pátio, Salão Principal (2 filhotes atacam) e Sala do Chefe (Dragão de Gelo Ancestral, 1 a 3 ovos ao morrer).
  Procure com `/locate structure linhagens:castelo_gelo`.
- **Mobs novos**: Dragão de Gelo (chefe e filhote, com sopro gélido), Ocultista do Culto Diablos (capa vermelha, maldição e invoca imps).
- **Texturas melhoradas** de armas, armadura e mobs; nomes 100% em português.
- Regerar arte/estruturas: `python3 tools/gerar_itens.py`, `python3 tools/gerar_vila.py`, `python3 tools/gerar_castelo.py`.

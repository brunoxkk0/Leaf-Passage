# Leaf Passage

Mod para **Minecraft 1.7.10 / Forge 10.13.4.1614** que permite atravessar e cair através das folhas. A alteração vale para jogadores e outras entidades, no cliente e no servidor.

Autor: [brunoxkk0](https://github.com/brunoxkk0).

Reconhece folhas pelo material `Material.leaves` ou pelo contrato Forge `Block.isLeaves(world, x, y, z)`. Não depende de uma lista de nomes nem exige Biomes O' Plenty ou Forestry. Blocos de mods que não usam nenhum desses identificadores precisam de uma integração adicional.

Ao atravessar folhas, jogadores emitem o som de passo do próprio bloco, no máximo a cada 5 ticks (0,25 segundo). O servidor envia o som ao jogador e às pessoas próximas. Durante uma queda, cruzar folhas reduz a velocidade vertical pela metade para o próximo movimento, simulando contato com os galhos. A detecção cobre também quedas rápidas que atravessam a copa inteira em uma atualização. Subida, movimento horizontal e voo criativo mantêm sua velocidade.

Seleção, quebra, drops e renderização permanecem sob responsabilidade dos blocos originais. A busca de caminhos e a navegação dos mobs tratam folhas como ar: podem atravessá-las e não tentam usá-las como chão ou degrau. Esta base não inclui encantamentos nem redução de dano de queda. A desaceleração não zera a distância de queda acumulada pelo Minecraft.

Jogadores cujo corpo está dentro de folhas ficam ocultos nas verificações de visão dos monstros, mesmo com os olhos acima da folhagem. Uma perseguição corpo a corpo já iniciada continua recalculando o caminho e atravessando as folhas para buscar o alvo. A visão continua bloqueada para detectar novos alvos e para ataques que exigem linha de visão. A memória padrão da IA é preservada: tarefas que verificam visão esquecem o alvo após 60 ticks sem enxergá-lo, se não o alcançarem antes. IAs de mods que usam sua própria detecção podem ter comportamento diferente.

## Instalação

Instale o JAR sem o sufixo `-dev` em `mods/`, junto com **[UniMixins 0.3.1 ou posterior](https://github.com/LegacyModdingMC/UniMixins/releases/tag/0.3.1)**. Em multiplayer, instale ambos no cliente e no servidor.

## Desenvolvimento

O build usa o [starter oficial GTNH ExampleMod1.7.10](https://github.com/GTNewHorizons/ExampleMod1.7.10/releases/download/master-packages/starter.zip), consultado em 2026-10-05 (repositório na revisão `6453dec5f5f534e604887d3505ddf7125889c6f9`), com Gradle 9.7.1 e convenções GTNH 2.0.33.

Use JDK 25 para o Gradle e JDK 8 para o jogo. O template seleciona as toolchains. No Windows:

```powershell
.\gradlew.bat setupDecompWorkspace
.\gradlew.bat spotlessApply
.\gradlew.bat spotlessCheck build
.\gradlew.bat runClient
.\gradlew.bat runClient -PcompatibilityMods
```

No Linux/macOS, substitua `.\gradlew.bat` por `./gradlew`.

`-PcompatibilityMods` abre o cliente com BOP, Forestry e suas dependências para teste manual, sem executar o teste automático.

O código de produção consiste na entrada Forge e em Mixins de `World`, `Entity`, `EntityPlayerSP`, `PathFinder` e `PathNavigate`. Eles reutilizam o mesmo reconhecimento de folhas para remover colisão, sufocamento e expulsão de entidades. O Mixin de `World` condiciona as chamadas nos dois métodos de coleta, cobrindo também blocos que sobrescrevem a colisão. Os pontos de injeção têm contagens obrigatórias para falhar explicitamente em uma alteração incompatível.

## Teste no jogo

```powershell
.\gradlew.bat runClient -PleafSmokeTest --no-configuration-cache
.\gradlew.bat runObfClient -PleafSmokeTest --no-configuration-cache
```

Esses comandos baixam as dependências de teste, abrem o cliente, criam um mundo plano separado, executam a verificação no jogador real nos dois lados e fecham o cliente. O Gradle falha se não houver `SMOKE RESULT client=true server=true` no log. Os mundos ficam em `run/leaf-smoke/saves/` ou `run/obfuscated/saves/`.

O teste fica em `src/smokeTest/`, fora do JAR distribuído. Confere os dois métodos de colisão, movimento lateral, queda, sufocamento, expulsão, conservação do bloco e pedra como controle sólido. Verifica desaceleração em quedas normais e rápidas, som do bloco, intervalo entre sons, silêncio parado e movimento normal no ar ou na subida. Confere também a chegada do som ao sistema de áudio do cliente do próprio jogador. Verifica também passagem na busca de caminhos e na navegação, rejeição de folhas como apoio e pedra como obstáculo e apoio sólido. Inclui folhas sintéticas com colisão sobrescrita e identificação apenas por API ou apenas por material.

Dependências do cenário de teste: Biomes O' Plenty **2.1.0.2308**, Forestry GTNH **4.11.39**, GTNHLib **0.11.48**, CoFHLib **2388748** e UniMixins **0.3.1**. GTNHLib e CoFHLib atendem ao Forestry escolhido e não são dependências do Leaf Passage.

## Inspiração e licença

Inspirado nas mecânicas de [Passable Foliage](https://modrinth.com/mod/passable-foliage), [Passable Leaves](https://modrinth.com/mod/passable-leaves) e [Passable Leaves Datapack](https://modrinth.com/datapack/passable-leaves-datapack). Implementação independente para Forge 1.7.10; não inclui código ou recursos desses projetos.

Licença MIT. A infraestrutura de build vem do template GTNH.

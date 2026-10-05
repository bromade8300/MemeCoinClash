# MemeCoin Clash

## Conventions

- Dépôt Git : `git@github.com:bromade8300/MemeCoinClash.git`, branche principale `main`.
- Java 17, Swing, aucune bibliothèque Java externe.
- Une classe par fichier dans `memecoinclash`.
- Une partie active par processus : quatre Maîtres Singletons imposés par le sujet.
- Moteur dans `Simulation` ; rendu dans `BoardPanel` ; commandes dans `GameWindow`.
- Compiler avec `--release 17 -encoding UTF-8` dans `build/classes`.
- Tous les tirages passent par `RandomSource`. Les graines explicites servent aux tests ; une nouvelle partie normale ne réinitialise pas le générateur.
- Rapport de six pages exactement, couverture comprise. Les diagrammes vectoriels utilisent deux pages agrandies pour préserver la lecture des déclarations.
- Identités et rôles réels proviennent exclusivement de `docs/equipe.json`. Ne jamais inventer les contributions.
- Archive définitive : `CO.MemeCoinClash.NOM.Prenom.zip`, selon le référent confirmé.

## QUICK LOOKUP

| Besoin | Fichier |
| --- | --- |
| Sujet et remise | `ConceptObjet - Practical Lab - Objects  Simulation.pdf`, attentes p. 5-6, remise p. 7 |
| Tours, échanges, livraison, victoire | `src/main/java/memecoinclash/Simulation.java` |
| Déplacement, énergie, épuisement | `src/main/java/memecoinclash/Agent.java` |
| Terrain et zones sûres | `src/main/java/memecoinclash/GameMap.java` |
| Réglages | `GameSettings.java`, `SettingsDialog.java` dans le package |
| Vérification | `src/test/java/memecoinclash/SimulationTest.java`, `test.ps1`, `test.sh` |
| Build et JAR | `build.ps1 -Package`, `sh build.sh --package` |
| Rapport | `tools/build_report.py`, `docs/equipe.json` |
| UML | `tools/generate_class_diagram.py`, `docs/diagrammes/*.puml` |
| Remise | `tools/package_submission.py`, `docs/CONFORMITE.md` |

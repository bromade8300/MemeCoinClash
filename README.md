# MemeCoin Clash

Simulation multi-agents en Java 17 pour le mini-projet Concept Objet. Doge, Shiba, Pepe et Bonk collectent des messages et les livrent à leur Maître.

## Lancer et tester

Pour jouer directement : `java -jar output/app/MemeCoinClash.jar`. Java 17 ou plus suffit ; le JAR contient les logos et ne dépend pas du dossier courant. Aucune bibliothèque Java externe n'est requise.

Pour compiler les sources, installer un JDK 17 ou plus :

| Action | Windows | Linux / macOS |
| --- | --- | --- |
| Jouer depuis les sources | `run.cmd` | `sh run.sh` |
| Vérifier les règles | `test.cmd` | `sh test.sh` |
| Construire le JAR | `powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1 -Package` | `sh build.sh --package` |

Les lanceurs se placent automatiquement à la racine. Les sources sont compilées en UTF-8, avec `--release 17`. Sous Windows, `JAVA_HOME` peut désigner le JDK si `jar` n'est pas dans le PATH.

## Commandes

- Démarrer / Pause : exécution automatique.
- Un tour : un pas discret.
- Nouvelle partie : recommence avec les réglages actuels.
- Réglages : population, obstacles, énergie, durée, vitesse, alliances, airdrop et thème néon. Appliquer lance une nouvelle partie.

Chaque pion affiche le logo de sa faction. M marque un Maître, × un agent épuisé, et une case barrée un obstacle. La barre sous un agent indique son énergie. Le panneau latéral montre les rencontres, livraisons et compteurs statiques d'instances.

Le score compte les messages distincts reçus par le Maître. Après chaque action, une collection complète arrête la partie. Sinon, la limite de tours désigne le meilleur score ; une égalité donne un match nul. Une rencontre exige une case occupée sur le trajet prévu, comme précisé page 4 du sujet.

## Organisation

- src/main/java/memecoinclash : une classe par fichier. GameWindow pilote les commandes ; BoardPanel dessine ; SettingsDialog configure ; Simulation applique les règles ; GameMap gère terrain et géométrie ; Agent et ses spécialisations incarnent la hiérarchie ; RandomSource centralise le hasard.
- src/test/java/memecoinclash/SimulationTest.java : tests autonomes, sans bibliothèque externe.
- docs/diagrammes : sources PlantUML et SVG des trois diagrammes UML.
- output/pdf/MemeCoinClash_Rapport.pdf : rapport de six pages.
- output/app/MemeCoinClash.jar : exécutable autonome avec logos.
- docs/equipe.json : identités, référent et descriptions anonymes des contributions confirmées.
- docs/CONFORMITE.md : correspondance avec le sujet.

Le moteur n'utilise aucun composant Swing. L'interface lit l'état de la simulation et n'applique aucune règle de combat ou de score. Les quatre Maîtres sont des Singletons partagés : une seule partie est active dans le programme. Les compteurs affichés sont cumulatifs depuis le lancement.

## Validation

La suite autonome couvre les initialisations 2/5/8 agents, voisinage, occupation des cases à chaque tour, zones réservées, énergie, échanges, combat, collisions, livraison, victoire, égalité, réinitialisation et reproductibilité par graine. Elle rend également le plateau sans écran.

Validation effectuée sur Windows avec le JDK 22, en cible Java 17 : `SimulationTest: OK`. Le JAR a été utilisé depuis un autre dossier avec vérification des quatre logos embarqués. Les commandes de fenêtre n'ont pas été vérifiées dans une session graphique interactive.

## Reproduire les documents

Ces outils sont facultatifs pour jouer. Ils nécessitent Python (`reportlab`, `svglib`, `pypdf`) et PlantUML pour les SVG.

1. Compiler : `build.ps1 -Package` ou `sh build.sh --package`.
2. Générer les déclarations : `python tools/generate_class_diagram.py` (`--javap chemin` si nécessaire).
3. Régénérer les SVG : `java -jar plantuml.jar -charset UTF-8 -tsvg docs/diagrammes/*.puml`.
4. Compléter `docs/equipe.json`, puis exécuter `python tools/build_report.py`.
5. Exporter : `python tools/package_submission.py`.

Les pages 3 et 4 du rapport sont en A2 paysage pour conserver la lisibilité des diagrammes vectoriels. Le sujet impose six pages, sans imposer leur format.

## Avant remise

Compléter les noms des 3 ou 4 membres, le référent et leurs rôles réels dans `docs/equipe.json`, puis régénérer le rapport. Le script d'export refuse une remise définitive avec ces informations manquantes. `--draft` produit explicitement une archive à compléter.

La remise définitive s'appelle `CO.MemeCoinClash.NOM.Prenom.zip`, selon le référent. Elle contient sources, rapport, JAR, logos, tests et diagrammes ; elle exclut `build/`, `tmp/`, les fichiers IDE et le sujet. Le référent envoie le code et le rapport à l'adresse indiquée page 7 du sujet. La date limite dépend du jour de l'examen ; aucune date absolue n'est fournie.

## Logos

Les images sont incluses dans assets/ pour jouer hors ligne. Doge, Shiba et Pepe proviennent de https://cryptologos.cc/ ; Bonk du kit de marque https://brand.bonkcoin.com/. Les marques restent la propriété de leurs détenteurs.

# Correspondance avec le sujet

Le sujet est « A MAS Polymorphic Simulation », section Expectations, pages 5 et 6.

| Exigence | Réalisation | Fichier |
| --- | --- | --- |
| Pas discrets, ordre aléatoire | Mélange à chaque tour, un appel de move() par agent actif, effet immédiat | Simulation.java |
| Quatre populations et deux alliances | Doge/Shiba sous HypeAgent ; Pepe/Bonk sous ViralAgent | Hiérarchie des agents |
| Trois concepts objet | Héritage, encapsulation de la carte, move() polymorphe | Agent.java, GameMap.java |
| Quatre Singletons | Instance unique, constructeur privé et get() pour chaque Maître | *Master.java |
| Compteurs statiques visibles | Compteurs des quatre populations affichés dans la barre latérale | *Agent.java, GameWindow.java |
| Carte et voisinage | 20 × 14, quatre SafeZones 3 × 3, voisins 3/5/8, direction du retour | GameMap.java |
| Énergie | Dépense hors zone, retour sous 20 %, récupération en zone, épuisement | Agent.java |
| Rencontres | Case occupée sur le trajet prévu ; union, copie aléatoire entre alliés ou transfert après confrontation | Simulation.java ; sujet p. 4 |
| Score et fin | Seuls les messages du Maître comptent ; 24 messages ou limite de tours | Simulation.java |
| Hasard centralisé | Une seule classe pour tous les tirages | RandomSource.java |
| Représentation graphique | Plateau Swing, logos, énergie, scores et journal | BoardPanel.java, GameWindow.java |
| Rapport et UML | PDF de six pages ; classes complet, états, activités | output/pdf, docs/diagrammes |
| Identités et rôles du groupe | Informations réelles à confirmer ; descriptions anonymes page 6 | docs/equipe.json ; rapport PDF |

## Validation effectuée

- Compilation UTF-8 avec `--release 17`, sur JDK 22.
- Suite `SimulationTest` réussie : initialisation, invariants à chaque tour, échanges, déplacement, énergie, livraison, conditions de fin et reproductibilité.
- JAR Java 17 avec les quatre logos embarqués ; rendu du plateau depuis un dossier extérieur au projet.
- Rapport de six pages, trois UML synchronisés avec le code, rendu de chaque page contrôlé.
- Les commandes de fenêtre restent à vérifier dans une session graphique interactive.

## À compléter avant remise

1. Renseigner les noms, prénoms, référent et contributions réelles dans `docs/equipe.json`.
2. Régénérer le PDF avec `python tools/build_report.py`.
3. Exécuter `python tools/package_submission.py` : le nom est fondé sur le référent et les fichiers temporaires sont exclus.

L'airdrop optionnel est une extension. Les autres bonus du sujet ne sont pas revendiqués.

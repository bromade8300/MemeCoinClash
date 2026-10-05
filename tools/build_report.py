#!/usr/bin/env python3
"""Build exactly six pages from the code, UML and confirmed team metadata."""
from pathlib import Path
import html
import json
import os
from io import BytesIO
import xml.etree.ElementTree as ET
import reportlab
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4, A2, landscape
from reportlab.lib import colors
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph
from reportlab.graphics import renderPDF
from reportlab.graphics.shapes import String
from svglib.svglib import svg2rlg

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'output/pdf/MemeCoinClash_Rapport.pdf'
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
TEAM = json.loads((ROOT / 'docs/equipe.json').read_text(encoding='utf-8'))
READY = (3 <= len(TEAM['membres']) <= 4 and all(TEAM['membres'])
         and TEAM['referent'].get('nom') and TEAM['referent'].get('prenom')
         and len(TEAM['roles_anonymes']) == len(TEAM['membres']))
font_dir = Path(os.environ.get('WINDIR', 'C:/Windows')) / 'Fonts'
fallback = Path(reportlab.__file__).parent / 'fonts'
for name, win, vera in [('Body', 'arial.ttf', 'Vera.ttf'), ('BodyBold', 'arialbd.ttf', 'VeraBd.ttf')]:
    path = font_dir / win if (font_dir / win).exists() else fallback / vera
    pdfmetrics.registerFont(TTFont(name, str(path)))
pdfmetrics.registerFontFamily('Body', normal='Body', bold='BodyBold', italic='Body', boldItalic='BodyBold')
NAVY = colors.HexColor('#12213b')
BLUE = colors.HexColor('#365acc')
GREY = colors.HexColor('#506078')
LIGHT = colors.HexColor('#eaf0fa')
c = canvas.Canvas(str(OUTPUT), pageCompression=1)
c.setTitle('MemeCoin Clash - Rapport de projet Concept Objet')
c.setAuthor(' ; '.join(TEAM['membres']) or 'Équipe à renseigner')

def base(title, subtitle='', size=A4, page=1):
    w, h = size
    c.setPageSize(size)
    c.setFillColor(NAVY)
    c.rect(0, h-79, w, 79, fill=1, stroke=0)
    c.setFillColor(colors.white)
    c.setFont('BodyBold', 21)
    c.drawString(45, h-42, title)
    c.setFont('Body', 9)
    c.drawString(46, h-62, subtitle)
    c.setStrokeColor(LIGHT)
    c.line(45, 40, w-45, 40)
    c.setFillColor(GREY)
    c.setFont('Body', 8)
    c.drawString(45, 25, 'Concept Objet | M2 | ISEN Lille | Java 17')
    c.drawRightString(w-45, 25, f'{page} / 6')
    return w, h

def text(x, y, width, content, size=10, leading=14.5, color=NAVY):
    style = ParagraphStyle('body', fontName='Body', fontSize=size, leading=leading, textColor=color)
    p = Paragraph(content, style)
    _, height = p.wrap(width, 10000)
    if y-height < 48:
        raise ValueError(f'Overflow: {content[:80]}')
    p.drawOn(c, x, y-height)
    return y-height

def section(x, y, width, heading, content, size=10):
    c.setFillColor(BLUE)
    c.setFont('BodyBold', 12.5)
    c.drawString(x, y, heading)
    return text(x, y-12, width, content, size=size)-23

def diagram(name, box):
    x, y, w, h = box
    svg = ET.parse(ROOT / f'docs/diagrammes/{name}.svg').getroot()
    for parent in list(svg.iter()):
        for child in list(parent):
            if child.tag.endswith('text') and 'underline' in child.get('text-decoration', ''):
                sx = float(child.get('x', '0'))
                sy = float(child.get('y', '0')) + 1.5
                length = float(child.get('textLength', '0'))
                ET.SubElement(parent, '{http://www.w3.org/2000/svg}line', {
                    'x1': str(sx), 'y1': str(sy), 'x2': str(sx+length), 'y2': str(sy),
                    'stroke': child.get('fill', '#000000'), 'stroke-width': '0.75',
                })
    drawing = svg2rlg(BytesIO(ET.tostring(svg)))
    def normalize(node):
        if isinstance(node, String):
            node.text = html.unescape(node.text)
        for child in getattr(node, 'contents', []):
            normalize(child)
    normalize(drawing)
    scale = min(w/drawing.width, h/drawing.height)
    c.saveState()
    c.translate(x+(w-drawing.width*scale)/2, y+(h-drawing.height*scale)/2)
    c.scale(scale, scale)
    renderPDF.draw(drawing, c, 0, 0)
    c.restoreState()

# 1 - Cover: the actual board and only confirmed identities.
w, h = base('MEMECOIN CLASH', 'Simulation multi-agents polymorphe - Rapport de projet', page=1)
c.setFillColor(BLUE)
c.setFont('BodyBold', 22)
c.drawString(48, h-126, '4 factions. 1 arène. 24 messages.')
text(48, h-143, w-96, 'Doge, Shiba, Pepe et Bonk explorent une carte, échangent leurs connaissances et livrent les messages collectés à leur Maître.', size=10.5)
c.drawImage(str(ROOT/'docs/illustrations/plateau.png'), 48, 370, width=w-96, height=285, preserveAspectRatio=True, anchor='c')
text(48, 356, w-96, 'Plateau Swing rendu à partir du moteur, au tour 6. Les logos identifient les factions ; M désigne un Maître.', size=8.5, leading=11, color=GREY)
members = '<br/>'.join(html.escape(name) for name in TEAM['membres']) or 'Identités des membres à renseigner.'
ref = TEAM['referent']
referent = html.escape((ref.get('prenom', '')+' '+ref.get('nom', '')).strip()) or 'À renseigner'
y = section(48, 302, w-96, 'Équipe et référent', members+'<br/><b>Référent :</b> '+referent)
y = section(48, y, w-96, 'Dossier remis', 'Sources Java, exécutable JAR avec logos intégrés, lanceurs Windows et Unix, tests autonomes, trois diagrammes UML et ce rapport de six pages.')
if not READY:
    text(48, y+2, w-96, '<b>Version à compléter :</b> identité de l’équipe et contributions réelles.', size=9, leading=12, color=colors.HexColor('#926016'))
c.showPage()

# 2 - Required OO concepts illustrated by concrete responsibilities.
w, h = base('Architecture et concepts objet', 'Responsabilités, héritage, encapsulation et polymorphisme', page=2)
y = h-109
y = section(48, y, w-96, 'Un moteur indépendant de la fenêtre', '<b>Simulation</b> conserve les agents, les Maîtres et le journal, puis applique les règles. <b>GameMap</b> gère coordonnées, obstacles, zones sûres, voisins et direction du retour. <b>GameSettings</b> porte une configuration immuable. <b>GameWindow</b>, <b>BoardPanel</b> et <b>SettingsDialog</b> pilotent, affichent et configurent la partie.')
y = section(48, y, w-96, 'Héritage : factoriser données et déplacement', '<b>Agent</b> rassemble équipe, position, énergie, dernière direction et messages. <b>HypeAgent</b> regroupe Doge/Shiba ; <b>ViralAgent</b> regroupe Pepe/Bonk : ces classes abstraites matérialisent les deux alliances. <b>Master</b> hérite aussi d’Agent, mais reste immobile.')
y = section(48, y, w-96, 'Polymorphisme : quatre comportements', 'Le moteur appelle <b>move(Simulation)</b> sur chaque agent actif. Doge choisit une direction et 1 à 3 cases ; Shiba une diagonale et 1 à 2 cases ; Pepe une direction et 1 case ; Bonk une direction et 1 à 2 cases. <b>travel()</b> gère collisions, énergie et livraison. La redéfinition de <b>Master.move()</b> est vide ; les Maîtres sont exclus de la liste mobile.')
y = section(48, y, w-96, 'Encapsulation : protéger les responsabilités', 'La grille d’obstacles et les collections du moteur sont privées. <b>agents()</b> et <b>events()</b> exposent des listes non modifiables. Les classes du modèle restent internes au package ; leur état est partagé entre moteur et rendu. Cette encapsulation est partielle : une vue non modifiable ne rend pas chaque Agent immuable. La fenêtre ne décide ni des combats ni des scores.')
y = section(48, y, w-96, 'Singleton : quatre Maîtres uniques', '<b>DogeMaster</b>, <b>ShibaMaster</b>, <b>PepeMaster</b> et <b>BonkMaster</b> possèdent chacun une instance statique, un constructeur privé et <b>get()</b>. Leur état est réinitialisé pour une nouvelle partie. Le programme gère une partie active ; ces instances sont partagées au niveau du processus. <b>RandomSource</b> centralise tous les tirages. Les compteurs statiques mesurent les instances créées depuis le lancement.')
c.showPage()

# 3 - Large vector page preserves the exhaustive class inventory.
size = landscape(A2)
w, h = base('Diagramme UML de classes complet', 'Attributs, méthodes, constructeurs, visibilités et relations du programme', size, 3)
diagram('classes-complet', (42, 78, w-84, h-177))
text(48, 70, w-96, '+ public ; - privé ; # protégé ; ~ package. Souligné : statique ; italique : abstrait. Les méthodes héritées ne sont pas répétées. Les membres synthétiques du compilateur sont exclus.', size=10, leading=12, color=GREY)
c.showPage()

# 4 - Both mandatory behavioral diagrams, with both simulation loops.
w, h = base('Diagrammes de comportement', 'États des agents mobiles et activités de la simulation, incluant les deux boucles', size, 4)
diagram('etats-agent', (42, 110, (w-114)/2, h-224))
diagram('activites-simulation', ((w+30)/2, 85, (w-114)/2, h-184))
text(48, 80, (w-114)/2, 'EP : points d’énergie. La récupération et la livraison ne réactivent pas un agent épuisé. La fin de partie termine chaque état.', size=10, leading=13, color=GREY)
c.showPage()

# 5 - Explicit calibration choices and message rules.
w, h = base('Règles et déroulement d’une partie', 'Carte, déplacements, interactions, énergie et victoire', page=5)
y = h-109
y = section(48, y, w-96, 'Carte et initialisation', 'L’arène comprend <b>20 × 14 cases</b>. Les quatre zones sûres de 3 × 3 occupent 36 cases sur 280, soit 12,9 % de la surface. Chaque zone est réservée à sa faction. Une case contient au plus un occupant. De 2 à 8 agents par faction commencent dans leur zone ; les messages A à X sont répartis entre eux sans disparition, quelle que soit cette population.')
y = section(48, y, w-96, 'Un tour discret et séquentiel', 'La liste mobile est mélangée à chaque tour. Chaque agent actif agit une fois et ses effets s’appliquent immédiatement. Le minuteur Swing déclenche les tours à intervalle configuré (450 ms par défaut) ; Pause et Un tour permettent l’observation. Maîtres et agents épuisés ne bougent pas. La dernière direction permet d’éviter un blocage répété.')
y = section(48, y, w-96, 'Énergie : une dépense calibrée', 'L’énergie initiale vaut 24 par défaut (réglable de 10 à 50). Hors de sa zone, chaque case parcourue coûte 1 EP. Une collision consomme le coût des cases restant à parcourir. Sous 20 % de l’énergie maximale, le déplacement est réduit à une case orientée vers le camp. Une fin de tour dans sa zone rend 5 EP, sans dépasser le maximum. À 0 EP, l’agent devient un obstacle permanent et ses messages sont effacés.')
y = section(48, y, w-96, 'Rencontres : trois traitements', 'Une rencontre survient lorsqu’une case occupée appartient au trajet prévu ; les agents restent sur deux cases adjacentes, diagonales comprises. <b>Même faction :</b> union des messages pour les deux agents. <b>Même alliance :</b> copie aléatoire de 1 ou 2 messages inédits dans chaque sens, selon disponibilité. <b>Factions adverses :</b> gagnant aléatoire, transfert de 1 ou 2 messages inédits retirés au perdant. Les ensembles éliminent les doublons ; le journal affiche les lettres transmises.')
y = section(48, y, w-96, 'Livraison, score et fin', 'Un agent adjacent à son Maître lui copie ses messages. Seuls les messages distincts du Maître comptent. Après chaque action, une collection complète arrête la partie. Sinon, la limite de tours (250 par défaut) désigne le meilleur score ou une égalité. Les réglages proposent aussi la désactivation des alliances, un airdrop tous les 10 tours et un thème néon. L’airdrop utilise uniquement A à X et ne crée aucune lettre supplémentaire.', size=9.8)
c.showPage()

# 6 - Verified evidence and genuine anonymous roles, never invented attribution.
w, h = base('Validation et organisation', 'Exécution, conformité et contributions du groupe', page=6)
y = h-109
y = section(48, y, w-96, 'Validation du moteur', 'La suite <b>SimulationTest</b> réussit après compilation en cible Java 17. Elle vérifie voisinage 3/5/8, quatre Singletons, populations 2/5/8, 24 messages initiaux, 70 obstacles et limite de tours. À chaque tour : coordonnées, occupation unique, énergie et zones réservées. Des scénarios ciblent union, alliances, transfert adverse, collision, retour, récupération, livraison, victoire, égalité et réinitialisation. Deux exécutions avec la même graine reproduisent le même état. Le plateau est rendu dans une image ; les commandes de fenêtre restent à vérifier en session graphique interactive.', size=9.8)
y = section(48, y, w-96, 'Lancer et vérifier', '<b>Exécutable :</b> <font face="Courier">java -jar output/app/MemeCoinClash.jar</font> (Java 17 ou plus, logos inclus). <b>Windows :</b> <font face="Courier">run.cmd</font> et <font face="Courier">test.cmd</font>. <b>Unix :</b> <font face="Courier">sh run.sh</font> et <font face="Courier">sh test.sh</font>. Compiler les sources nécessite le JDK ; jouer avec le JAR ne nécessite aucune bibliothèque Java externe.', size=9.8)
y = section(48, y, w-96, 'Attentes couvertes et adaptations', 'Le sujet autorise une adaptation de l’univers et des règles (p. 1). Le projet conserve quatre populations, deux alliances, pas discrets, héritage, encapsulation, polymorphisme, compteurs statiques, quatre Maîtres Singletons, hasard centralisé et représentation graphique. Les trois diagrammes figurent aux pages 3 et 4. Les identifiants entiers A à X remplacent les chaînes de messages ; leurs ensembles assurent l’unicité.', size=9.8)
roles = TEAM['roles_anonymes']
role_text = '<br/>'.join(f'<b>Rôle {chr(65+i)} :</b> {html.escape(role)}' for i, role in enumerate(roles)) if roles else '<b>Informations attendues :</b> contributions réelles des membres. Les rôles A, B, C et éventuellement D seront décrits anonymement après confirmation, sans attribuer de travaux par supposition.'
y = section(48, y, w-96, 'Répartition anonyme des rôles', role_text, size=9.8)
y = section(48, y, w-96, 'Limites et références', 'Le retour utilise une direction simple ; le plus court chemin est un bonus (sujet, p. 6). Météorites, virus, coordination et statistiques multi-parties ne sont pas implémentés. Référence : <i>A MAS Polymorphic Simulation</i>, Concept Objet, M2 ISEN Lille, attentes p. 5-6, remise p. 7. Logos : cryptologos.cc (Doge, Shiba, Pepe) et brand.bonkcoin.com (Bonk).', size=9.3)
c.showPage()
c.save()
print(OUTPUT)
print('Team metadata complete:', bool(READY))

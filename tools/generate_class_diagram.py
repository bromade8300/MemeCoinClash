#!/usr/bin/env python3
"""Generate the exhaustive UML class diagram from compiled Java declarations."""
from pathlib import Path
import argparse
import re
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--javap', default=shutil.which('javap') or 'javap')
args = parser.parse_args()
CLASSES = [p.stem for p in (ROOT / 'src/main/java/memecoinclash').glob('*.java')]
CLASSES.sort()
lines = [
    '@startuml',
    'title MemeCoin Clash — diagramme de classes complet (déclarations du code)',
    'skinparam classAttributeIconSize 0',
    'skinparam defaultFontName SansSerif',
    'skinparam classBackgroundColor #F5F7FC',
    'skinparam classBorderColor #506078',
    'skinparam shadowing false',
    'left to right direction',
    'hide empty members',
]
relationships = []
for name in CLASSES:
    raw = subprocess.check_output([args.javap, '-classpath', str(ROOT / 'build/classes'), '-p', f'memecoinclash.{name}'], text=True)
    rows = raw.splitlines()
    declaration = next(row for row in rows if re.match(r'(public |final |abstract )?(class|interface|record|enum) memecoinclash\.', row))
    if ' extends ' in declaration:
        parent = declaration.split(' extends ')[1].split()[0].replace('memecoinclash.', '')
        if parent in CLASSES:
            relationships.append(f'{parent} <|-- {name}')
    stereotype = ' <<Singleton>>' if name in ('DogeMaster', 'ShibaMaster', 'PepeMaster', 'BonkMaster', 'RandomSource') else ''
    kind = 'abstract class' if declaration.startswith('abstract class') else 'class'
    if 'extends java.lang.Enum' in declaration:
        kind = 'enum'
    lines.append(f'{kind} {name}{stereotype} {{')
    for row in rows[rows.index(declaration)+1:]:
        row = row.strip()
        if row == '}': break
        if not row or '$' in row or 'static {}' in row: continue
        row = row.removesuffix(';')
        visibility = '+' if row.startswith('public ') else '-' if row.startswith('private ') else '#' if row.startswith('protected ') else '~'
        is_static = bool(re.search(r'\bstatic\b', row))
        is_abstract = bool(re.search(r'\babstract\b', row))
        row = re.sub(r'^(public|private|protected) ', '', row)
        row = row.replace('memecoinclash.', '').replace('java.lang.', '').replace('java.util.', '').replace('javax.swing.', '').replace('java.awt.image.', '').replace('java.awt.event.', '').replace('java.awt.', '').replace('java.io.', '')
        row = re.sub(r'\b(final|abstract|static)\b', '', row)
        row = re.sub(r'\s+', ' ', row).strip()
        modifier = '{static} ' if is_static else '{abstract} ' if is_abstract else ''
        lines.append(f'  {modifier}{visibility}{row}')
    lines.append('}')
lines += relationships
lines += [
    'Simulation *-- GameMap',
    'Simulation o-- "4" Master',
    'Simulation o-- "0..*" Agent',
    'Simulation --> GameSettings',
    'GameWindow --> Simulation',
    'GameWindow --> BoardPanel',
    'GameWindow --> SettingsDialog',
    'MemeCoinClash ..> GameWindow',
    'Simulation ..> RandomSource',
    'BoardPanel --> Simulation',
    'Agent --> Team',
    'GameMap --> Position',
    '@enduml',
]
(ROOT / 'docs/diagrammes/classes-complet.puml').write_text('\n'.join(lines) + '\n', encoding='utf-8')

#!/usr/bin/env python3
"""Export an explicit allowlist; reject incomplete metadata for a final archive."""
from pathlib import Path
import argparse
import json
import re
import zipfile
from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--draft', action='store_true')
parser.add_argument('--output-dir', type=Path, default=ROOT / 'output/remise')
args = parser.parse_args()
team = json.loads((ROOT / 'docs/equipe.json').read_text(encoding='utf-8'))
ready = (3 <= len(team['membres']) <= 4 and all(team['membres'])
         and team['referent'].get('nom') and team['referent'].get('prenom')
         and len(team['roles_anonymes']) == len(team['membres'])
         and all(team['roles_anonymes']))
if not ready and not args.draft:
    raise SystemExit('Identites, referent et roles reels requis avant la remise definitive.')
report = ROOT / 'output/pdf/MemeCoinClash_Rapport.pdf'
reader = PdfReader(report)
if len(reader.pages) != 6:
    raise SystemExit('Le rapport doit contenir exactement six pages.')
if ready and not args.draft:
    content = re.sub(r'\s+', ' ', ' '.join(page.extract_text() for page in reader.pages))
    expected_author = ' ; '.join(team['membres'])
    if (reader.metadata.author != expected_author or 'Version à compléter' in content
            or any(re.sub(r'\s+', ' ', role) not in content for role in team['roles_anonymes'])):
        raise SystemExit('Rapport non synchronise avec equipe.json : le regenerer avant export.')
jar = ROOT / 'output/app/MemeCoinClash.jar'
with zipfile.ZipFile(jar) as app:
    for logo in ['doge', 'shiba', 'pepe', 'bonk']:
        if f'assets/{logo}.png' not in app.namelist():
            raise SystemExit(f'Logo absent du JAR : {logo}')
    for member in app.namelist():
        if member.endswith('.class'):
            major = int.from_bytes(app.read(member)[6:8], 'big')
            if major != 61:
                raise SystemExit(f'Classe non compilee pour Java 17 : {member}')
            if 'Test' in member:
                raise SystemExit('Le JAR contient des classes de test.')

def clean(value):
    return re.sub(r'[^A-Za-z0-9À-ÿ_-]', '', value.strip())

name = (f"CO.MemeCoinClash.{clean(team['referent']['nom']).upper()}.{clean(team['referent']['prenom'])}.zip"
        if ready and not args.draft else 'MemeCoinClash_Dossier_a_completer.zip')
patterns = [
    'README.md', 'AGENTS.md', '.gitignore', '*.cmd', '*.ps1', '*.sh',
    'src/**/*.java', 'assets/*.png', 'docs/CONFORMITE.md', 'docs/equipe.json',
    'docs/diagrammes/*.puml', 'docs/diagrammes/*.svg', 'docs/illustrations/*.png',
    'tools/*.py', 'output/pdf/MemeCoinClash_Rapport.pdf', 'output/app/MemeCoinClash.jar',
]
files = sorted({path for pattern in patterns for path in ROOT.glob(pattern) if path.is_file()})
args.output_dir.mkdir(parents=True, exist_ok=True)
destination = args.output_dir / name
with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED) as archive:
    for path in files:
        relative = path.relative_to(ROOT).as_posix()
        info = zipfile.ZipInfo('MemeCoinClash/' + relative, (2026, 10, 1, 12, 0, 0))
        info.create_system = 3
        info.external_attr = (0o100755 if path.suffix == '.sh' else 0o100644) << 16
        archive.writestr(info, path.read_bytes(), compress_type=zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(destination) as archive:
    if archive.testzip() is not None:
        raise SystemExit('Archive corrompue.')
print(destination)
print('Files:', len(files), 'Final:', bool(ready and not args.draft))

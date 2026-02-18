import os, zipfile, json

mods_dir = r'C:\Users\35320\Desktop\Github\AnythingButFishMod\run\mods'
out_lines = []

for fname in os.listdir(mods_dir):
    if not fname.endswith('.jar'):
        continue
    fpath = os.path.join(mods_dir, fname)
    out_lines.append(f'=== {fname} ===')
    try:
        with zipfile.ZipFile(fpath, 'r') as z:
            names = z.namelist()
            if 'fabric.mod.json' in names:
                data = json.loads(z.read('fabric.mod.json').decode('utf-8'))
                aw = data.get('accessWidener', None)
                if aw:
                    out_lines.append(f'  accessWidener declared: {aw}')
                    if aw in names:
                        raw = z.read(aw)
                        first = raw.decode('utf-8', errors='replace').split('\n')[0]
                        out_lines.append(f'  First line: {first!r}')
                    else:
                        out_lines.append(f'  WARNING: {aw!r} NOT FOUND in JAR!')
                else:
                    out_lines.append('  No accessWidener declared')
            # Also list any .aw files
            aw_files = [n for n in names if n.endswith('.accesswidener') or n.endswith('.aw')]
            if aw_files:
                out_lines.append(f'  AW files in JAR: {aw_files}')
                for awf in aw_files:
                    raw = z.read(awf)
                    first = raw.decode('utf-8', errors='replace').split('\n')[0]
                    out_lines.append(f'    {awf} first line: {first!r}')
    except Exception as e:
        out_lines.append(f'  Error: {e}')

result = '\n'.join(out_lines)
print(result)
with open(r'C:\Users\35320\Desktop\Github\AnythingButFishMod\aw_check.txt', 'w', encoding='utf-8') as f:
    f.write(result)

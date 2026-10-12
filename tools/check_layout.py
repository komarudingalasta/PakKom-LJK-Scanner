"""Basic consistency checks for the fixed 50-question OMR layout."""
import json
from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'app/src/main/assets/omr_coordinates_v1.json'
data = json.loads(p.read_text(encoding='utf-8'))
assert data['templateId'] == 'PK-LJK-50-AE-V1'
assert data['page'] == {'width': 210, 'height': 297}
assert len(data['markers']) == 4
assert len(data['questions']) == 50
for i, q in enumerate(data['questions'], 1):
    assert q['number'] == i
    assert set(q['choices']) == set('ABCDE')
    for xy in q['choices'].values():
        assert 15 < xy['x'] < 195 and 15 < xy['y'] < 275
for a in data['markers']:
    assert 5 <= a['x'] <= 205 and 5 <= a['y'] <= 292
print('OMR template checks passed: 50 questions, A-E, 4 markers, A4.')

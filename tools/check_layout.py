import json
from pathlib import Path
for paper in ('A4','A5'):
    path=Path('app/src/main/assets')/f'coordinates-{paper}.json'
    d=json.loads(path.read_text())
    assert d['paper']==paper
    assert len(d['marks_mm'])==4
    assert len(d['participant_code_bubbles'])==30
    assert len(d['package_bubbles'])==4
    assert len(d['answer_bubbles'])==50
    assert [q['question'] for q in d['answer_bubbles']]==list(range(1,51))
    assert all(len(q['bubbles'])==5 for q in d['answer_bubbles'])
    for collection in [d['marks_mm'],d['participant_code_bubbles'],d['package_bubbles']]:
        for point in collection:
            assert 0<point['x_mm']<d['width_mm'] and 0<point['y_mm']<d['height_mm']
    for q in d['answer_bubbles']:
        for b in q['bubbles']:
            assert 0<b['x_mm']<d['width_mm'] and 0<b['y_mm']<d['height_mm']
    print(f'{paper}: validated 50 questions, 3-digit ID, package and markers')

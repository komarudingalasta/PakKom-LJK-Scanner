import json,math
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'app/src/main/assets'
count=0
for path in sorted(root.glob('scan-*.json')):
 d=json.loads(path.read_text()); n=d['question_count']; paper=d['paper']; opts=d['options']; answers=d['answers']; rows=(n+1)//2
 r=(2.35 if n==30 else 2.5) if paper=='A5' else (2.65 if n==50 else 3.0)
 pts=[]
 for q in range(1,n+1):
  row=answers[str(q)]; values=[row[k] for k in opts]
  assert len({round(p[1],2) for p in values})==1,(path,q,'unaligned')
  pts.extend((x,y,q,k) for k,(x,y) in row.items())
 for i,(x,y,q,k) in enumerate(pts):
  assert 11<x<d['paper_mm'][0]-11 and 99<y<d['paper_mm'][1]-13,(path,q,x,y)
  for xx,yy,qq,kk in pts[:i]:
   assert math.hypot(x-xx,y-yy)>2*r+0.2,(path,q,qq,'overlap')
 for q in range(1,n+1):
  expected=0 if q<=rows else 1
  x=answers[str(q)][opts[0]][0]
  assert (x<d['paper_mm'][0]/2)==(expected==0),(path,q,'column')
 for field in d['identity_bubbles'].values():
  for x,y in field.values(): assert 46<y<93,(path,'identity outside')
 print(f'{path.name}: OK ({n} questions, {len(opts)} choices)')
 count+=1
assert count==15,count
print('PASS: 12 format combinations (15 JSON files); no answer bubble collisions')

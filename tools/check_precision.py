import json,math
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'app/src/main/assets'
paths=sorted(root.glob('scan-*.json'))
assert len(paths)==15
for path in paths:
 d=json.loads(path.read_text()); w,h=d['paper_mm']; a=d['answers']; n=d['question_count']; r=2.35 if d['paper']=='A5' and n==30 else 2.5 if d['paper']=='A5' else 2.65 if n==50 else 3.0
 marks=d['registration_marks_mm']; assert len(marks)==4
 for x,y in marks: assert x-3>=4 and y-3>=4 and x+3<=w-4 and y+3<=h-4,(path,'mark off page')
 bubbles=[]
 for q in range(1,n+1):
  row=a[str(q)]
  assert set(row)==set(d['options'])
  for k,(x,y) in row.items():
   assert 10<x-r and x+r<w-10 and 98<y-r and y+r<h-14,(path,q,k,'outside')
   for xx,yy in bubbles: assert math.hypot(x-xx,y-yy)>2*r+0.2,(path,q,k,'collision')
   bubbles.append((x,y))
 identity=d['identity_bubbles']; ir=2.25 if d['paper']=='A5' else 3
 ip=[]
 for field in identity.values():
  for x,y in field.values():
   assert 15<x-ir and x+ir<w-15 and 41<y-ir and y+ir<(85 if d['paper']=='A5' else 99),(path,'identity outside box')
   for xx,yy in ip: assert math.hypot(x-xx,y-yy)>2*ir+0.15,(path,'identity collision')
   ip.append((x,y))
 print(path.name,'PASS')
print('PASS:',len(paths),'JSON templates')

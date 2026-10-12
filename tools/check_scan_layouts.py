import json,pathlib
root=pathlib.Path(__file__).resolve().parents[1]/'app/src/main/assets'
for name,n,options in [('scan-A5-20.json',20,'ABCD'),('scan-A5-30.json',30,'ABCDE'),('scan-A4-50.json',50,'ABCDE')]:
 d=json.loads((root/name).read_text())
 assert d['question_count']==n and d['options']==options
 assert len(d['registration_marks_mm'])==4 and len(d['participant_code'])==3 and len(d['answers'])==n
 for row in d['participant_code'].values():assert len(row)==10
 for row in d['answers'].values():assert set(row)==set(options)
 print(name,'OK')

-- Aluno demo precisa de disciplina para ACL de materiais da turma
UPDATE users
SET subject_ids = ARRAY['MAT']
WHERE username = 'aluno'
  AND tenant_id = '11111111-1111-1111-1111-111111111111';

-- Professor demo enxerga materiais da turma 8A de Matemática
UPDATE users
SET class_ids = ARRAY['8A']
WHERE username = 'prof'
  AND tenant_id = '11111111-1111-1111-1111-111111111111';

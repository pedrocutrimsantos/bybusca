# Setup do banco local (PostgreSQL instalado no Windows)
#
# 1) Abra um terminal e rode (vai pedir a senha do usuário postgres):
#    & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -f scripts\setup-db.sql
#
# 2) Confirme:
#    & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U bybusca -h localhost -d bybusca -c "SELECT 1;"
#    (senha padrão do script: bybusca)
#
# 3) Suba a API:
#    cd apps\api ; .\gradlew.bat bootRun
#
# 4) Suba o front:
#    cd apps\web ; npm run dev

Write-Host "Use o comando psql do comentario acima com a senha do seu usuario postgres."

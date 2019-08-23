@echo off
setlocal EnableDelayedExpansion
rem change to the directory
set /p workspace="Insira o caminho do Workspace (ou local a partir de onde serao procurados os repositorios): "
echo %workspace%
cd %workspace%

for /F "delims=" %%a in ('dir /S /P /b .git /AD') do (
   echo %%a
   cd %%a
   cd ../
   call git fetch -p  
)
endlocal
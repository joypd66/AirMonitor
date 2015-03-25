rem @echo off

set target=C:\android\projects\AirMonitor\doc\
set javalink=http://docs.oracle.com/javase/8/docs/api/
set androidlink=http://developer.android.com/reference/

REM This must be updated to match your local file structure
set androidpackages=C:/android/reference

set log=%target%\doc.log

if not exist "%target%" md "%target%"

for %%f in ("%target%*.*") do (del /q "%%f")

for /d %%d in ("%target%*") do (rd /q /s "%%d")

javadoc -private -verbose -d %target% -link %javalink% -linkoffline %androidlink% %androidpackages% @packages.txt > "%log%" 2>&1

pause
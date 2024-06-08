call ./gradlew desktop:dist
RMDIR /S /Q "..\RTDExported"
call java -jar packr-all-4.0.0.jar config.json
REM xcopy "desktop\build\libs\rtd.jar" "..\RTDExported"
xcopy "assets" "..\RTDExported\assets\" /s /e /A
REM ren "../RTDExported/desktop-1.0.jar" "rtd.jar"
REM to zip: jar -cfM "../RTD.zip" "../RTDExported/"
pause
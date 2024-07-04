call echo Building the desktop build of the game...
call ./gradlew desktop:dist
RMDIR /S /Q "..\PTTDExported"
call echo Converting the game into an executable file...
call java -jar packr-all-4.0.0.jar config.json
call echo Game successfully converted to executable!
call echo Copying game files to final folder...
REM xcopy "desktop\build\libs\pttd.jar" "..\PTTDExported"
xcopy "assets" "..\PTTDExported\assets\" /s /e /A
call echo Finished copying game assets to the final folder
call echo Compressing exported game...
jar -cfM "../PTTD.zip" "../PTTDExported/"
call echo Successfully exported and compressed game!
pause
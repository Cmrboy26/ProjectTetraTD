@echo Creating executable for the desktop. NOTE! Make sure that the JDK is correct in "config.json" according to the machine.
@echo Ensure that the directory above this folder called "RTDGame" is DELETED so the project can output there.
.\gradlew desktop:dist && java -jar packr-all-4.0.0.jar config.json
@pause
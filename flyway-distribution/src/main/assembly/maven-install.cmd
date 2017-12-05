@echo off
echo ============== INSTALLING Flyway jar and pom files in your local Maven repository
pause

SET FLYWAY_VERSION=${project.version}
SET GROUP_ID=org.flywaydb.trial

call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-parent\pom.xml                                   -DartifactId=flyway-parent        -Dversion=%VERSION% -Dpackaging=pom
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-core\pom.xml                                     -DartifactId=flyway-core          -Dversion=%VERSION% -Dpackaging=pom
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-core\flyway-core-%VERSION%.jar                   -DartifactId=flyway-core          -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-core\flyway-core-%VERSION%-javadoc.jar
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-commandline\pom.xml                              -DartifactId=flyway-commandline   -Dversion=%VERSION% -Dpackaging=pom
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-commandline\flyway-commandline-%VERSION%.jar     -DartifactId=flyway-commandline   -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-commandline\flyway-commandline-%VERSION%-javadoc.jar
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-maven-plugin\pom.xml                             -DartifactId=flyway-maven-plugin  -Dversion=%VERSION% -Dpackaging=pom
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-maven-plugin\flyway-maven-plugin-%VERSION%.jar   -DartifactId=flyway-maven-plugin  -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-maven-plugin\flyway-maven-plugin-%VERSION%-javadoc.jar
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-gradle-plugin\pom.xml                            -DartifactId=flyway-gradle-plugin -Dversion=%VERSION% -Dpackaging=pom
call mvn install:install-file -DgroupId=%GROUP_ID% -Dfile=flyway-gradle-plugin\flyway-gradle-plugin-%VERSION%.jar -DartifactId=flyway-gradle-plugin -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-gradle-plugin\flyway-gradle-plugin-%VERSION%-javadoc.jar

echo ============== SUCCESS
pause
goto :EOF

:error
echo ============== FAILED WITH ERROR %errorlevel%
cd c:\Workspaces\flyway-master
exit /b %errorlevel%
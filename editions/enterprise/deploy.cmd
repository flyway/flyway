@echo off

setlocal

set VERSION=%1
set GROUP_ID=org.flywaydb.enterprise
set REPOSITORY=flyway-repo
set URL=s3://flyway-repo/release

call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=pom.xml -DgroupId=%GROUP_ID% -DartifactId=flyway-parent -Dversion=%VERSION% -Dpackaging=pom -DupdateReleaseInfo=true
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-core/target/flyway-core-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-core -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-core/target/flyway-core-%VERSION%-javadoc.jar -Dsources=flyway-core/target/flyway-core-%VERSION%-sources.jar -DpomFile=flyway-core/pom.xml -DupdateReleaseInfo=true
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-gradle-plugin/target/flyway-gradle-plugin-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-gradle-plugin -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-gradle-plugin/target/flyway-gradle-plugin-%VERSION%-javadoc.jar -Dsources=flyway-gradle-plugin/target/flyway-gradle-plugin-%VERSION%-sources.jar -DpomFile=flyway-gradle-plugin/pom.xml -DupdateReleaseInfo=true
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-maven-plugin/target/flyway-maven-plugin-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-maven-plugin -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-maven-plugin/target/flyway-maven-plugin-%VERSION%-javadoc.jar -Dsources=flyway-maven-plugin/target/flyway-maven-plugin-%VERSION%-sources.jar -DpomFile=flyway-maven-plugin/pom.xml -DupdateReleaseInfo=true
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-commandline/target/flyway-commandline-%VERSION%-javadoc.jar -Dsources=flyway-commandline/target/flyway-commandline-%VERSION%-sources.jar -DpomFile=flyway-commandline/pom.xml -DupdateReleaseInfo=true
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%-windows-x64.zip -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=zip -DgeneratePom=false -Dclassifier=windows-x64
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%-linux-x64.tar.gz -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=linux-x64
call mvn -f pom.xml deploy:deploy-file -Durl=%URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%-macosx-x64.tar.gz -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=macosx-x64

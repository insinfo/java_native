 
## documentação gradle
https://docs.gradle.org/current/samples/sample_building_java_applications.html
## para criar a estrutura de um projeto gradle
gradle init  
## para inicializar as variaveis de ambiente de compilação C++
"C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvars64.bat"
or
"C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvarsall.bat" x64

or
## powershell not working
powershell.exe -noe -c '&{Import-Module """C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\Common7\Tools\Microsoft.VisualStudio.DevShell.dll"""; Enter-VsDevShell 1213e5af}'

or 

pwsh -noe -c '&{Import-Module """C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\Common7\Tools\Microsoft.VisualStudio.DevShell.dll"""; Enter-VsDevShell 1213e5af}'

set __VCVARSALL_TARGET_ARCH=x64
set __VCVARSALL_HOST_ARCH=x64
set __local_ARG_FOUND=1

## para compilar para byte code  sem gradle
javac  .\Example.java
## para compilar para codigo nativo sem gradle
native-image Example
or
jar cfe  Example.jar Example Example.class 
native-image -jar Example.jar

## para executar o app com gradle
./gradlew run --stacktrace

## para build com reflexão com gradle
./gradlew -Pagent nativeBuild
./gradlew nativeBuild
or
.\gradlew -Pagent nativeBuild
.\gradlew nativeBuild

## para executar o executavel final
.\app\build\native\nativeBuild\Example.exe
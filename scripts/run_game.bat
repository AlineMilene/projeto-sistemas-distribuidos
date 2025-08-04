@echo off

rem Define o caminho para a sua instalacao do Zookeeper
set ZOOKEEPER_PATH=C:\zookeeper\apache-zookeeper-3.9.3-bin

rem Define o CLASSPATH incluindo os JARs corretos do Zookeeper e do SLF4J
set CP=.;%ZOOKEEPER_PATH%\lib\zookeeper-3.9.3.jar;%ZOOKEEPER_PATH%\lib\zookeeper-jute-3.9.3.jar;%ZOOKEEPER_PATH%\lib\slf4j-api-1.7.30.jar;%ZOOKEEPER_PATH%\lib\netty-buffer-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-codec-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-common-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-handler-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-resolver-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-transport-4.1.113.Final.jar

echo Compilando JokenpoGame.java...
javac -cp "%CP%" JokenpoGame.java

if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilacao.
    pause
    exit /b
)

echo Executando o Jogo...
java -cp "%CP%" JokenpoGame localhost:2181
pause
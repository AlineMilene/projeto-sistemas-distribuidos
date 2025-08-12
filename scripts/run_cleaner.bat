@echo off

rem Caminho da instalacao do Zookeeper
set ZOOKEEPER_PATH=C:\zookeeper\apache-zookeeper-3.9.3-bin

rem Definindo CLASSPATH com todos os jars necessários
set CP=.;%ZOOKEEPER_PATH%\lib\zookeeper-3.9.3.jar;%ZOOKEEPER_PATH%\lib\zookeeper-jute-3.9.3.jar;%ZOOKEEPER_PATH%\lib\slf4j-api-1.7.30.jar;%ZOOKEEPER_PATH%\lib\netty-buffer-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-codec-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-common-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-handler-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-resolver-4.1.113.Final.jar;%ZOOKEEPER_PATH%\lib\netty-transport-4.1.113.Final.jar

echo Executando limpeza do ZooKeeper no caminho /jokenpo...

javac -cp "%CP%" ZkCleaner.java
java -cp "%CP%" ZkCleaner

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao executar a limpeza.
    pause
    exit /b
)

echo Limpeza concluída com sucesso.
pause

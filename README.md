# JokenpoGame com Apache ZooKeeper 🤝

Este projeto implementa um jogo de Joquempô (pedra, papel, tesoura) para dois jogadores, utilizando o Apache ZooKeeper para sincronização e coordenação entre os participantes.

## Pré-requisitos ⚙️

Antes de começar, certifique-se de que você tem:

* **Java Development Kit (JDK) 8 ou superior**: Necessário para compilar e executar o código Java.
* **Apache ZooKeeper**: O servidor de coordenação. Baixe-o do [site oficial do Apache ZooKeeper](https://zookeeper.apache.org/releases.html).
* **Classpath configurado**: O arquivo JAR do ZooKeeper (`zookeeper-x.x.x.jar`) deve estar no seu `CLASSPATH`.

---

## Estrutura do Projeto 📂

O projeto consiste em dois arquivos principais:

* `JokenpoGame.java`: O código-fonte do jogo.
* `run_game.bat`: Um script para compilar e iniciar duas instâncias do jogo em paralelo, facilitando a execução no Windows.

---

## Configuração do ZooKeeper 🛠️

1.  **Baixe e Descompacte**: Extraia o arquivo `.tar.gz` do ZooKeeper em um diretório de sua preferência (ex: `C:\zookeeper`).
2.  **Ajuste o arquivo de configuração**: Na pasta `conf`, copie `zoo_sample.cfg` para `zoo.cfg`.
3.  **Inicie o servidor**: Abra um terminal e navegue até a pasta `bin` do ZooKeeper. Execute o comando:
    ```
    zkServer.cmd
    ```
    Mantenha este terminal aberto enquanto joga.

---

## Como Rodar o Jogo 🚀

1.  **Ajuste o `run_game.bat`**: Edite o arquivo `run_game.bat` para garantir que os caminhos do `ZOOKEEPER_HOME` e do arquivo JAR (`zookeeper-3.9.3.jar`) estejam corretos para a sua instalação.
2.  **Execute o `run_game.bat`**: Abra um terminal na mesma pasta onde estão `JokenpoGame.java` e `run_game.bat` e execute o script.
    ```
    run_game.bat
    ```
3.  **Início da Partida**: O script irá compilar o código e abrirá duas novas janelas de terminal, uma para cada jogador.
4.  **Jogando**: Siga as instruções em cada terminal. Os jogadores deverão escolher entre `Pedra`, `Papel` ou `Tesoura`.
5.  **Fim do Jogo**: A partida termina quando um jogador alcançar 2 vitórias. O placar final será exibido no terminal.

---

## Observações 📝

* **Conflitos**: O `run_game.bat` inclui uma pausa (`timeout /t 5`) entre as execuções para garantir que os jogadores iniciem em momentos ligeiramente diferentes, evitando conflitos de sincronização.
* **ZooKeeper**: O jogo depende de um servidor ZooKeeper em execução. Se você fechar o servidor, o jogo não funcionará.
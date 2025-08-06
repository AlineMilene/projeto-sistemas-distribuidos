# Projeto Final: Jokenpo Distribuído com ZooKeeper 🤝

Este projeto é uma aplicação distribuída do jogo Jokenpo (pedra, papel e tesoura) implementada em **Java** e que utiliza o **Apache ZooKeeper** para coordenar a comunicação e a sincronização entre dois jogadores. A aplicação explora conceitos de sistemas distribuídos, como barreiras, filas, locks e eleição de líder.

---

## Como Rodar ⚙️

### Pré-requisitos
Para executar o jogo, certifique-se de ter os seguintes itens instalados:

* **Apache ZooKeeper:** Deve estar instalado e rodando em `localhost:2181`.
* **Java 11+:** O ambiente de execução Java deve estar instalado.
* **Classpath:** O arquivo JAR do ZooKeeper (`zookeeper-x.x.x.jar`) deve estar configurado no seu `CLASSPATH`.

### Passos para executar 🚀
1.  **Ajuste o script `run_game.bat`:** Edite o arquivo `run_game.bat` para garantir que os caminhos para o `ZOOKEEPER_HOME` e o arquivo JAR (`zookeeper-3.9.3.jar`) estejam corretos, de acordo com a sua instalação.

2.  **Execute o script:** Abra um terminal no diretório do projeto e execute o script.

    ```
    run_game.bat
    ```

3.  **Inicie a partida:** Execute duas instâncias do comando acima em terminais diferentes para que os dois jogadores se conectem.

4.  **Limpeza de ambiente:** Caso precise, o arquivo `run_cleaner.bat` pode ser usado para limpar o ambiente do ZooKeeper e evitar conflitos de sincronização com nós pré-existentes.

---

## Como Jogar
* O primeiro jogador a entrar na fila se torna o **líder**.
* O segundo jogador é o **desafiante**.
* O líder aguarda o desafiante se conectar para iniciar o jogo.
* A partida consiste em **3 rodadas**.
* Em cada rodada, ambos os jogadores fazem suas jogadas (pedra, papel ou tesoura) e o resultado é exibido.
* Ao final, o placar final e o vencedor são declarados.

---

## Funcionalidades e Requisitos Cumpridos
O projeto usa o ZooKeeper para implementar as seguintes funcionalidades:

### 1. Barriers
A sincronização do início da partida é feita através de uma barreira. O método `waitForSecondPlayer()` garante que o jogo só comece quando os dois jogadores estiverem conectados.

### 2. Queues (Filas)
A fila de jogadores é gerenciada no caminho `/jokenpo/queue` usando **nós sequenciais efêmeros**. O primeiro jogador a criar um nó se torna o líder (posição 0 na fila), e o segundo é o desafiante. O método `waitForTurn()` define o papel de cada jogador.

### 3. Locks
Locks de exclusividade são gerenciados de forma implícita, usando os nós sequenciais efêmeros para garantir que apenas um jogador seja o líder em um dado momento, seguindo o padrão de locks distribuídos do ZooKeeper.

### 4. Eleição de Líder
A eleição de líder é implementada pela ordenação dos nós na fila `/jokenpo/queue`. O jogador com o nó de menor número é automaticamente eleito o líder, e os outros aguardam sua vez e o reconhecem.

---

## Estrutura do ZooKeeper
A aplicação utiliza a seguinte estrutura de diretórios no ZooKeeper para gerenciar o estado do jogo:

* `/jokenpo` (raiz do jogo)
    * `/jokenpo/queue`: Fila de jogadores (nós efêmeros sequenciais).
    * `/jokenpo/moves`: Nós para armazenar as jogadas de cada rodada.
    * `/jokenpo/locks`, `/jokenpo/barrier`, `/jokenpo/election`: Diretórios criados para organizar as funcionalidades do projeto, podendo ser expandidos em futuras implementações.

---

## Funcionamento Técnico
Cada jogador cria um nó efêmero sequencial para entrar na fila. O líder então inicia a criação dos nós para as jogadas de cada rodada. A sincronização para a leitura e escrita das jogadas é feita através de **polling** (uso de `Thread.sleep`) para aguardar a disponibilidade dos dados.

Ao final das 3 rodadas, o sistema compara as jogadas, contabiliza os pontos e exibe o resultado final.

### Observações
O jogo depende de um servidor **ZooKeeper em execução**. Se o servidor for desligado, a aplicação não funcionará corretamente.
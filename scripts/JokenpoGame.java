import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class JokenpoGame implements Watcher {
    private static final String ZK_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;

    private static final String ROOT_PATH = "/jokenpo";
    private static final String QUEUE_PATH = ROOT_PATH + "/queue";
    private static final String LOCKS_PATH = ROOT_PATH + "/locks";
    private static final String MOVES_PATH = ROOT_PATH + "/moves";
    private static final String BARRIER_PATH = ROOT_PATH + "/barrier";
    private static final String ELECTION_PATH = ROOT_PATH + "/election";

    private static final int NUM_ROUNDS = 3;

    private ZooKeeper zk;
    private String playerId;
    private boolean isLeader = false;

    private int leaderScore = 0;
    private int secondPlayerScore = 0;

    public static void main(String[] args) throws Exception {
        new JokenpoGame().start();
    }

    private void start() throws IOException, KeeperException, InterruptedException {
        connectToZookeeper();
        ensureRootPaths();

        this.playerId = UUID.randomUUID().toString();
        registerPlayer();

        if (isLeader) {
            System.out.println("Voce entrou no jogo. Voce eh o lider.");
            waitForSecondPlayer();
            createMovesNode();
            playGameAsLeader();
        } else {
            System.out.println("Voce entrou no jogo. Voce eh o desafiante.");
            waitForMovesNode();
            playGameAsSecondPlayer();
        }

        printFinalResult();

        zk.close();
    }

    private void connectToZookeeper() throws IOException {
        zk = new ZooKeeper(ZK_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void ensureRootPaths() throws KeeperException, InterruptedException {
        createZNodeIfNotExists(ROOT_PATH);
        createZNodeIfNotExists(QUEUE_PATH);
        createZNodeIfNotExists(LOCKS_PATH);
        createZNodeIfNotExists(MOVES_PATH);
        createZNodeIfNotExists(BARRIER_PATH);
        createZNodeIfNotExists(ELECTION_PATH);
    }

    private void createZNodeIfNotExists(String path) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(path, false);
        if (stat == null) {
            zk.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    private void registerPlayer() throws KeeperException, InterruptedException {
        String path = zk.create(QUEUE_PATH + "/player-", playerId.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        waitForTurn(path);
    }

    private void waitForTurn(String path) throws KeeperException, InterruptedException {
        while (true) {
            List<String> children = zk.getChildren(QUEUE_PATH, false);
            Collections.sort(children);
            String first = children.get(0);
            String myNode = path.substring(QUEUE_PATH.length() + 1);
            if (first.equals(myNode)) {
                isLeader = true;
                break;
            } else if (children.size() > 1 && myNode.equals(children.get(1))) {
                break;
            } else {
                System.out.println("Esperando sua vez de jogar...");
                Thread.sleep(1000);
            }
        }
    }

    private void waitForSecondPlayer() throws KeeperException, InterruptedException {
        while (true) {
            List<String> children = zk.getChildren(QUEUE_PATH, false);
            if (children.size() >= 2) {
                break;
            }
            Thread.sleep(1000);
        }
    }

    private void createMovesNode() throws KeeperException, InterruptedException {
        for (int i = 1; i <= NUM_ROUNDS; i++) {
            createZNodeIfNotExists(MOVES_PATH + "/round" + i + "-leader");
            createZNodeIfNotExists(MOVES_PATH + "/round" + i + "-second");
        }
    }

    private void waitForMovesNode() throws KeeperException, InterruptedException {
        while (zk.exists(MOVES_PATH + "/round1-leader", false) == null) {
            Thread.sleep(1000);
        }
    }

    private void playGameAsLeader() throws KeeperException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        for (int round = 1; round <= NUM_ROUNDS; round++) {
            System.out.println("\nRodada " + round + ":");

            System.out.print("Digite sua jogada (pedra, papel ou tesoura): ");
            String leaderMove = scanner.nextLine();
            String movePathLeader = MOVES_PATH + "/round" + round + "-leader";
            zk.setData(movePathLeader, leaderMove.getBytes(), -1);

            System.out.println("Esperando jogada do desafiante...");
            String movePathSecond = MOVES_PATH + "/round" + round + "-second";
            while (zk.getData(movePathSecond, false, null).length == 0) {
                Thread.sleep(1000);
            }

            String secondMove = new String(zk.getData(movePathSecond, false, null));
            int result = compareMoves(leaderMove, secondMove);

            printRoundResult("Líder", leaderMove, result);
        }
    }

    private void playGameAsSecondPlayer() throws KeeperException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        for (int round = 1; round <= NUM_ROUNDS; round++) {
            System.out.println("\nRodada " + round + ":");

            String movePathLeader = MOVES_PATH + "/round" + round + "-leader";
            while (zk.getData(movePathLeader, false, null).length == 0) {
                Thread.sleep(1000);
            }

            System.out.print("Digite sua jogada (pedra, papel ou tesoura): ");
            String secondMove = scanner.nextLine();
            String movePathSecond = MOVES_PATH + "/round" + round + "-second";
            zk.setData(movePathSecond, secondMove.getBytes(), -1);

            String leaderMove = new String(zk.getData(movePathLeader, false, null));
            int result = compareMoves(leaderMove, secondMove);

            printRoundResult("Desafiante", secondMove, -result); // inverte resultado
        }
    }

    private void printRoundResult(String jogador, String jogada, int resultado) {
        String mensagem = jogador + " jogou: " + jogada + " e ";

        if (resultado > 0) {
            mensagem += "venceu esta rodada.";
            if (jogador.equals("Lider"))
                leaderScore++;
            else
                secondPlayerScore++;
        } else if (resultado < 0) {
            mensagem += "perdeu esta rodada.";
            if (jogador.equals("Lider"))
                secondPlayerScore++;
            else
                leaderScore++;
        } else {
            mensagem += "esta rodada deu empate.";
        }

        System.out.println(mensagem);
    }

    private void printFinalResult() {
        System.out.println("\nFim do jogo. Resultado Final:");
        System.out.println("Placar - Lider: " + leaderScore + " | Desafiante: " + secondPlayerScore);

        if (leaderScore == secondPlayerScore) {
            System.out.println("Empate!");
        } else if ((leaderScore > secondPlayerScore && isLeader) || (secondPlayerScore > leaderScore && !isLeader)) {
            System.out.println("Voce venceu!");
        } else {
            System.out.println("Voce perdeu!");
        }

        System.out.println("Obrigado por jogar!");

        if(isLeader){
            new ZkCleaner().clean("/jokenpo");
            System.out.println("Limpeza concluida.");
        }
    }

    private int compareMoves(String move1, String move2) {
        move1 = move1.toLowerCase();
        move2 = move2.toLowerCase();

        if (move1.equals(move2))
            return 0;

        return switch (move1) {
            case "pedra" -> move2.equals("tesoura") ? 1 : -1;
            case "papel" -> move2.equals("pedra") ? 1 : -1;
            case "tesoura" -> move2.equals("papel") ? 1 : -1;
            default -> 0;
        };
    }

    @Override
    public void process(WatchedEvent event) {
        // No-op
    }
}

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class JokenpoGame implements Watcher {
    private static final String ZK_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ROOT_PATH = "/jokenpo";
    private static final String PLAYERS_PATH = ROOT_PATH + "/players";
    private static final String MOVES_PATH = ROOT_PATH + "/moves";
    private static final int NUM_ROUNDS = 3;

    private ZooKeeper zk;
    private String playerId;
    private boolean isLeader;

    public static void main(String[] args) throws Exception {
        new JokenpoGame().start();
    }

    private void start() throws IOException, KeeperException, InterruptedException {
        connectToZookeeper();
        ensureRootPaths();

        this.playerId = UUID.randomUUID().toString();
        registerPlayer();

        if (isLeader) {
            System.out.println("Você é o líder (juiz). Aguardando o segundo jogador...");
            waitForSecondPlayer();
            createMovesNode();
            System.out.println("Segundo jogador detectado. Iniciando o jogo...");
            playGameAsLeader();
        } else {
            System.out.println("Você é o segundo jogador. Aguarde o início da partida...");
            waitForMovesNode();
            playGameAsSecondPlayer();
        }

        zk.close();
    }

    private void connectToZookeeper() throws IOException {
        zk = new ZooKeeper(ZK_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void ensureRootPaths() throws KeeperException, InterruptedException {
        createZNodeIfNotExists(ROOT_PATH, new byte[0]);
        createZNodeIfNotExists(PLAYERS_PATH, new byte[0]);
    }

    private void registerPlayer() throws KeeperException, InterruptedException {
        List<String> players = zk.getChildren(PLAYERS_PATH, false);

        if (players.isEmpty()) {
            isLeader = true;
        }

        String path = PLAYERS_PATH + "/player-";
        zk.create(path, playerId.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    private void waitForSecondPlayer() throws KeeperException, InterruptedException {
        while (zk.getChildren(PLAYERS_PATH, false).size() < 2) {
            Thread.sleep(1000);
        }
    }

    private void createMovesNode() throws KeeperException, InterruptedException {
        createZNodeIfNotExists(MOVES_PATH, new byte[0]);
    }

    private void waitForMovesNode() throws KeeperException, InterruptedException {
        Stat stat;
        do {
            stat = zk.exists(MOVES_PATH, false);
            Thread.sleep(1000);
        } while (stat == null);
    }

    private void playGameAsLeader() throws KeeperException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        int leaderScore = 0;
        int secondPlayerScore = 0;

        for (int round = 1; round <= NUM_ROUNDS; round++) {
            System.out.println("\nRodada " + round + ":");

            System.out.print("Digite sua jogada (pedra, papel ou tesoura): ");
            String leaderMove = scanner.nextLine();
            String movePathLeader = MOVES_PATH + "/round" + round + "-leader";
            zk.create(movePathLeader, leaderMove.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            System.out.println("Aguardando jogada do segundo jogador...");
            String movePathSecond = MOVES_PATH + "/round" + round + "-second";
            while (zk.exists(movePathSecond, false) == null) {
                Thread.sleep(1000);
            }

            String secondMove = new String(zk.getData(movePathSecond, false, null));
            System.out.println("Segundo jogador escolheu: " + secondMove);

            int result = compareMoves(leaderMove, secondMove);
            if (result > 0) {
                leaderScore++;
            } else if (result < 0) {
                secondPlayerScore++;
            }
        }

        printResult(leaderScore, secondPlayerScore, true);
    }

    private void playGameAsSecondPlayer() throws KeeperException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        int leaderScore = 0;
        int secondPlayerScore = 0;

        for (int round = 1; round <= NUM_ROUNDS; round++) {
            System.out.println("\nRodada " + round + ":");

            String movePathLeader = MOVES_PATH + "/round" + round + "-leader";
            while (zk.exists(movePathLeader, false) == null) {
                Thread.sleep(1000);
            }

            System.out.print("Digite sua jogada (pedra, papel ou tesoura): ");
            String secondMove = scanner.nextLine();
            String movePathSecond = MOVES_PATH + "/round" + round + "-second";
            zk.create(movePathSecond, secondMove.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            String leaderMove = new String(zk.getData(movePathLeader, false, null));
            System.out.println("Líder escolheu: " + leaderMove);

            int result = compareMoves(leaderMove, secondMove);
            if (result > 0) {
                leaderScore++;
            } else if (result < 0) {
                secondPlayerScore++;
            }
        }

        printResult(leaderScore, secondPlayerScore, false);
    }

    private int compareMoves(String move1, String move2) {
        move1 = move1.toLowerCase();
        move2 = move2.toLowerCase();

        if (move1.equals(move2))
            return 0;

        switch (move1) {
            case "pedra":
                return move2.equals("tesoura") ? 1 : -1;
            case "papel":
                return move2.equals("pedra") ? 1 : -1;
            case "tesoura":
                return move2.equals("papel") ? 1 : -1;
            default:
                return 0;
        }
    }

    private void printResult(int leaderScore, int secondPlayerScore, boolean isLeader) {
        System.out.println("\nResultado Final:");
        System.out.println("Placar - Juiz: " + leaderScore + " | Segundo Jogador: " + secondPlayerScore);
        if (leaderScore == secondPlayerScore) {
            System.out.println("Empate!");
        } else if ((leaderScore > secondPlayerScore && isLeader) || (secondPlayerScore > leaderScore && !isLeader)) {
            System.out.println("Você venceu!");
        } else {
            System.out.println("Você perdeu!");
        }
    }

    private void createZNodeIfNotExists(String path, byte[] data) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(path, false);
        if (stat == null) {
            zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        // No-op
    }
}

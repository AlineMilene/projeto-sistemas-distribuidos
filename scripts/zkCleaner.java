import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class ZkCleaner implements Watcher {

    private static final String ZK_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;

    private ZooKeeper zk;

    public static void main(String[] args) throws Exception {
        new ZkCleaner().clean("/jokenpo");
    }

    public void clean(String path) throws IOException, InterruptedException, KeeperException {
        connect();
        deleteRecursive(path);
        zk.close();
        System.out.println("Limpeza completa do caminho " + path);
    }

    private void connect() throws IOException {
        zk = new ZooKeeper(ZK_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void deleteRecursive(String path) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(path, false);
        if (stat == null) {
            // nó não existe, nada a fazer
            return;
        }
        List<String> children = zk.getChildren(path, false);
        for (String child : children) {
            deleteRecursive(path + "/" + child);
        }
        zk.delete(path, -1);
        System.out.println("Deletado: " + path);
    }

    @Override
    public void process(WatchedEvent event) {
        // No-op
    }
}

package study.cryptochain.minicoin.config;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minicoin.node")
public class NodeProperties {

    private String id = "node-1";
    private Integer port;
    private List<URI> peers = new ArrayList<>();
    private Duration syncInterval = Duration.ofSeconds(30);
    private int syncRetryAttempts = 2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<URI> getPeers() {
        return List.copyOf(peers);
    }

    public void setPeers(List<URI> peers) {
        this.peers = peers == null ? new ArrayList<>() : new ArrayList<>(peers);
    }

    public Duration getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(Duration syncInterval) {
        if (syncInterval != null) {
            this.syncInterval = syncInterval;
        }
    }

    public int getSyncRetryAttempts() {
        return syncRetryAttempts;
    }

    public void setSyncRetryAttempts(int syncRetryAttempts) {
        this.syncRetryAttempts = Math.max(1, syncRetryAttempts);
    }
}

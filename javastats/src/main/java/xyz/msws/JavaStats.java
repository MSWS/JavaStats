package xyz.msws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import xyz.msws.formatter.Formatter;
import xyz.msws.formatter.ForumsFormat;
import xyz.msws.parser.GTParser;
import xyz.msws.server.AWSServerData;
import xyz.msws.server.ServerConfig;
import xyz.msws.server.ServerData;
import xyz.msws.server.StatConfig;

@SpringBootApplication
@RestController
public class JavaStats extends TimerTask {
    private Map<String, ServerData> servers = new HashMap<>();
    private List<ServerConfig> configs = new ArrayList<>();
    private GTParser parser;
    private final Timer timer = new Timer();
    private String data = null;
    private long lastRun = 0;
    private AmazonS3 client;

    public static void main(String[] args) {
        new JavaStats();
        SpringApplication.run(JavaStats.class, new String[0]);
    }

    public JavaStats() {
        try {
            client = AmazonS3ClientBuilder.standard().withCredentials(new EnvironmentVariableCredentialsProvider())
                    .withRegion(Regions.DEFAULT_REGION).build();
        } catch (Exception e) {
            throw e;
        }

        configs.add(new ServerConfig("jb.csgo.edgegamers.cc:27015", "Jailbreak"));
        configs.add(new ServerConfig("ttt.csgo.edgegamers.cc:27015", "Trouble in Terrorist Town"));
        configs.add(new ServerConfig("ttt.csgo.edgegamers.cc:27015", "Surf"));
        configs.add(new ServerConfig("surf.csgo.edgegamers.cc:27015", "Surf Summer"));
        configs.add(new ServerConfig("mg.csgo.edgegamers.cc:27015", "Minigames"));
        configs.add(new ServerConfig("bhop.csgo.edgegamers.cc:27015", "Bhop"));
        configs.add(new ServerConfig("104.128.58.205:27015", "AWP Only"));
        configs.add(new ServerConfig("104.128.58.204:27015", "KZ Climb"));

        configs.sort((a, b) -> a.getName().compareTo(b.getName()));

        for (ServerConfig config : configs)
            servers.put(config.getName(), new AWSServerData(client, config));

        StatConfig statConfig = new StatConfig("https://www.gametracker.com/server_info/");

        parser = new GTParser(statConfig);
        timer.schedule(this, 0, TimeUnit.HOURS.toMillis(12));
    }

    @RequestMapping("/")
    public String getGreeting() {
        run();
        return data;
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - lastRun < TimeUnit.HOURS.toMillis(1))
            return;
        lastRun = System.currentTimeMillis();
        for (ServerConfig config : configs) {
            ServerData data = servers.get(config.getName());
            data.addData(parser.parseData(config));
            data.save();
        }
        Formatter format = new ForumsFormat();
        System.out.println(format.format(servers.values()));
        data = format.format(servers.values()).replace(System.lineSeparator(), "<br>");
    }
}

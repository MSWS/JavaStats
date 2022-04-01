package xyz.msws;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
@Controller
public class JavaStats extends TimerTask {
    private Map<String, ServerData> servers = new HashMap<>();
    private GTParser parser;
    private final Timer timer = new Timer();
    private String data = null;
    private long lastRun = 0;
    private AmazonS3 client;
    private StatConfig statConfig;

    public static void main(String[] args) {
        SpringApplication.run(JavaStats.class, args);
    }

    public JavaStats() {
        client = AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.US_WEST_1).build();

        statConfig = new StatConfig("https://www.gametracker.com/server_info/");

        statConfig.add("jb.csgo.edgegamers.cc:27015", "Jailbreak");
        statConfig.add("ttt.csgo.edgegamers.cc:27015", "Trouble in Terrorist Town");
        statConfig.add("surf.csgo.edgegamers.cc:27015", "Surf");
        statConfig.add("surf-static.csgo.edgegamers.cc:27015", "Surf Summer");
        statConfig.add("mg.csgo.edgegamers.cc:27015", "Minigames");
        statConfig.add("bhop.csgo.edgegamers.cc:27015", "Bhop");
        statConfig.add("104.128.58.205:27015", "AWP Only");
        statConfig.add("104.128.58.204:27015", "KZ Climb");
        statConfig.add("d2.css.edgegamers.cc:27015", "Dust2");

        for (ServerConfig config : statConfig.getServers())
            servers.put(config.getName(), new AWSServerData(client, config));

        parser = new GTParser(statConfig);
        timer.schedule(this, 0, TimeUnit.HOURS.toMillis(12));
    }

    @RequestMapping("/")
    @ResponseBody
    public String getGreeting() {
        run();
        return data;
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - lastRun < TimeUnit.HOURS.toMillis(1))
            return;
        lastRun = System.currentTimeMillis();

        for (ServerData data : servers.values()) {
            data.addData(parser.parseData(data.getConfig()));
            data.save();
        }

        Formatter format = new ForumsFormat();
        System.out.println(format.format(servers.values()));
        data = format.format(servers.values()).replace(System.lineSeparator(), "<br>");
    }
}

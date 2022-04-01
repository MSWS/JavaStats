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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class JavaStats extends TimerTask {
    private Map<String, ServerData> servers = new HashMap<>();
    private GTParser parser;
    private final Timer timer = new Timer();
    private String data = null;
    private long lastRun = 0;
    private AmazonS3 client;
    private StatConfig statConfig;
    private Map<Long, String> cache = new HashMap<>();

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

    @GetMapping("/")
    @ResponseBody
    public String getHome() {
        if (data != null && System.currentTimeMillis() - lastRun < TimeUnit.HOURS.toMillis(1))
            return data;
        lastRun = System.currentTimeMillis();
        data = run(System.currentTimeMillis());
        return data;
    }

    @GetMapping("/{time}")
    @ResponseBody
    public String getHomeAtTime(@PathVariable(name = "time") String timeStr) {
        long stamp = System.currentTimeMillis();
        try {
            stamp = Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            return getHome();
        }
        if (stamp < 0) {
            stamp *= TimeUnit.DAYS.toMillis(1);
            stamp = System.currentTimeMillis() + stamp;
        } else if (stamp < 999999999L)
            return getHome();
        if (stamp < 9999999999L)
            stamp *= 1000;

        return getCachedTimed(stamp);
    }

    String getCachedTimed(long time) {
        long round = TimeUnit.DAYS.toMillis(1);
        time = Math.round(Math.floor(time / (double) round)) * round;
        if (cache.containsKey(time))
            return cache.get(time);
        String result = run(time);
        cache.put(time, result);
        return result;
    }

    @Override
    public void run() {
        run(System.currentTimeMillis());
    }

    public String run(long time) {
        System.out.println("Fetching new data... of " + time);
        for (ServerData data : servers.values()) {
            data.addData(parser.parseData(data.getConfig()));
            data.save();
        }

        Formatter format = new ForumsFormat();
        System.out.println(format.format(servers.values(), time));
        return format.format(servers.values(), time).replace(System.lineSeparator(), "<br>");
    }
}

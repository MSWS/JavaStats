package xyz.msws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import xyz.msws.data.PostSnapshot;
import xyz.msws.formatter.Formatter;
import xyz.msws.formatter.ForumsFormat;
import xyz.msws.parser.ForumParser;
import xyz.msws.parser.GTParser;
import xyz.msws.parser.XenParser;
import xyz.msws.server.AWSServerData;
import xyz.msws.server.ServerConfig;
import xyz.msws.server.ServerData;
import xyz.msws.server.StatConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
public class JavaStats extends TimerTask {
    private final Map<String, ServerData> servers = new HashMap<>();
    private final GTParser serverParser;
    private final ForumParser forumParser;
    private final Map<Long, String> cache = new HashMap<>();
    private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private long lastRun = 0, lastFetch = 0;
    private String data = null, raw = null;
    public static int RANKING_ID = 221595, USER_ID = 102210;
    private final String token = System.getenv("XF_Api_Key");

    public static void main(String[] args) {
        SpringApplication.run(JavaStats.class, args);
    }

    public JavaStats() {
        try {
            RANKING_ID = Integer.parseInt(System.getenv("XF-RANKING-ID"));
            USER_ID = Integer.parseInt(System.getenv("XF-USER-ID"));
        } catch (NumberFormatException e) {
            System.out.println("Error occured when parsing post/user envars, assuming defaults.");
        }
        if (token == null) System.out.println("WARNING: Unable to fetch xenforo token.");
        dateFormat.setTimeZone(TimeZone.getTimeZone("PST"));
        AmazonS3 client = AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(Regions.US_WEST_1).build();

        StatConfig statConfig = new StatConfig("https://www.gametracker.com/server_info/");

        statConfig.add("jb.csgo.edgegamers.cc:27015", "Jailbreak");
        statConfig.add("ttt.csgo.edgegamers.cc:27015", "Trouble in Terrorist Town");
        statConfig.add("surf.csgo.edgegamers.cc:27015", "Surf");
//        statConfig.add("surf-static.csgo.edgegamers.cc:27015", "Surf Summer");
        statConfig.add("bhop.csgo.edgegamers.cc:27015", "Bhop");
        statConfig.add("104.128.58.205:27015", "AWP Only");
        statConfig.add("104.128.58.204:27015", "KZ Climb");
        statConfig.add("d2.css.edgegamers.cc:27015", "Dust2");
        statConfig.add("104.128.58.203:27015", "Surf Advanced");

        for (ServerConfig config : statConfig.getServers())
            servers.put(config.getName(), new AWSServerData(client, config));

        serverParser = new GTParser(statConfig);
        forumParser = new XenParser(token, RANKING_ID, "https://www.edgegamers.com/api");
        Timer timer = new Timer();
        timer.schedule(this, 0, TimeUnit.HOURS.toMillis(12));
    }

    @GetMapping("/")
    public String getHome() {
        if (data != null && System.currentTimeMillis() - lastRun < TimeUnit.HOURS.toMillis(1)) return data;
        lastRun = System.currentTimeMillis();
        data = run(System.currentTimeMillis());
        return data;
    }

    @GetMapping(value = {"/lastFetch", "/lf"})
    public String getLastFetch() {
        return String.format("%d | %d (%d)", System.currentTimeMillis(), lastFetch, System.currentTimeMillis() - lastFetch);
    }

    @GetMapping("/{time}")
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
        } else if (stamp < 999999999L) return getHome();
        if (stamp < 9999999999L) stamp *= 1000;

        return getCachedTimed(stamp);
    }

    @GetMapping("/{month}/{day}/{year}")
    public String getHomeAtTime(@PathVariable String month, @PathVariable String day, @PathVariable String year) {
        long stamp = System.currentTimeMillis();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(String.format("%s/%s/%s", month, day, year)));
            if (cal.get(Calendar.YEAR) < 2000) cal.add(Calendar.YEAR, 2000);
            stamp = cal.getTimeInMillis();
        } catch (ParseException e) {
            return getHome();
        }
        return getCachedTimed(stamp);
    }

    String getCachedTimed(long time) {
        try { // Round to nearest date
            time = dateFormat.parse(dateFormat.format(time)).getTime();
        } catch (ParseException e) {
            return getHome();
        }
        if (cache.containsKey(time)) return cache.get(time);
        String result = run(time);
        cache.put(time, result);
        return result;
    }

    @Override
    public void run() {
        data = run(System.currentTimeMillis());
    }

    public void fetch() {
        for (ServerData dat : servers.values())
            if (dat.addData(serverParser.parseData(dat.getConfig()))) dat.save();
        lastFetch = System.currentTimeMillis();
    }

    public String run(long time) {
        if (System.currentTimeMillis() - lastFetch > TimeUnit.HOURS.toMillis(1)) fetch();
        Formatter format = new ForumsFormat();
        String str = format.format(servers.values(), time);
        System.out.println(str);
        raw = str;
        str = str.replace(System.lineSeparator(), "<br>");
        String metaContent = """
                <title>EdgeGamers Stats</title>
                <meta property="og:title" content="EdgeGamers Stats" />
                <meta property="og:description" content="EGO Stats Auto-Generated by MSWS" />
                <meta property="og:url" content="http://egostats.msws.xyz/" />
                <meta property="og:image" content="https://msws.xyz/logo" />
                <meta property="og:type" content="article" />
                <meta data-react-helmet="true" name="theme-color" content="#43B581" />
                """;
        str = String.format("<head>" + metaContent + "</head><body><p>%s</p></body>", str);
        updateForums();
        return str;
    }

    private void updateForums() {
        PostSnapshot snapshot = forumParser.getData();
        if (snapshot == null || token == null) return;
        if (snapshot.getUserId() != USER_ID && snapshot.getDate() == 0) // Someone else posted and didn't follow format >:(
            return;
        if (System.currentTimeMillis() - snapshot.getEditDate() < TimeUnit.HOURS.toMillis(1))
            return;
        SimpleDateFormat sdf = ForumsFormat.sdf;
        try {
            URL upload;
            if (sdf.format(System.currentTimeMillis()).equals(snapshot.getDateString())) {
                upload = new URL("https://www.edgegamers.com/api/posts/" + snapshot.getId() + "/");
            } else {
                upload = new URL("https://www.edgegamers.com/api/posts/");
            }
            HttpURLConnection conn = (HttpURLConnection) upload.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("XF-Api-Key", token);
            conn.setRequestProperty("User-Agent", "Java");
            conn.setDoOutput(true);
            Map<String, String> args = new HashMap<>();
            args.put("thread_id", RANKING_ID + "");
            args.put("message", raw);
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : args.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(out.length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.connect();
            try (OutputStream stream = conn.getOutputStream()) {
                stream.write(out);
            }
            System.out.println(conn.getResponseCode() + ": " + conn.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

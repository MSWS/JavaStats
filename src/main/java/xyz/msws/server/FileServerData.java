package xyz.msws.server;

import java.io.*;
import java.util.Map;

public class FileServerData extends ServerData {
    private File file;

    public FileServerData(File file, ServerConfig config) {
        super(config);
        this.file = file;
        if (!file.exists())
            return;
        String data = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            data = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!data.isEmpty())
            for (String line : data.split(System.lineSeparator())) {
                long time = Long.parseLong(line.split(":")[0]);
                DataSnapshot snap = new DataSnapshot(line.substring(line.indexOf(":") + 1));
                snapshots.put(time, snap);
                if (snap.getDate() > System.currentTimeMillis())
                    System.out.println("WARNING: " + config.getName() + "'s snapshot in the future! (" + snap.getDate()
                            + ">" + System.currentTimeMillis() + ")");
            }
    }

    @Override
    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<Long, DataSnapshot> entry : snapshots.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue().toJSON().toString());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
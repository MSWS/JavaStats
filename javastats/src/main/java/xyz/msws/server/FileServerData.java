package xyz.msws.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class FileServerData extends ServerData {
    private File file;

    public FileServerData(File file, ServerConfig config) {
        super(config);
        this.file = file;
        if (!file.exists())
            return;
        String data = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String line : data.split("\n"))
            snapshots.put(Long.parseLong(line.split(":")[0]), new DataSnapshot(line.substring(line.indexOf(":") + 1)));
    }

    @Override
    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<Long, DataSnapshot> entry : snapshots.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue().toJSON().toString());
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
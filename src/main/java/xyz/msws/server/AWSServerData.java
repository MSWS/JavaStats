package xyz.msws.server;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import xyz.msws.data.DataSnapshot;

import java.io.*;
import java.util.Map;

/**
 * AWS Implementation of {@link ServerData}
 * Uses files for temporary storage
 */
public class AWSServerData extends ServerData {
    private File file;
    private AmazonS3 client;

    public AWSServerData(AmazonS3 client, ServerConfig config) {
        super(config);
        this.client = client;
        file = new File(config.getName() + ".txt");

        try (FileOutputStream out = new FileOutputStream(file)) {
            S3Object obj = client.getObject("egostats", file.getName());
            S3ObjectInputStream input = obj.getObjectContent();
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = input.read(read_buf)) > 0)
                out.write(read_buf, 0, read_len);
            input.close();
        } catch (IOException | AmazonS3Exception e) {
            e.printStackTrace();
        }

        if (!file.exists()) // S3 doesn't have the file
            return;

        String data = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append(System.lineSeparator());
            data = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data.isEmpty()) // Avoid empty files causing errors
            return;
        for (String line : data.split(System.lineSeparator())) {
            long time = Long.parseLong(line.split(":")[0]);
            DataSnapshot snap = new DataSnapshot(line.substring(line.indexOf(":") + 1));
            snapshots.put(time, snap);
            if (snap.getDate() <= System.currentTimeMillis())
                continue;
            System.out.printf("WARNING: %s's snapshot is in the future! (%L > %L)%s", config.getName(),
                    snap.getDate(), System.currentTimeMillis(), System.lineSeparator());
        }
    }

    @Override
    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<Long, DataSnapshot> entry : snapshots.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue().toJSON());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            client.putObject("egostats", file.getName(), file);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }
}
package xyz.msws.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

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
            System.out.println(sb.toString());
            data = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!data.isEmpty())
            for (String line : data.split(System.lineSeparator()))
                snapshots.put(Long.parseLong(line.split(":")[0]),
                        new DataSnapshot(line.substring(line.indexOf(":") + 1)));
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

        try {
            client.putObject("egostats", file.getName(), file);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
    }
}
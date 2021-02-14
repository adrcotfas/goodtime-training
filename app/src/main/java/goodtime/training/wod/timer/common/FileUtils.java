package goodtime.training.wod.timer.common;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class FileUtils {
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    public static void copy(InputStream inStream, File dst) throws IOException
    {
        FileOutputStream outStream = new FileOutputStream(dst);
        copy(inStream, outStream);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException
    {
        int numBytes;
        byte[] buffer = new byte[1024];

        while ((numBytes = in.read(buffer)) != -1) {
            out.write(buffer, 0, numBytes);
        }
        out.close();
    }

    public static boolean isSQLite3File(@NonNull File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);

        byte[] sqliteHeader = "SQLite format 3".getBytes();
        byte[] buffer = new byte[sqliteHeader.length];

        int count = fis.read(buffer);
        if(count < sqliteHeader.length) return false;

        return Arrays.equals(buffer, sqliteHeader);
    }
}

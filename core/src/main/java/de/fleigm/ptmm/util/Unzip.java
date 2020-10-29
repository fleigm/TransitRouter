package de.fleigm.ptmm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Unzip {

  public static void apply(String source, String destination) throws IOException {
    byte[] buffer = new byte[1024];
    File destDir = new File(destination);
    if (!destDir.exists()) {
      destDir.mkdir();
    }

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        File newFile = newFile(destDir, zipEntry);
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
    }
  }

  private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }
}

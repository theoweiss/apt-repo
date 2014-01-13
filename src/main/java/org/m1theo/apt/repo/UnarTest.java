package org.m1theo.apt.repo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.m1theo.apt.repo.utils.ControlHandler;

public class UnarTest {
  private static final String FAILED_TO_CREATE_APT_REPO = "Failed to create apt-repo: ";
  private static final String CONTROL_FILE_NAME = "./control";

  public static void main(String[] args) {
    File file = new File(args[0]);
    try {
      TarArchiveInputStream control_tgz;
      ArArchiveEntry entry;
      TarArchiveEntry control_entry;
      ArchiveInputStream debStream =
          new ArchiveStreamFactory().createArchiveInputStream("ar", new FileInputStream(file));
      while ((entry = (ArArchiveEntry) debStream.getNextEntry()) != null) {
        if (entry.getName().equals("control.tar.gz")) {
          final File outputFile = new File(file.getParentFile(), entry.getName());
          final OutputStream outputFileStream = new FileOutputStream(outputFile);
          IOUtils.copy(debStream, outputFileStream);
          outputFileStream.close();
          ControlHandler controlHandler = new ControlHandler();
          // GZIPInputStream gzipInputStream = new GZIPInputStream(debStream);
          GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(outputFile));
          control_tgz =
              (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar",
                  gzipInputStream);
          while ((control_entry = (TarArchiveEntry) control_tgz.getNextTarEntry()) != null) {
            System.out.println("control entry: " + control_entry.getName());
            if (control_entry.getName().equals(CONTROL_FILE_NAME)) {
              ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
              IOUtils.copy(control_tgz, outputStream);
              String content_string = outputStream.toString("UTF-8");
              outputStream.close();
              controlHandler.setControlContent(content_string);
              System.out.println("control cont: " + outputStream.toString("utf-8"));
              break;
            }
          }
          control_tgz.close();
          if (controlHandler.hasControlContent()) {
            System.out.println("control content found");
          } else {
            System.out.println("control content *not* found");
          }
          break;
        }
      }
      debStream.close();
    } catch (FileNotFoundException e) {
      String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
      System.out.println(msg + " " + e );
    } catch (ArchiveException e) {
      String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
      System.out.println(msg + " " + e );
    } catch (IOException e) {
      String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
      System.out.println(msg + " " + e );
    }
  }
  }



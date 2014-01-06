/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.m1theo.apt.repo.packages.PackageEntry;
import org.m1theo.apt.repo.packages.Packages;
import org.m1theo.apt.repo.release.Release;
import org.m1theo.apt.repo.release.ReleaseInfo;
import org.m1theo.apt.repo.utils.ControlHandler;
import org.m1theo.apt.repo.utils.DefaultHashes;
import org.m1theo.apt.repo.utils.Utils;

/**
 * Goal which creates an apt repository.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
@Mojo(name = "apt-repo", defaultPhase = LifecyclePhase.PACKAGE)
public class AptRepoMojo extends AbstractMojo {
  private static final String RELEASE = "Release";
  private static final String PACKAGES_GZ = "Packages.gz";
  private static final String FAILED_TO_CREATE_APT_REPO = "Failed to create apt-repo: ";
  private static final String CONTROL_FILE_NAME = "./control";
  private BufferedWriter packagesWriter;

  @Component
  private MavenProjectHelper projectHelper;

  @Component
  MavenProject project;

  /**
   * Location of the apt repository.
   */
  @Parameter(defaultValue = "${project.build.directory}/apt-repo", property = "apt-repo.repoDir", required = true)
  private File repoDir;

  /**
   * File type of the deb files.
   */
  @Parameter(defaultValue = "deb", property = "apt-repo.type", required = true)
  private String type;

  /**
   * Boolean option whether to aggregate the artifacts of all sub modules of the project
   */
  @Parameter(defaultValue = "true", property = "apt-repo.aggregate")
  private Boolean aggregate;

  /**
   * Boolean option whether to attach the artifact to the project
   */
  @Parameter(defaultValue = "true")
  private Boolean attach;

  /**
   * The classifier of attached artifacts.
   */
  @Parameter(defaultValue = "apt-repo", property = "apt-repo.classifier")
  private String classifier;

  public void execute() throws MojoExecutionException {
    getLog().info("repo dir: " + repoDir.getPath());
    if (!repoDir.exists()) {
      repoDir.mkdirs();
    }
    Collection<Artifact> artifacts = Utils.getAllArtifacts4Type(project, type, aggregate);
    for (Artifact artifact : artifacts) {
      getLog().debug("Artifact: " + artifact);
      getLog().debug("Artifact type: " + artifact.getType());
      try {
        FileUtils.copyFileToDirectory(artifact.getFile(), repoDir);
      } catch (IOException e) {
        getLog().error(FAILED_TO_CREATE_APT_REPO, e);
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO, e);
      }
    }
    File[] files = repoDir.listFiles(new FileFilter() {
      private String ext = "." + type;

      public boolean accept(File pathname) {
        if (pathname.getName().endsWith(ext)) {
          return true;
        }
        return false;
      }
    });
    Packages packages = new Packages();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      PackageEntry packageEntry = new PackageEntry();
      packageEntry.setSize(file.length());
      packageEntry.setSha1(Utils.getDigest("SHA-1", file));
      packageEntry.setSha256(Utils.getDigest("SHA-256", file));
      packageEntry.setMd5sum(Utils.getDigest("MD5", file));
      // String fileName = debFilesDir.getName() + File.separator + file.getName();
      String fileName = file.getName();
      packageEntry.setFilename(fileName);
      getLog().info("found deb: " + fileName);
      try {
        ArchiveInputStream control_tgz;
        ArArchiveEntry entry;
        TarArchiveEntry control_entry;
        ArchiveInputStream debStream =
            new ArchiveStreamFactory().createArchiveInputStream("ar", new FileInputStream(file));
        while ((entry = (ArArchiveEntry) debStream.getNextEntry()) != null) {
          if (entry.getName().equals("control.tar.gz")) {
            ControlHandler controlHandler = new ControlHandler();
            GZIPInputStream gzipInputStream = new GZIPInputStream(debStream);
            control_tgz =
                new ArchiveStreamFactory().createArchiveInputStream("tar", gzipInputStream);
            while ((control_entry = (TarArchiveEntry) control_tgz.getNextEntry()) != null) {
              getLog().debug("control entry: " + control_entry.getName());
              if (control_entry.getName().equals(CONTROL_FILE_NAME)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(control_tgz, outputStream);
                String content_string = outputStream.toString("UTF-8");
                outputStream.close();
                controlHandler.setControlContent(content_string);
                getLog().debug("control cont: " + outputStream.toString("utf-8"));
              }
            }
            control_tgz.close();
            controlHandler.handle(packageEntry);
            break;
          }
        }
        debStream.close();
        packages.addPackageEntry(packageEntry);
        if (attach) {
          getLog().info("Attaching file: " + file);
          projectHelper.attachArtifact(project, type, file.getName(), file);
          projectHelper.attachArtifact(project, file, fileName);
        }
      } catch (FileNotFoundException e) {
        getLog().error(FAILED_TO_CREATE_APT_REPO, e);
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO, e);
      } catch (ArchiveException e) {
        getLog().error(FAILED_TO_CREATE_APT_REPO, e);
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO, e);
      } catch (IOException e) {
        getLog().error(FAILED_TO_CREATE_APT_REPO, e);
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO, e);
      }
    }
    try {
      File packagesFile = new File(repoDir, PACKAGES_GZ);
      packagesWriter =
          new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(
              packagesFile))));
      packagesWriter.write(packages.toString());
      // FileUtils.fileWrite(packagesFile, packages.toString());
      DefaultHashes hashes = Utils.getDefaultDigests(packagesFile);
      ReleaseInfo pinfo = new ReleaseInfo(PACKAGES_GZ, packagesFile.length(), hashes);
      Release release = new Release();
      release.addInfo(pinfo);
      final File releaseFile = new File(repoDir, RELEASE);
      FileUtils.fileWrite(releaseFile, release.toString());
      if (attach) {
        getLog().info("Attaching created apt-repo files: " + releaseFile + ", " + packagesFile);
        projectHelper.attachArtifact(project, "gz", "Packages", packagesFile);
        projectHelper.attachArtifact(project, "Release-File", "Release", packagesFile);
      }
    } catch (IOException e) {
      throw new MojoExecutionException("writing files failed", e);
    } finally {
      if (packagesWriter != null) {
        try {
          packagesWriter.close();
        } catch (IOException e) {
          throw new MojoExecutionException("writing files failed", e);
        }
      }
    }
  }
}

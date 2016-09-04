/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo;

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
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.codehaus.plexus.util.FileUtils;
import org.m1theo.apt.repo.packages.PackageEntry;
import org.m1theo.apt.repo.packages.Packages;
import org.m1theo.apt.repo.release.Release;
import org.m1theo.apt.repo.release.ReleaseInfo;
import org.m1theo.apt.repo.signing.PGPSigner;
import org.m1theo.apt.repo.utils.ControlHandler;
import org.m1theo.apt.repo.utils.DefaultHashes;
import org.m1theo.apt.repo.utils.Utils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
  private static final String RELEASEGPG = "Release.gpg";
  private static final String INRELEASE = "InRelease";
  private static final String PACKAGES = "Packages";
  private static final String PACKAGES_GZ = "Packages.gz";
  private static final String FAILED_TO_CREATE_APT_REPO = "Failed to create apt-repo: ";
  private static final String CONTROL_FILE_NAME = "./control";

  @Component
  private MavenProjectHelper projectHelper;

  @Component
  MavenProject project;

  /**
   * If sign is true then a gpg signature will be added to the repo. keyring, key and passphrase
   * will also be required.
   */
  @Parameter(defaultValue = "false", property = "apt-repo.sign")
  private boolean sign;

  /**
   * The keyring to use for signing operations.
   */
  @Parameter(readonly = true, property = "apt-repo.keyring")
  private File keyring;

  /**
   * The key to use for signing operations.
   */
  @Parameter(property = "apt-repo.key")
  private String key;

  /**
   * The passphrase to use for signing operations.
   */
  @Parameter(property = "apt-repo.passphrase")
  private String passphrase;

  /**
   * A file containg the passphrase to use for signing operations.
   * The passphrase must be in the first line of the file.
   */
  @Parameter(readonly = true, property = "apt-repo.passphrase-file")
  private File passphraseFile;

  /**
   * The digest algorithm to use.
   *
   * @see org.bouncycastle.bcpg.HashAlgorithmTags
   */
  @Parameter(defaultValue = "SHA256", property = "apt-repo.digest")
  private String digest;

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

  /**
   * Contains the full list of projects in the reactor.
   */
  @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
  private List<MavenProject> reactorProjects;

  public List<MavenProject> getReactorProjects() {
    return reactorProjects;
  }

  public void execute() throws MojoExecutionException {
    if (sign){
      if (keyring == null || !keyring.exists()){
        getLog().error("Signing requested, but no or invalid keyrring supplied");
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO + "keyring invalid or missing");
      }
      if (key == null){
        getLog().error("Signing requested, but no key supplied");
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO + "key is missing");
      }
      if (passphrase == null && passphraseFile == null){
        getLog().error("Signing requested, but no passphrase or passphrase file supplied");
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO + "passphrase or passphrase file must be specified");
      }
      if (passphraseFile != null && ! passphraseFile.exists()){
        getLog().error("Signing requested, passphrase file does not exist: " + passphraseFile.getAbsolutePath());
        throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO + "passphrase file does not exist " + passphraseFile.getAbsolutePath());
      }
    }
    getLog().info("repo dir: " + repoDir.getPath());
    if (!repoDir.exists()) {
      repoDir.mkdirs();
    }
    try {
      Collection<Artifact> artifacts = Utils.getAllArtifacts4Type(project, type, aggregate);
      // Collection<Artifact> artifacts =
      // Utils.getDebArtifacts(project, reactorProjects, type, aggregate, getLog());
      for (Artifact artifact : artifacts) {
        getLog().debug("Artifact: " + artifact);
        getLog().debug("Artifact type: " + artifact.getType());
        FileUtils.copyFileToDirectory(artifact.getFile(), repoDir);
      }
    } catch (IOException e) {
      getLog().error(FAILED_TO_CREATE_APT_REPO, e);
      throw new MojoExecutionException(FAILED_TO_CREATE_APT_REPO, e);
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
      packageEntry.setSha512(Utils.getDigest("SHA-512", file));
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
                break;
              }
            }
            control_tgz.close();
            if (controlHandler.hasControlContent()) {
              controlHandler.handle(packageEntry);
            } else {
              throw new MojoExecutionException("no control content found for: " + file.getName());
            }
            break;
          }
        }
        debStream.close();
        packages.addPackageEntry(packageEntry);
        if (attach) {
          getLog().info("Attaching file: " + file);
          projectHelper.attachArtifact(project, type, file.getName(), file);
          // projectHelper.attachArtifact(project, file, fileName);
        }
      } catch (MojoExecutionException e) {
        String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
        getLog().error(msg, e);
        throw new MojoExecutionException(msg, e);
      } catch (FileNotFoundException e) {
        String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
        getLog().error(msg, e);
        throw new MojoExecutionException(msg, e);
      } catch (ArchiveException e) {
        String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
        getLog().error(msg, e);
        throw new MojoExecutionException(msg, e);
      } catch (IOException e) {
        String msg = FAILED_TO_CREATE_APT_REPO + " " + file.getName();
        getLog().error(msg, e);
        throw new MojoExecutionException(msg, e);
      }
    }
    try {
      Release release = new Release();

      File packagesFile = new File(repoDir, PACKAGES);
      BufferedWriter packagesWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          packagesFile)));
      packagesWriter.write(packages.toString());
      packagesWriter.close();
      DefaultHashes hashes = Utils.getDefaultDigests(packagesFile);
      ReleaseInfo pinfo = new ReleaseInfo(PACKAGES, packagesFile.length(), hashes);
      release.addInfo(pinfo);

      File packagesGzFile = new File(repoDir, PACKAGES_GZ);
      BufferedWriter packagesGzWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(
          packagesGzFile))));
      packagesGzWriter.write(packages.toString());
      packagesGzWriter.close();
      DefaultHashes gzHashes = Utils.getDefaultDigests(packagesGzFile);
      ReleaseInfo gzPinfo = new ReleaseInfo(PACKAGES_GZ, packagesGzFile.length(), gzHashes);
      release.addInfo(gzPinfo);

      final File releaseFile = new File(repoDir, RELEASE);
      FileUtils.fileWrite(releaseFile, release.toString());
      if (sign){
        if (passphraseFile != null){
          getLog().debug("passphrase file will be used " + passphraseFile.getAbsolutePath());
          BufferedReader pwReader = new BufferedReader(new FileReader(passphraseFile));
          passphrase = pwReader.readLine();
          pwReader.close();
        }
        final File inReleaseFile = new File(repoDir, INRELEASE);
        final File releaseGpgFile = new File(repoDir, RELEASEGPG);
        PGPSigner signer = new PGPSigner(new FileInputStream(keyring), key, passphrase, getDigestCode(digest));
        signer.clearSignDetached(release.toString(), new FileOutputStream(releaseGpgFile));
        signer.clearSign(release.toString(), new FileOutputStream(inReleaseFile));
      }
      // if (attach) {
      // getLog().info("Attaching created apt-repo files: " + releaseFile + ", " + packagesFile);
      // projectHelper.attachArtifact(project, "gz", "Packages", packagesFile);
      // projectHelper.attachArtifact(project, "Release-File", "Release", packagesFile);
      // }
    } catch (IOException e) {
      throw new MojoExecutionException("writing files failed", e);
    } catch (PGPException e) {
      throw new MojoExecutionException("gpg signing failed",e);
    } catch (GeneralSecurityException e) {
      throw new MojoExecutionException("generating release failed",e);
    }
  }
  static int getDigestCode(String digestName) throws MojoExecutionException {
    if ("SHA1".equals(digestName)) {
      return HashAlgorithmTags.SHA1;
    } else if ("MD2".equals(digestName)) {
      return HashAlgorithmTags.MD2;
    } else if ("MD5".equals(digestName)) {
      return HashAlgorithmTags.MD5;
    } else if ("RIPEMD160".equals(digestName)) {
      return HashAlgorithmTags.RIPEMD160;
    } else if ("SHA256".equals(digestName)) {
      return HashAlgorithmTags.SHA256;
    } else if ("SHA384".equals(digestName)) {
      return HashAlgorithmTags.SHA384;
    } else if ("SHA512".equals(digestName)) {
      return HashAlgorithmTags.SHA512;
    } else if ("SHA224".equals(digestName)) {
      return HashAlgorithmTags.SHA224;
    } else {
      throw new MojoExecutionException("unknown hash algorithm tag in digestName: " + digestName);
    }
  }

}

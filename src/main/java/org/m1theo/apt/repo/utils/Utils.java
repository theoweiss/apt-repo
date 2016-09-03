/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Providing utilities as static methods.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public class Utils {

  /**
   * Compute the given message digest for a file.
   * 
   * @param hashType algorithm to be used (as {@code String})
   * @param file File to compute the digest for (as {@code File}).
   * @return A {@code String} for the hex encoded digest.
   * @throws MojoExecutionException
   */
  public static String getDigest(String hashType, File file) throws MojoExecutionException {
    try {
      FileInputStream fis = new FileInputStream(file);
      BufferedInputStream bis = new BufferedInputStream(fis);
      MessageDigest digest = MessageDigest.getInstance(hashType);
      DigestInputStream dis = new DigestInputStream(bis, digest);
      @SuppressWarnings("unused")
      int ch;
      while ((ch = dis.read()) != -1);
      String hex = new String(Hex.encodeHex(digest.digest()));
      fis.close();
      bis.close();
      dis.close();
      return hex;
    } catch (NoSuchAlgorithmException e) {
      throw new MojoExecutionException("could not create digest", e);
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("could not create digest", e);
    } catch (IOException e) {
      throw new MojoExecutionException("could not create digest", e);
    }
  }

  /**
   * Compute md5, sha1, sha256 message digest for a file.
   * 
   * @param file File to compute the digest for (as {@code File}).
   * @return {@link DefaultHashes} with the computed digests.
   * @throws MojoExecutionException
   */
  public static DefaultHashes getDefaultDigests(File file) throws MojoExecutionException {
    DefaultHashes h = new DefaultHashes();
      for (Hashes hash : Hashes.values()) {
        String hex = getDigest(hash.toString(), file);
        switch (Hashes.values()[hash.ordinal()]) {
          case MD5:
            h.setMd5(hex);
            break;
          case SHA1:
            h.setSha1(hex);
            break;
          case SHA256:
            h.setSha256(hex);
            break;
          case SHA512:
            h.setSha512(hex);
            break;
          default:
            throw new MojoExecutionException("unknown hash type: " + hash.toString());
        }
      }
      return h;
  }

  /**
   * Collects all artifacts of the given type.
   * 
   * @param project The maven project which should be used.
   * @param type The file type which should be collected.
   * @return A collection of all artifacts with the given type.
   */
  @SuppressWarnings("unchecked")
  public static Collection<Artifact> getAllArtifacts4Type(MavenProject project, String type,
      Boolean aggregate) {
    final Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
    List<MavenProject> modules = new ArrayList<MavenProject>();
    modules.add(project);
    List<MavenProject> collectedProjects = project.getCollectedProjects();
    if (collectedProjects != null) {
      modules.addAll(collectedProjects);
    }
    for (MavenProject module : modules) {
      addDebArtifact(module.getArtifact(), artifacts, type);
      for (Object artifact : module.getArtifacts()) {
        if (artifact instanceof Artifact) {
          addDebArtifact((Artifact) artifact, artifacts, type);
        }
      }
      for (Object artifact : module.getAttachedArtifacts()) {
        if (artifact instanceof Artifact) {
          addDebArtifact((Artifact) artifact, artifacts, type);
        }
      }
    }
    if (project.hasParent() && aggregate) {
      artifacts.addAll(getAllArtifacts4Type(project.getParent(), type, aggregate));
    }
    return artifacts;
  }

  private static void addDebArtifact(Artifact artifact, Set<Artifact> artifacts, String type) {
    if (artifact.getType().equals(type)) {
      artifacts.add(artifact);
    }
  }

  // public static Set<Artifact> getDebArtifacts(final MavenProject project,
  // final List<MavenProject> reactorProjects, final String type, final Boolean aggregate,
  // final Log logger) throws IOException {
  // final Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
  // Set<MavenProject> modules = getProjectModules(project, reactorProjects, aggregate, logger);
  // for (MavenProject module : modules) {
  // addDebArtifact(module.getArtifact(), artifacts, type);
  // for (Object artifact : module.getArtifacts()) {
  // if (artifact instanceof Artifact) {
  // addDebArtifact((Artifact) artifact, artifacts, type);
  // }
  // }
  // for (Object artifact : module.getAttachedArtifacts()) {
  // if (artifact instanceof Artifact) {
  // addDebArtifact((Artifact) artifact, artifacts, type);
  // }
  // }
  // }
  //
  // return artifacts;
  // }
  //
  // /**
  // * Copied from org.apache.maven.plugin.assembly.utils.ProjectUtils
  // *
  // * @param project
  // * @param reactorProjects
  // * @param includeSubModules
  // * @param logger
  // * @return
  // * @throws IOException
  // */
  // private static Set<MavenProject> getProjectModules(final MavenProject project,
  // final List<MavenProject> reactorProjects, final boolean includeSubModules, final Log logger)
  // throws IOException {
  // final Set<MavenProject> singleParentSet = Collections.singleton(project);
  //
  // final Set<MavenProject> moduleCandidates = new LinkedHashSet<MavenProject>(reactorProjects);
  //
  // final Set<MavenProject> modules = new LinkedHashSet<MavenProject>();
  //
  // // we temporarily add the master project to the modules set, since this
  // // set is pulling double duty as a set of
  // // potential module parents in the tree rooted at the master
  // // project...this allows us to use the same looping
  // // algorithm below to discover both direct modules of the master project
  // // AND modules of those direct modules.
  // modules.add(project);
  //
  // int changed = 0;
  //
  // do {
  // changed = 0;
  //
  // for (final Iterator<MavenProject> candidateIterator = moduleCandidates.iterator();
  // candidateIterator
  // .hasNext();) {
  // final MavenProject moduleCandidate = candidateIterator.next();
  //
  // if (moduleCandidate.getFile() == null) {
  // logger.warn("Cannot compute whether " + moduleCandidate.getId() + " is a module of: "
  // + project.getId()
  // + "; it does not have an associated POM file on the local filesystem.");
  // continue;
  // }
  //
  // Set<MavenProject> currentPotentialParents;
  // if (includeSubModules) {
  // currentPotentialParents = new LinkedHashSet<MavenProject>(modules);
  // } else {
  // currentPotentialParents = singleParentSet;
  // }
  //
  // for (final Iterator<MavenProject> parentIterator = currentPotentialParents.iterator();
  // parentIterator
  // .hasNext();) {
  // final MavenProject potentialParent = parentIterator.next();
  //
  // if (potentialParent.getFile() == null) {
  // logger.warn("Cannot use: " + moduleCandidate.getId()
  // + " as a potential module-parent while computing the module set for: "
  // + project.getId()
  // + "; it does not have an associated POM file on the local filesystem.");
  // continue;
  // }
  //
  // // if this parent has an entry for the module candidate in
  // // the path adjustments map, it's a direct
  // // module of that parent.
  // if (projectContainsModule(potentialParent, moduleCandidate)) {
  // // add the candidate to the list of modules (and
  // // potential parents)
  // modules.add(moduleCandidate);
  //
  // // remove the candidate from the candidate pool, because
  // // it's been verified.
  // candidateIterator.remove();
  //
  // // increment the change counter, to show that we
  // // verified a new module on this pass.
  // changed++;
  // }
  // }
  // }
  // } while (changed != 0);
  //
  // // remove the master project from the modules set, now that we're done
  // // using it as a set of potential module
  // // parents...
  // // modules.remove(project);
  //
  // return modules;
  // }
  //
  // private static boolean projectContainsModule(final MavenProject mainProject,
  // final MavenProject moduleProject) throws IOException {
  // @SuppressWarnings("unchecked")
  // final List<String> modules = mainProject.getModules();
  // final File basedir = mainProject.getBasedir();
  //
  // final File moduleFile = moduleProject.getFile().getCanonicalFile();
  //
  // File moduleBasedir = moduleProject.getBasedir();
  //
  // if (moduleBasedir == null) {
  // if (moduleFile != null) {
  // moduleBasedir = moduleFile.getParentFile();
  // }
  //
  // if (moduleBasedir == null) {
  // moduleBasedir = new File(".");
  // }
  // }
  //
  // moduleBasedir = moduleBasedir.getCanonicalFile();
  //
  // for (final Iterator<String> it = modules.iterator(); it.hasNext();) {
  // final String moduleSubpath = it.next();
  //
  // final File moduleDir = new File(basedir, moduleSubpath).getCanonicalFile();
  //
  // if (moduleDir.equals(moduleFile) || moduleDir.equals(moduleBasedir)) {
  // return true;
  // }
  // }
  //
  // return false;
  // }

}

/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.release;

import org.apache.maven.plugin.MojoExecutionException;
import org.m1theo.apt.repo.utils.DefaultHashes;
import org.m1theo.apt.repo.utils.Hashes;

/**
 * Models an Release file entry.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public class ReleaseInfo {
  private String md5hash;
  private String sha1hash;
  private String sha256hash;
  private String sha512hash;
  private Long size;
  private String name;

  public ReleaseInfo(String name, long size, DefaultHashes hashes) throws MojoExecutionException {
    this.name = name;
    this.size = size;
    for (Hashes hash : Hashes.values()) {
      switch (Hashes.values()[hash.ordinal()]) {
        case MD5:
          this.setMd5hash(hashes.getMd5());
          break;
        case SHA1:
          this.setSha1hash(hashes.getSha1());
          break;
        case SHA256:
          this.setSha256hash(hashes.getSha256());
          break;
        case SHA512:
          this.setSha512hash(hashes.getSha512());
          break;
        default:
          throw new MojoExecutionException("unknown hash type: " + hash.toString());
      }
    }
  }

  public void setMd5hash(String md5hash) {
    this.md5hash = md5hash;
  }

  public void setSha1hash(String sha1hash) {
    this.sha1hash = sha1hash;
  }

  public void setSha256hash(String sha256hash) {
    this.sha256hash = sha256hash;
  }

  public void setSha512hash(String sha512hash) {
    this.sha512hash = sha512hash;
  }

  public String getMd5hash() {
    return md5hash;
  }

  public String getSha1hash() {
    return sha1hash;
  }

  public String getSha256hash() {
    return sha256hash;
  }

  public String getSha512hash() {
    return sha512hash;
  }

  public Long getSize() {
    return size;
  }

  public String getName() {
    return name;
  }
}

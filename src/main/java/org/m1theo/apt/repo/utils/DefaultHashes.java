/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.utils;

/**
 * A container for commonly used hashes.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public class DefaultHashes {
  private String md5;
  private String sha1;
  private String sha256;
  private String sha512;

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getSha1() {
    return sha1;
  }

  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
  }

  public String getSha512() {
    return sha512;
  }

  public void setSha512(String sha512) {
    this.sha512 = sha512;
  }

}

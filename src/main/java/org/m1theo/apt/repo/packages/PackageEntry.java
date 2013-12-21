/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.packages;

public class PackageEntry {
  private String packageName;
  private String version;
  private String architecture;
  private String maintainer;
  private String installed_size;
  private String depends;
  private String filename;
  private long size;
  private String md5sum;
  private String sha1;
  private String sha256;
  private String section;
  private String priority;
  private String description;


  @Override
  public String toString() {
    return "Package: " + packageName + "\n" + "Version: " + version + "\n" + "Architecture: "
        + architecture + "\n" + "Maintainer: " + maintainer + "\n" + "Installed-Size: "
        + installed_size + "\n" + "Depends: " + depends + "\n" + "Filename: " + filename + "\n"
        + "Size: " + size + "\n" + "MD5sum: " + md5sum + "\n" + "SHA1: " + sha1 + "\n" + "SHA256: "
        + sha256 + "\n" + "Section: " + section + "\n" + "Priority: " + priority + "\n"
        + "Description: " + description + "\n";
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  public void setMaintainer(String maintainer) {
    this.maintainer = maintainer;
  }

  public void setInstalled_size(String installed_size) {
    this.installed_size = installed_size;
  }

  public void setDepends(String depends) {
    this.depends = depends;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void setMd5sum(String md5sum) {
    this.md5sum = md5sum;
  }

  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public void setDescription(String description) {
    this.description = description;
  }


}

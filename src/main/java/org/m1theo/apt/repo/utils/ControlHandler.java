/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.m1theo.apt.repo.packages.PackageEntry;

/**
 * Parses the control file.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public class ControlHandler {
  private String controlContent;

  public void setControlContent(String controlContent) {
    this.controlContent = controlContent.trim();
  }

  private void parseControl(PackageEntry packageEntry) throws MojoExecutionException {
    if (controlContent == null) {
      throw new MojoExecutionException("no controlContent to parse");
    }
    String[] lines = controlContent.split("\\r?\\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      String[] stmt = line.split(":", 2);
      if (stmt.length != 2) {
        continue;
      }
      String key = stmt[0].trim();
      String value = stmt[1].trim();
      if (key.equals("Package")) {
        packageEntry.setPackageName(value);
      } else if (key.equals("Version")) {
        packageEntry.setVersion(value);
      } else if (key.equals("Architecture")) {
        packageEntry.setArchitecture(value);
      } else if (key.equals("Maintainer")) {
        packageEntry.setMaintainer(value);
      } else if (key.equals("Installed-Size")) {
        packageEntry.setInstalled_size(value);
      } else if (key.equals("Depends")) {
        packageEntry.setDepends(value);
      } else if (key.equals("Section")) {
        packageEntry.setSection(value);
      } else if (key.equals("Priority")) {
        packageEntry.setPriority(value);
      } else if (key.equals("Description")) {
        packageEntry.setDescription(value);
      }
    }
  }

  /**
   * Parse the control file contents and update the {@link PackageEntry}.
   * 
   * @param packageEntry
   * @throws MojoExecutionException
   */
  public void handle(PackageEntry packageEntry) throws MojoExecutionException {
    parseControl(packageEntry);
  }

}

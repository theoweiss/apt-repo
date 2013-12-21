/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.packages;

import java.util.ArrayList;

/**
 * A model the Packages file.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public class Packages {
  private ArrayList<PackageEntry> packages = new ArrayList<PackageEntry>();

  public void addPackageEntry(PackageEntry packageEntry) {
    packages.add(packageEntry);
  }

  @Override
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer();
    for (PackageEntry p : packages) {
      stringBuffer.append(p.toString());
      stringBuffer.append("\n");
    }
    return stringBuffer.toString();
  }

}

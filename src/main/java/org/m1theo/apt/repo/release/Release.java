/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.release;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A model for the Release file.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public class Release {
  String date;
  List<ReleaseInfo> infos = new ArrayList<ReleaseInfo>();

  public Release() {
    SimpleDateFormat dateFormat =
        (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
    dateFormat.applyPattern("EEE, d MMM yyyy HH:mm:ss z");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.date = dateFormat.format(new Date());
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void addInfo(ReleaseInfo info) {
    infos.add(info);
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append("Date: " + date + "\n");
    b.append("MD5Sum:\n");
    for (ReleaseInfo info : infos) {
      b.append(String.format(" %s  %s %s\n", info.getMd5hash(), info.getSize(), info.getName()));
    }
    b.append("SHA1:\n");
    for (ReleaseInfo info : infos) {
      b.append(String.format(" %s  %s %s\n", info.getSha1hash(), info.getSize(), info.getName()));
    }
    b.append("SHA256:\n");
    for (ReleaseInfo info : infos) {
      b.append(String.format(" %s  %s %s\n", info.getSha256hash(), info.getSize(), info.getName()));
    }
    b.append("SHA512:\n");
    for (ReleaseInfo info : infos) {
      b.append(String.format(" %s  %s %s\n", info.getSha512hash(), info.getSize(), info.getName()));
    }
    return b.toString();
  }

}

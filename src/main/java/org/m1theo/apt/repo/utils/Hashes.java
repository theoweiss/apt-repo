/**
 * Copyright (c) 2010-2013, theo@m1theo.org.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.m1theo.apt.repo.utils;

/**
 * Commonly used hashes.
 * 
 * @author Theo Weiss
 * @since 0.1.0
 * 
 */
public enum Hashes {
  MD5("MD5"), SHA1("SHA-1"), SHA256("SHA-256"), SHA512("SHA-512");

  private String hString;

  private Hashes(String hString) {
    this.hString = hString;
  }

  @Override
  public String toString() {
    return hString;
  }

}

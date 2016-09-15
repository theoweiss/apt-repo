## Apt repository build with Java

A maven plugin to create an apt repository for your java artifacts.

This plugin turns your maven build for deb files into an apt-repository. The plugin only needs java and
does not require any native tools to be installed.

It is meant to be used in conjunction with plugins like [jdeb](https://github.com/tcurdt/jdeb),
which create deb files for your artifacts.

### New and Noteworthy

As of version 0.3.0 adding a pgp signature to the repo is supported! 

### Explanation

apt-repo generates an apt repository in "flat repository format".
apt-repo will use all artifacts of type "deb" and will aggregate them to the "apt-repo" directory in
your ${project.build.directory}.
An apt "Release" and "Packages.gz" file will be placed into this directory.
Therefore the apt command will recognize your deb files - build by maven - as a valid repository.
If signing is enabled also a InRelease file and a Release.gpg file will be created.

### Where to get it
The jars are available in the [Maven central repository](http://central.maven.org/maven2/org/m1theo/apt-repo/).

### Configuration

  Available basic parameters:

    aggregate (Default: true)
      Boolean option whether to aggregate the artifacts of all sub modules of
      the project
      User property: apt-repo.aggregate

    attach (Default: true)
      Boolean option whether to attach the artifact to the project

    classifier (Default: apt-repo)
      The classifier of attached artifacts.
      User property: apt-repo.classifier

    repoDir (Default: ${project.build.directory}/apt-repo)
      Location of the apt repository.
      Required: Yes
      User property: apt-repo.repoDir

    type (Default: deb)
      File type of the deb files.
      Required: Yes
      User property: apt-repo.type

Add the plugin to your pom.xml link this:
```
 <build>
    <plugins>
      <plugin>
        <artifactId>apt-repo</artifactId>
        <groupId>org.m1theo</groupId>
        <version>0.2.1</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>apt-repo</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
</build>
```

#### Signing
  Available signing parameters:

    sign (Default: false)
      If sign is true then a gpg signature will be added to the repo. keyring,
      key and passphrase will also be required.
      User property: apt-repo.sign

    key
      The key to use for signing operations.
      User property: apt-repo.key

    passphrase
      The passphrase to use for signing operations.
      User property: apt-repo.passphrase

    passphraseFile
      A file containg the passphrase to use for signing operations.
      The passphrase must be in the first line of the file.
      User property: apt-repo.passphrase-file

    digest (Default: SHA256)
      The digest algorithm to use.
      User property: apt-repo.digest


If you use apt-repo in conjunction with jdeb make sure that the jdeb plugin is executed before apt-repo.
Normally this could be achieved by registering it prior to the apt-repo plugin in the plugins order.

Add something like this to your apt sources.list:
```
deb http://192.168.1.100:8000/${my.project.dir}/target/apt-repo/ /
```
Now check with "apt-get update" if apt recognizes your new apt repository.

apt-repo supports some configuration options

Element       | Description                                                                  | Required
------------- | ---------------------------------------------------------------------------- | -----------------------------------------------------------------
type          | The artifact type of the deb files                                           | No; defaults to `deb`
attach        | Attach artifacts to project                                                  | No; defaults to `true`
aggregate     | Execute the goal on all submodules                                           | No; defaults to `true`
repoDir       | Directory where the repo should be created                                   | No; defaults to `${buildDirectory}/apt-repo`

### Related projects
[jdeb](https://github.com/tcurdt/jdeb) Debian packages in Java

apt-repo
========

A maven plugin to create an apt repository for your java artifacts.

This plugin turns your maven build for deb files into an apt-repository.

It is meant to be used in conjunction with plugins like "jdeb", which generate deb files for 
your artifacts.
apt-repo generates an apt repository in "flat repository format".
apt-repo will find all artifacts of type "deb" and will aggregate them to the "apt-repo" directory in your ${project.build.directory}.
An apt "Release" and "Packages.gz" file will be placed into this directory.
Therefore the apt command will recognize your deb files - build by maven - as a valid repository.

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
If you use apt-repo in conjunction with jdeb make sure that the jdeb plugin is executed before apt-repo. 
Normally this could be achieved by registering it prior to the apt-repo plugin in the plugins order.

Add something like this to your apt sources.list:
```
deb http://192.168.1.100:8000/${my.project.dir}/target/apt-repo /
```
Now check with "apt-get update" if apt recognizes your new apt repository.

apt-repo supports some configuration options

Element       | Description                                                                  | Required
------------- | ---------------------------------------------------------------------------- | -----------------------------------------------------------------
type          | The artifact type of the deb files                                           | No; defaults to `deb`
attach        | Attach artifacts to project                                                  | No; defaults to `true`
aggregate     | Execute the goal on all submodules                                           | No; defaults to `true`
repoDir       | Directory where the repo should be created                                   | No; defaults to `${buildDirectory}/apt-repo`


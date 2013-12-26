apt-repo
========

A maven plugin to create an apt repository for your java artifacts.

This plugin turns your maven build for deb files into an apt-repository.

It is meant to be used in conjunction with plugins like "jdeb", which generate deb files for 
your artifacts.
apt-repo generates an apt repository in "flat repository format".
An apt "Release" and "Packages.gz" file will be placed into the directory containing the deb files.
Therefore the apt command will recognize your deb files - build by maven - as a valid repository.

Add the plugin to your pom.xml link this:
```
 <build>
    <plugins>
      <plugin>
        <artifactId>apt-repo</artifactId>
        <groupId>org.m1theo</groupId>
        <version>0.0.1</version>
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
If you use jdeb make sure that the jdeb plugin is executed before apt-repo. 
Normally this could be achieved by registering it prior to the apt-repo plugin in the plugins order.

The apt-repo plugin will find all deb files in your ${project.build.directory} and will 
generate a file named "Release" and a file named 
"Packages.gz" in the ${project.build.directory}.

Add something like this to your apt sources.list:
```
deb http://192.168.1.100:8000/${my.project.dir}/target/ /
```
Now check with "apt-get update" if apt recognizes your new apt repository.

apt-repo supports the configuration option "debDir" to override the default deb files location:
```
...
 <configuration>
  <debDir>${project.build.directory}/deb</debDir>
 </configuration>
...
```


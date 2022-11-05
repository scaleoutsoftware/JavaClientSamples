# JavaClientSamples
 
This repository houses samples for the ScaleOut Java client API. 

- SampleApplication
	- Docker-compose sample that runs four containers 
	- One application that stores/retrieves objects from two ScaleOut grids
	- One application that registers for expiration events
	- Two SOSS grids called store1 and store2
- CachingSamples
	- Standalone samples demonstrating the API 


Gradle

For Gradle, you can add the ScaleOut API Repository to your build.gradle by adding the following under repositories:
```
repositories {
    mavenCentral()
    maven {
        url "https://repo.scaleoutsoftware.com/repository/external"
    }
}
```

Then add the following under dependencies: 
```
// Use the following for the Java ScaleOut Client API
implementation group: 'com.scaleoutsoftware.client', name: 'api', version: '1.0.1'
```

Maven: 

For Maven, you can add the ScaleOut API Repository to your pom.xml by adding the following repository reference:
```
<repository>
    <id>ScaleOut API Repository</id>
    <url>https://repo.scaleoutsoftware.com/repository/external</url>
</repository>
```

And then you can add the following dependency:
```
<dependencies>
    <!-- Use the following for the Java ScaleOut Client API -->
    <dependency>
      <groupId>com.scaleoutsoftware.client</groupId>
      <artifactId>api</artifactId>
      <version>1.0.1</version>
    </dependency>
</dependencies>
```


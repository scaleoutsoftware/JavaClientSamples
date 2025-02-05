# JavaClientSamples
 
This repository houses samples for the ScaleOut Java client API. 

- SampleApplication
	- Docker-compose sample that runs four containers 
	- One application that stores/retrieves objects from two ScaleOut grids
	- One application that registers for expiration events
	- Two SOSS grids called store1 and store2
- CachingSamples
	- Standalone samples demonstrating the API 

The Java ScaleOutClient and InvocationGrid libraries are available on Maven central. 

Gradle

```
// Use the following for the Java ScaleOut Client API
implementation group: 'com.scaleoutsoftware', name: 'client', version: '2.0.0'

// Use the following for the Java ScaleOut InvocationGrid API
implementation group: 'com.scaleoutsoftware', name: 'invocationgrid', version: '2.0.0'
```

Maven: 

And then you can add the following dependency:
```
<dependencies>
    <!-- Use the following for the Java ScaleOut Client API -->
    <dependency>
      <groupId>com.scaleoutsoftware</groupId>
      <artifactId>client</artifactId>
      <version>2.0.0</version>
    </dependency>
</dependencies>

<dependencies>
    <!-- Use the following for the Java ScaleOut InvocationGrid API -->
    <dependency>
      <groupId>com.scaleoutsoftware</groupId>
      <artifactId>invocationgrid</artifactId>
      <version>2.0.0</version>
    </dependency>
</dependencies>
```


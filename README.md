# Sy Notes
Sy's notes for Linkedin restli.

## Getting started
https://linkedin.github.io/rest.li/get_started/quick_start


## Requirements
```
>>> java -version
openjdk version "1.8.0_322"
OpenJDK Runtime Environment (Zulu 8.60.0.21-CA-linux64) (build 1.8.0_322-b06)
OpenJDK 64-Bit Server VM (Zulu 8.60.0.21-CA-linux64) (build 25.322-b06, mixed mode)
```

## Setting it up
```
# download it
cd /opt
sudo curl -o zulu8.60.0.21-ca-jdk8.0.322-linux_x64.tar.gz https://cdn.azul.com/zulu/bin/zulu8.60.0.21-ca-jdk8.0.322-linux_x64.tar.gz

# unzip it
sudo tar -xvf zulu8.60.0.21-ca-jdk8.0.322-linux_x64.tar.gz

# add it to your path file ~/.bashrc
PATH="$PATH:/opt/zulu8.60.0.21-ca-jdk8.0.322-linux_x64/bin"
```


## Example commands
### Starting the server
https://github.com/synle/rest.li/blob/master/restli-example-server/src/main/java/com/linkedin/restli/example/RestLiExampleBasicServer.java

```
./gradlew startExampleBasicServer
```

### Starting the client
https://github.com/synle/rest.li/blob/master/restli-example-client/src/main/java/com/linkedin/restli/example/RestLiExampleBasicClient.java

```
./gradlew startExampleBasicClient
```


### Sample code and notes

Resource file is located here: https://github.com/synle/rest.li/blob/master/restli-example-server/src/main/java/com/linkedin/restli/example/impl/PhotoResource.java

Documentation regarding the resource can be found at https://linkedin.github.io/rest.li/user_guide/restli_server

#### Get by ID

```
...
@Override
public Photo get(Long key)
{
  return _db.getData().get(key);
}
...
```


```
curl http://localhost:7279/photos/1
```

```
{"urn":"1","format":"JPG","id":1,"title":"Photo 1","exif":{"location":{"latitude":66.7151,"longitude":-77.66235}}}
```


#### Batch Get

#### Update full

#### Update Partial (Patch)

#### Delete
```
@Override
public UpdateResponse delete(Long key)
{
  final boolean isRemoved = (_db.getData().remove(key) != null);

  // Remove this photo from all albums to maintain referential integrity.
  AlbumEntryResource.purge(_entryDb, null, key);

  return new UpdateResponse(isRemoved ? HttpStatus.S_204_NO_CONTENT
      : HttpStatus.S_404_NOT_FOUND);
}
```

```
curl -X DELETE http://localhost:7279/photos/1
```

```
HTTP/1.1 204 No Content
```

#### Finder

#### Batch Finder

#### Custom action name
These custom actions need to be sent as a post (`-X POST`)
```
@Action(name = "purge", resourceLevel = ResourceLevel.COLLECTION)
public int purge()
{
  final int numPurged = _db.getData().size();
  _db.getData().clear();

  AlbumEntryResource.purge(_entryDb, null, null);
  return numPurged;
}
```

```
curl -X POST "http://localhost:7279/photos?action=purge"
```

```
{"value":0}
```

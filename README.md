# Sy Notes
Sy's notes for Linkedin restli.


## Getting started
- https://linkedin.github.io/rest.li/get_started/quick_start


### Remote Debugging
Refer to [this stackoverflow for more information about remote debugging](Source: https://stackoverflow.com/questions/37702073/gradle-remote-debugging-process)

- Note The `"suspend=y"` part will pause the execution for you to attach a debugger.
- Start the server as usual but add the following param, so the command will look like this:
```
./gradlew startExampleBasicServer -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y'
```

```
./gradlew startExampleBasicClient -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5007,suspend=y'
```

or in the `~/.gradle/gradle.properties`
```
org.gradle.jvmargs=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y
```

Then in your InteliJ, add this configuration:

Use `Remote JVM Debug` and add this into the command line argument
`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`
![image](https://user-images.githubusercontent.com/3792401/155806095-ce429e57-cd6b-4d91-b421-521be5301c83.png)


VS Code Configuration
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Remote Debugger Attach",
      "request": "attach",
      "hostName": "localhost",
      "port": "5005"
    }
  ]
}
```

## Requirements
```bash
>>> java -version
openjdk version "1.8.0_322"
OpenJDK Runtime Environment (Zulu 8.60.0.21-CA-linux64) (build 1.8.0_322-b06)
OpenJDK 64-Bit Server VM (Zulu 8.60.0.21-CA-linux64) (build 25.322-b06, mixed mode)
```

## Setting it up
```bash
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

```bash
./gradlew startExampleBasicServer
```

### Starting the client
https://github.com/synle/rest.li/blob/master/restli-example-client/src/main/java/com/linkedin/restli/example/RestLiExampleBasicClient.java

```bash
./gradlew startExampleBasicClient
```

### Basic java client code
For java client code, [refer to this sample client code in java for more information about java client code](https://github.com/synle/rest.li/blob/master/restli-example-client/src/main/java/com/linkedin/restli/example/RestLiExampleBasicClient.java). Essentially, we need to initiate

```java
import import com.linkedin.restli.example.photos.PhotosRequestBuilders;
import com.linkedin.restli.client.RestClient;

//...
// this can be used to generate the request
private static final PhotosRequestBuilders _photoBuilders          = new PhotosRequestBuilders();

// this used to make the actual rest client call
private final RestClient _restClient;

//...
// This section of the code initialize the rest client
public static void main(String[] args) throws Exception
{
  final StringBuilder serverUrlBuilder = new StringBuilder("http://").append(SERVER_HOSTNAME).append(":").append(SERVER_PORT).append("/");
  final RestClient restClient = new RestClient(r2Client, serverUrlBuilder.toString());
  //...
}

// here's a sample way to call the rest client
private void getNonPhoto() throws RemoteInvocationException
{
  final Request<Photo> failReq = _photoBuilders.get().id(-1L).build();
  final ResponseFuture<Photo> failFuture = _restClient.sendRequest(failReq);
  final Response<Photo> failResponse = failFuture.getResponse();
}
```


### Sample code and notes

- Resource file is located here: https://github.com/synle/rest.li/blob/master/restli-example-server/src/main/java/com/linkedin/restli/example/impl/PhotoResource.java
- Documentation regarding the resource can be found at https://linkedin.github.io/rest.li/user_guide/restli_server
- Note that the snapshot is generated automatically by code
`rest.li/restli-example-api/src/main/snapshot/com.linkedin.restli.example.photos.photos.snapshot.json`
- All of these calls can be async. Just wrap the result into Task. So it's `Task<Map<K, V>>` instead of `Map<K, V>`

#### Get by ID

Annotate with this `@RestMethod.Get` or override the method as below:

```java
@Override
public Photo get(Long key)
{
  return _db.getData().get(key);
}
...
```


```bash
curl http://localhost:7279/photos/1
```

```json
{"urn":"1","format":"JPG","id":1,"title":"Photo 1","exif":{"location":{"latitude":66.7151,"longitude":-77.66235}}}
```

```java
// send request to retrieve created photo
private void getPhoto(PrintWriter respWriter, long newPhotoId) throws RemoteInvocationException
{
  final Request<Photo> getReq = _photoBuilders.get().id(newPhotoId).build();
  final ResponseFuture<Photo> getFuture = _restClient.sendRequest(getReq);
  final Response<Photo> getResp = getFuture.getResponse();
  respWriter.println("Photo: " + getResp.getEntity().toString());
}
```


#### Get All

Annotate with this `@RestMethod.BatchGet` or override the method as below:

```java
@Override
public List<Photo> getAll(@PagingContextParam PagingContext pagingContext)
{
  return new ArrayList<>(_db.getData().values());
}
```

```bash
curl http://localhost:7279/photos
```

```json
{"elements":[{"urn":"1","format":"PNG","id":1,"title":"Photo 1","exif":{"location":{"latitude":82.0381,"longitude":-64.79977}}},{"urn":"2","format":"$UNKNOWN","id":2,"title":"Photo 2","exif":{"location":{"latitude":61.78369,"longitude":16.80307}}},{"urn":"3","format":"JPG","id":3,"title":"Photo 3","exif":{"location":{"latitude":-30.974697,"longitude":-12.672791}}},{"urn":"4","format":"JPG","id":4,"title":"Photo 4","exif":{"location":{"latitude":0.5505829,"longitude":-60.278046}}},{"urn":"5","format":"JPG","id":5,"title":"Photo 5","exif":{"location":{"latitude":44.0925,"longitude":71.784805}}},{"urn":"6","format":"JPG","id":6,"title":"Photo 6","exif":{"location":{"latitude":-42.16773,"longitude":84.99654}}},{"urn":"7","format":"PNG","id":7,"title":"Photo 7","exif":{"location":{"latitude":-15.465607,"longitude":51.504105}}},{"urn":"8","format":"BMP","id":8,"title":"Photo 8","exif":{"location":{"latitude":74.12041,"longitude":31.77507}}},{"urn":"9","format":"$UNKNOWN","id":9,"title":"Photo 9","exif":{"location":{"latitude":78.718765,"longitude":-66.72035}}},{"urn":"10","format":"BMP","id":10,"title":"Photo 10","exif":{"location":{"latitude":15.1922455,"longitude":8.678276}}}],"paging":{"count":10,"start":0,"links":[{"rel":"next","type":"application/json","href":"/photos?start=10&count=10"}]}}
```


##### More notes
An annotated get method may also have arbitrary query params added:

```
@RestMethod.Get
public GetResult<V> get(K key, @QueryParam("viewerId") String viewerId);
```

#### Batch Get


Clients should make requests to a batch resource using buildKV() (not build(), it is deprecated), for example:

```
new FortunesBuilders().batchGet().ids(...).buildKV();
```

#### Update full

#### Update Partial (Patch)

```java
private void partialUpdatePhoto(PrintWriter respWriter, long photoId) throws RemoteInvocationException
{
  final Request<Photo> getReq = _photoBuilders.get().id(photoId).build();
  final ResponseFuture<Photo> getFuture = _restClient.sendRequest(getReq);
  final Response<Photo> getResp = getFuture.getResponse();
  final Photo originalPhoto = getResp.getEntity();

  final Photo updatedPhoto = new Photo().setTitle("Partially Updated Photo");
  final PatchRequest<Photo> patch = PatchGenerator.diff(originalPhoto, updatedPhoto);

  final Request<EmptyRecord> partialUpdateRequest = _photoBuilders.partialUpdate().id(photoId).input(patch).build();
  final int status = _restClient.sendRequest(partialUpdateRequest).getResponse().getStatus();
  respWriter.println("Partial update photo is successful: " + (status == 202));
}
```

#### Delete
```java
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

```bash
curl -X DELETE http://localhost:7279/photos/1
```

```bash
HTTP/1.1 204 No Content
```

#### @Finder

- Note that here we used the query string `q` to indicate the finder name. In this case `@Finder("titleAndOrFormat")` will need a query strign `q=titleAndOrFormat`

```java
@Finder("titleAndOrFormat")
public List<Photo> find(@PagingContextParam PagingContext pagingContext,
                        @QueryParam("title") @Optional String title,
                        @QueryParam("format") @Optional PhotoFormats format)
{
  System.out.println("\n\n>>>> @Finder (titleAndOrFormat) was called:" + pagingContext + ". title=" + title + ". format="+format);

  final List<Photo> photos = new ArrayList<>();
  int index = 0;
  final int begin = pagingContext.getStart();
  final int end = begin + pagingContext.getCount();
  final Collection<Photo> dbPhotos = _db.getData().values();
  for (Photo p : dbPhotos)
  {
    if (index == end)
    {
      break;
    }
    else if (index >= begin)
    {
      if (title == null || p.getTitle().equalsIgnoreCase(title))
      {
        if (format == null || format == p.getFormat())
        {
          photos.add(p);
        }
      }
    }

    index++;
  }
  return photos;
}
```

```bash
curl http://localhost:7279/photos?q=titleAndOrFormat&format=PNG
```

```json
{"elements":[{"urn":"1","format":"JPG","id":1,"title":"Photo 1","exif":{"location":{"latitude":-6.3178253,"longitude":57.696823}}},{"urn":"2","format":"JPG","id":2,"title":"Photo 2","exif":{"location":{"latitude":-5.5022736,"longitude":18.33355}}},{"urn":"3","format":"JPG","id":3,"title":"Photo 3","exif":{"location":{"latitude":84.11627,"longitude":-64.39552}}},{"urn":"4","format":"$UNKNOWN","id":4,"title":"Photo 4","exif":{"location":{"latitude":55.052383,"longitude":-7.9325027}}},{"urn":"5","format":"BMP","id":5,"title":"Photo 5","exif":{"location":{"latitude":-54.010677,"longitude":-60.018898}}},{"urn":"6","format":"PNG","id":6,"title":"Photo 6","exif":{"location":{"latitude":28.385742,"longitude":-71.32381}}},{"urn":"7","format":"JPG","id":7,"title":"Photo 7","exif":{"location":{"latitude":-15.153236,"longitude":-59.620316}}},{"urn":"8","format":"$UNKNOWN","id":8,"title":"Photo 8","exif":{"location":{"latitude":-83.43374,"longitude":77.27614}}},{"urn":"9","format":"BMP","id":9,"title":"Photo 9","exif":{"location":{"latitude":46.31407,"longitude":-84.41014}}},{"urn":"10","format":"JPG","id":10,"title":"Photo 10","exif":{"location":{"latitude":2.3792572,"longitude":36.30168}}}],"paging":{"count":10,"start":0,"links":[{"rel":"next","type":"application/json","href":"/photos?q=titleAndOrFormat&start=10&count=10"}]}}
```

#### @BatchFinder

- [More information on batchfinder here](https://linkedin.github.io/rest.li/batch_finder_resource_method#resource-api)


```java
@BatchFinder(value = "searchPhotos", batchParam = "criteria")
public BatchFinderResult<PhotoCriteria, Photo, NoMetadata> searchPhotos(@PagingContextParam PagingContext pagingContext,
    @QueryParam("criteria") PhotoCriteria[] criteria, @QueryParam("exif") @Optional EXIF exif)
{
  System.out.println("\n\n>>>> @BatchFinder (searchPhotos - criteria) was called:" + pagingContext + ". criteria=" + criteria);

  BatchFinderResult<PhotoCriteria, Photo, NoMetadata> batchFinderResult = new BatchFinderResult<>();

  for (PhotoCriteria currentCriteria: criteria) {
    if (currentCriteria.getTitle() != null) {
      // on success
      final List<Photo> photos = new ArrayList<>();
      int index = 0;
      final int begin = pagingContext.getStart();
      final int end = begin + pagingContext.getCount();
      final Collection<Photo> dbPhotos = _db.getData().values();
      for (Photo p : dbPhotos)
      {
        if (index == end)
        {
          break;
        }
        else if (index >= begin)
        {
          if (p.getTitle().equalsIgnoreCase(currentCriteria.getTitle()))
          {
            if (currentCriteria.getFormat() == null || currentCriteria.getFormat() == p.getFormat())
            {
              photos.add(p);
            }
          }
        }

        index++;
      }
      CollectionResult<Photo, NoMetadata> cr = new CollectionResult<>(photos, photos.size());
      batchFinderResult.putResult(currentCriteria, cr);
    } else {
      // on error: to construct error response for test
      batchFinderResult.putError(currentCriteria, new RestLiServiceException(HttpStatus.S_404_NOT_FOUND, "Failed to find Photo!"));
    }
  }

  return batchFinderResult;
}
```

```bash
curl 'http://localhost:7279/photos?bq=searchPhotos&criteria=List((format:PNG))' --header 'X-RestLi-Protocol-Version: 2.0.0'
```


#### Custom action name
These custom actions need to be sent as a post (`-X POST`)
```java
@Action(name = "purge", resourceLevel = ResourceLevel.COLLECTION)
public int purge()
{
  final int numPurged = _db.getData().size();
  _db.getData().clear();

  AlbumEntryResource.purge(_entryDb, null, null);
  return numPurged;
}
```

```bash
curl -X POST "http://localhost:7279/photos?action=purge"
```

```json
{"value":0}
```


```java
// call action purge to delete all photos on server
private void purgeAllPhotos(PrintWriter respWriter) throws RemoteInvocationException
{
  final Request<Integer> purgeReq = _photoBuilders.actionPurge().build();
  final ResponseFuture<Integer> purgeFuture = _restClient.sendRequest(purgeReq);
  final Response<Integer> purgeResp = purgeFuture.getResponse();
  respWriter.println("Purged " + purgeResp.getEntity() + " photos");
}
```

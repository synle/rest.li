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
org.gradle.daemon=false
org.gradle.jvmargs=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y
```

Then in your InteliJ, add this configuration:

Use `Remote JVM Debug` and add this into the command line argument
`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`
![image](https://user-images.githubusercontent.com/3792401/155806095-ce429e57-cd6b-4d91-b421-521be5301c83.png)

##### Sample Server Response

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
private static final PhotosRequestBuilders _photoBuilders = new PhotosRequestBuilders();

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

##### Sample Server Java Code

```java
@Override
public Photo get(Long key)
{
  return _db.getData().get(key);
}
...
```

##### Sample Curl Call

```bash
curl http://localhost:7279/photos/1
```

##### Sample Server Response

```json
{
  "urn": "1",
  "format": "JPG",
  "id": 1,
  "title": "Photo 1",
  "exif": { "location": { "latitude": 66.7151, "longitude": -77.66235 } }
}
```

##### Sample Client Java Code

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

##### Sample Server Java Code

```java
@Override
public List<Photo> getAll(@PagingContextParam PagingContext pagingContext)
{
  return new ArrayList<>(_db.getData().values());
}
```

##### Sample Curl Call

```bash
curl http://localhost:7279/photos
```

##### Sample Server Response

```json
{
  "elements": [
    {
      "urn": "1",
      "format": "PNG",
      "id": 1,
      "title": "Photo 1",
      "exif": { "location": { "latitude": 82.0381, "longitude": -64.79977 } }
    },
    {
      "urn": "2",
      "format": "$UNKNOWN",
      "id": 2,
      "title": "Photo 2",
      "exif": { "location": { "latitude": 61.78369, "longitude": 16.80307 } }
    },
    {
      "urn": "3",
      "format": "JPG",
      "id": 3,
      "title": "Photo 3",
      "exif": {
        "location": { "latitude": -30.974697, "longitude": -12.672791 }
      }
    },
    {
      "urn": "4",
      "format": "JPG",
      "id": 4,
      "title": "Photo 4",
      "exif": { "location": { "latitude": 0.5505829, "longitude": -60.278046 } }
    },
    {
      "urn": "5",
      "format": "JPG",
      "id": 5,
      "title": "Photo 5",
      "exif": { "location": { "latitude": 44.0925, "longitude": 71.784805 } }
    }
  ],
  "paging": {
    "count": 5,
    "start": 0,
    "links": [
      {
        "rel": "next",
        "type": "application/json",
        "href": "/photos?start=5&count=5"
      }
    ]
  }
}
```

##### Sample Client Java Code

```java
import com.linkedin.restli.client.GetAllRequest;

private void getAllPhotos(PrintWriter respWriter) throws RemoteInvocationException
{
  final GetAllRequest<Photo> getAllReq = _photoBuilders.getAll().build();
  final CollectionResponse<Photo> crPhotos = _restClient.sendRequest(getAllReq).getResponse().getEntity();
  final List<Photo> photos = crPhotos.getElements();

  respWriter.println("Get All Photo: " + photos.toString());
}
```

#### Batch Get

##### Sample Server Java Code

```java
// TODO
```

##### Sample Curl Call

```bash
# TODO
```

##### Sample Server Response

```json
{ "TODO": "TODO" }
```

##### Sample Client Java Code

```java
// TODO
```

#### Update full

##### Sample Server Java Code

```java
@Override
public UpdateResponse update(Long key, Photo entity)
{
  System.out.println("\n\n>>>> update was called:" + key + ". " + entity);


  final Photo currPhoto = _db.getData().get(key);
  if (currPhoto == null)
  {
    return new UpdateResponse(HttpStatus.S_404_NOT_FOUND);
  }
  //Disallow changing entity ID and URN
  //ID and URN are required fields, so use a dummy value to denote "empty" fields
  if ((entity.hasId() && entity.getId() != -1) || (entity.hasUrn() && !entity.getUrn().equals("")))
  {
    throw new RestLiServiceException(HttpStatus.S_400_BAD_REQUEST,
                                     "Photo ID is not acceptable in request");
  }

  // make sure the ID in the entity is consistent with the key in the database
  entity.setId(key);
  entity.setUrn(String.valueOf(key));
  _db.getData().put(key, entity);
  return new UpdateResponse(HttpStatus.S_204_NO_CONTENT);
}
```

##### Sample Curl Call

```bash
# TODO
```

##### Sample Server Response

```json
{ "TODO": "TODO" }
```

##### Sample Client Java Code

```java
private void createPhotoAsync(final PrintWriter respWriter, final CountDownLatch latch, final long newPhotoId)
{
  // this resembles to photo-create-id.json
  final LatLong newLatLong = new LatLong().setLatitude(40.725f).setLongitude(-74.005f);
  final EXIF newExif = new EXIF().setIsFlash(false).setLocation(newLatLong);
  final Photo newPhoto = new Photo().setTitle("Updated Photo").setFormat(PhotoFormats.JPG).setExif(newExif);

  final Request<EmptyRecord> createReq2 = _photoBuilders.update().id(newPhotoId).input(newPhoto).build();

  // send request with callback
  _restClient.sendRequest(createReq2);
}
```

#### Update Partial (Patch)

```java
// TODO
```

##### Sample Curl Call

```bash
# TODO
```

##### Sample Server Response

```json
{ "TODO": "TODO" }
```

##### Sample Client Java Code

```java
private void partialUpdatePhoto(PrintWriter respWriter, long photoId) throws RemoteInvocationException
{
  // get the original photo
  final Request<Photo> getReq = _photoBuilders.get().id(photoId).build();
  final ResponseFuture<Photo> getFuture = _restClient.sendRequest(getReq);
  final Response<Photo> getResp = getFuture.getResponse();
  final Photo originalPhoto = getResp.getEntity();

  // make a partial change and generate the diff
  final Photo updatedPhoto = new Photo().setTitle("Partially Updated Photo");
  final PatchRequest<Photo> patch = PatchGenerator.diff(originalPhoto, updatedPhoto);

  // send update request
  final Request<EmptyRecord> partialUpdateRequest = _photoBuilders.partialUpdate().id(photoId).input(patch).build();
  final int status = _restClient.sendRequest(partialUpdateRequest).getResponse().getStatus();
  respWriter.println("Partial update photo is successful: " + (status == 202));
}
```

#### Delete

##### Sample Server Java Code

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

##### Sample Curl Call

```bash
curl -X DELETE http://localhost:7279/photos/1
```

```bash
HTTP/1.1 204 No Content
```

##### Sample Client Java Code

```java
import com.linkedin.restli.client.DeleteRequest;

private void deletePhoto(PrintWriter respWriter) throws RemoteInvocationException {
  final DeleteRequest<Photo> deleteRequest = _photoBuilders.delete().id(2L).build();
  final int status = _restClient.sendRequest(deleteRequest).getResponse().getStatus();
  respWriter.println("Delete Photo: StatusCode=" + status);
}
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

##### Sample Curl Call

```bash
curl http://localhost:7279/photos?q=titleAndOrFormat&format=PNG
```

##### Sample Server Response

```json
{
  "elements": [
    {
      "urn": "1",
      "format": "JPG",
      "id": 1,
      "title": "Photo 1",
      "exif": { "location": { "latitude": -6.3178253, "longitude": 57.696823 } }
    },
    {
      "urn": "2",
      "format": "JPG",
      "id": 2,
      "title": "Photo 2",
      "exif": { "location": { "latitude": -5.5022736, "longitude": 18.33355 } }
    },
    ... truncated for readability ...
  ],
  "paging": {
    "count": 10,
    "start": 0,
    "links": [
      {
        "rel": "next",
        "type": "application/json",
        "href": "/photos?q=titleAndOrFormat&start=10&count=10"
      }
    ]
  }
}
```

##### Sample Client Java Code

```java
private void findPhoto(PrintWriter respWriter) throws RemoteInvocationException
{
  final FindRequest<Photo> findReq = _photoBuilders
      .findByTitleAndOrFormat()
      .titleParam("Photo 1")
      .formatParam("PNG")
      .build();

  final CollectionResponse<Photo> crPhotos = _restClient.sendRequest(findReq).getResponse().getEntity();
  final List<Photo> photos = crPhotos.getElements();

  respWriter.println("Found " + photos.size() + " photos with title " + photo.getTitle());
}
```

#### @BatchFinder

- [More information on batchfinder here](https://linkedin.github.io/rest.li/batch_finder_resource_method#resource-api)
- Note that here we used the query string `bq` to indicate the batch finder name and `criteria` for the list of search filter. In this case `@BatchFinder(value = "searchPhotos", batchParam = "criteria")` will need two query strings `bq=searchPhotos` and `criteria=List((format:PNG))`.

##### Sample Server Java Code

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

##### Sample Curl Call

```bash
curl 'http://localhost:7279/photos?bq=searchPhotos&criteria=List((format:PNG))' --header 'X-RestLi-Protocol-Version: 2.0.0'
```

##### Sample Server Response

```json
{ "TODO": "TODO" }
```

##### Sample Client Java Code

```java
// TODO
```

#### Custom action name

These custom actions need to be sent as a post (`-X POST`)

##### Sample Server Java Code

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

##### Sample Curl Call

```bash
curl -X POST "http://localhost:7279/photos?action=purge"
```

##### Sample Server Response

```json
{ "value": 0 }
```

##### Sample Client Java Code

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

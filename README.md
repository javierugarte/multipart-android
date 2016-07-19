Multipart Request
=======
[ ![Download](https://api.bintray.com/packages/javiergm/maven/multipart-android/images/download.svg) ](https://bintray.com/javiergm/maven/multipart-android/_latestVersion)

What is http multipart request?
=======
A HTTP multipart request is a HTTP request that HTTP clients construct to send files and data over to a HTTP Server. It is commonly used by browsers and HTTP clients to uploawd files to the server.

How to use this library
=======

- Using Gradle

```groovy
	compile 'com.bikomobile:multipart:1.1.0'
```
- Using Maven

```xml
	<dependency>
		<groupId>com.bikomobile</groupId>
		<artifactId>multipart</artifactId>
		<version>1.1.0</version>
		<type>pom</type>
	</dependency>
```

- Compile you project with android sdk v15+

API
=======

- Constructs the object

```java
Multipart multipart = new Multipart(context);
```

- Add a file

```java
multipart.addFile("image/jpeg", "image", name, imageUri);
multipart.addFile("video/mp4", "video", name, videoUri);
```

- Add post params

```java
multipart.addParam("key1", "value1");
multipart.addParam("key2", "value2");
// or

HashMap<String, String> params = new HashMap<>();
params.put("key1", "value1");
params.put("key2", "value2");
        
multipart.addParams(params);
```

- Launch request

```java
multipart.launchRequest("url", new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
            }
        });
```

- Or get request and launch yourself

```java
MultipartRequest multipartRequest = multipart.getRequest("url", new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
            }
        });

VolleySingleton.getInstance(this).addToRequestQueue(multipartRequest);
```


Contribute
=======

* [X] Add more files
* [X] Add others post params


About me
=======

License
=======

Multipart Request
=======

What is http multipart request?
=======
A HTTP multipart request is a HTTP request that HTTP clients construct to send files and data over to a HTTP Server. It is commonly used by browsers and HTTP clients to uploawd files to the server.

How to use this library
=======

- Using Gradle

```groovy
    compile 'com.cocosw:bottomsheet:1.+@aar'
```
- Using Maven

```xml
<dependency>
    <groupId>com.cocosw</groupId>
    <artifactId>bottomsheet</artifactId>
    <version>1.x</version>
    <type>apklib</type>
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
multipart.addFile(name, videoUri);
```

- Launch request or get request to launch yourself

```java
multipart.launchRequest("url", "postParam", new Response.Listener<NetworkResponse>() {
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
MultipartRequest multipartRequest = multipart.getRequest("url", "postParam", new Response.Listener<NetworkResponse>() {
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

- Add more files

About me
=======

License
=======

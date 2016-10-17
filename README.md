# Multipart Request

[ ![Download](https://api.bintray.com/packages/javiergm/maven/Multipart/images/download.svg) ](https://bintray.com/javiergm/maven/Multipart/_latestVersion)

## What is http multipart request?
A HTTP multipart request is a HTTP request that HTTP clients construct to send files and data over to a HTTP Server. It is commonly used by browsers and HTTP clients to uploawd files to the server.

## How to use this library

- Using Gradle

```groovy
	compile 'com.bikomobile:multipart:1.3.4'
```
- Using Maven

```xml
	<dependency>
		<groupId>com.bikomobile</groupId>
		<artifactId>multipart</artifactId>
		<version>1.3.4</version>
		<type>pom</type>
	</dependency>
```

- Compile you project with android sdk v15+

## API

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


## Contribute

* [X] Add more files
* [X] Add others post params
* [X] Upload google drive videos


## About me


## License
```
Copyright 2016 Javier González

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


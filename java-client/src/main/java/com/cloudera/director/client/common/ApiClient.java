// Licensed to Cloudera, Inc. under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  Cloudera, Inc. licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Wordnik.com's Swagger generic API client. This client handles the client-
 * server communication, and is invariant across implementations. Specifics of
 * the methods and models for each application are generated from the Swagger
 * templates.
 */

// Note: This file is auto generated. Do not edit manually.

package com.cloudera.director.client.common;

import com.fasterxml.jackson.databind.JavaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.multipart.FormDataMultiPart;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.Status.Family;

public class ApiClient {
  private Map<String, Client> hostMap = new HashMap<String, Client>();
  private Map<String, String> defaultHeaderMap = new HashMap<String, String>();
  private Set<Cookie> cookies = new HashSet<Cookie>();
  private String basePath;
  private boolean isDebug = false;
  private String username;
  private String password;

  public void enableDebug() {
    isDebug = true;
  }

  public void addDefaultHeader(String key, String value) {
     defaultHeaderMap.put(key, value);
  }

  public String escapeString(String str) {
    try {
      return URLEncoder.encode(str, "UTF-8").replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      return str;
    }
  }

  public String getBasePath() {
    return this.basePath;
  }

  public ApiClient(String apiServer) {
    this.basePath = Preconditions.checkNotNull(apiServer, "apiServer is null");
  }

  public ApiClient(String apiServer, String username, String password) {
    this.basePath = Preconditions.checkNotNull(apiServer, "apiServer is null");
    this.username = Preconditions.checkNotNull(username, "username is null");
    this.password = Preconditions.checkNotNull(password, "password is null");
  }

  public static Object deserialize(String json, String containerType, Class cls) throws ApiException {
    try {
      if ("List".equals(containerType)) {
        JavaType typeInfo = JsonUtil.getJsonMapper().getTypeFactory().constructCollectionType(List.class, cls);
        List response = (List<?>) JsonUtil.getJsonMapper().readValue(json, typeInfo);
        return response;
      } else if (String.class.equals(cls)) {
        if (json != null && json.startsWith("\"") && json.endsWith("\"") && json.length() > 1)
          return json.substring(1, json.length() - 2);
        else
          return json;
      } else {
        return JsonUtil.getJsonMapper().readValue(json, cls);
      }
    } catch (IOException e) {
      throw new ApiException(500, e.getMessage());
    }
  }

  public static String serialize(Object obj) throws ApiException {
    try {
      if (obj != null)
        return JsonUtil.getJsonMapper().writeValueAsString(obj);
      else
        return null;
    } catch (Exception e) {
      throw new ApiException(500, e.getMessage());
    }
  }

  @SuppressWarnings("PMD.EmptyCatchBlock")
  public String invokeAPI(String path, String method, Map<String, String> queryParams, Object body,
    Map<String, String> headerParams, Map<String, String> formParams, String contentType) throws ApiException {

    String host = getBasePath();
    Client client;

    if (username == null || password == null) {
      client = getClient(host);
    } else {
      client = getClient(host, username, password);
    }

    StringBuilder b = new StringBuilder();

    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (value != null) {
        if (b.toString().length() == 0)
          b.append("?");
        else
          b.append("&");
        b.append(escapeString(key)).append("=").append(escapeString(value));
      }
    }
    String querystring = b.toString();

    Builder builder = client.resource(host + path + querystring).accept("application/json");
    for (Map.Entry<String, String> entry : headerParams.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }
    for (Cookie c : cookies) {
      builder.cookie(c);
    }

    for (String key : defaultHeaderMap.keySet()) {
      if (!headerParams.containsKey(key)) {
        builder.header(key, defaultHeaderMap.get(key));
      }
    }
    ClientResponse response = null;

    if ("GET".equals(method)) {
      response = (ClientResponse) builder.get(ClientResponse.class);
    } else if ("POST".equals(method)) {
      if (body == null) {
        response = builder.post(ClientResponse.class, null);
      } else if (body instanceof FormDataMultiPart) {
        response = builder.type(contentType).post(ClientResponse.class, body);
      } else {
        if ("text/plain".equals(contentType)) {
          response = builder.type(contentType).post(ClientResponse.class, body);
        } else {
          response = builder.type(contentType).post(ClientResponse.class, serialize(body));
        }
      }
    } else if ("PUT".equals(method)) {
      if (body == null) {
        response = builder.put(ClientResponse.class, null);
      } else {
        if ("application/x-www-form-urlencoded".equals(contentType)) {
          StringBuilder formParamBuilder = new StringBuilder();

          // encode the form params
          for (Map.Entry<String, String> entry : formParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !"".equals(value.trim())) {
              if (formParamBuilder.length() > 0) {
                formParamBuilder.append("&");
              }
              try {
                formParamBuilder.append(URLEncoder.encode(key, "UTF-8"))
                  .append("=")
                  .append(URLEncoder.encode(value, "UTF-8"));
              } catch (UnsupportedEncodingException e) {
                // move on to next
              }
            }
          }
          response = builder.type(contentType).put(ClientResponse.class, formParamBuilder.toString());
        } else {
          response = builder.type(contentType).put(ClientResponse.class, serialize(body));
        }
      }
    } else if ("DELETE".equals(method)) {
      if (body == null)
        response = builder.delete(ClientResponse.class, null);
      else
        response = builder.type(contentType).delete(ClientResponse.class, serialize(body));
    } else {
      throw new ApiException(500, "unknown method type " + method);
    }
    if (response.getClientResponseStatus() == ClientResponse.Status.NO_CONTENT) {
      return null;
    } else if (response.getClientResponseStatus().getFamily() == Family.SUCCESSFUL) {
      for (NewCookie newCookie : response.getCookies()) {
        cookies.add(newCookie.toCookie());
      }
      return (String) response.getEntity(String.class);
    } else {
      throw new ApiException(
                response.getClientResponseStatus().getStatusCode(),
                response.getEntity(String.class));
    }
  }

  private Client getClient(String host) {
    if (!hostMap.containsKey(host)) {
      Client client = Client.create();
      if (isDebug) {
        client.addFilter(new LoggingFilter());
      }

      hostMap.put(host, client);
    }
    return hostMap.get(host);
  }

  private Client getClient(String host, String username, String password) {
    if (!hostMap.containsKey(host)) {
      ClientConfig clientConfig = new DefaultClientConfig();
      Client client = Client.create(clientConfig);
      client.addFilter(new HTTPBasicAuthFilter(username, password));

      if (isDebug) {
        client.addFilter(new LoggingFilter());
      }

      hostMap.put(host, client);
    }
    return hostMap.get(host);
  }
}


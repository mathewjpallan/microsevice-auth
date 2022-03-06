# Authentication and Authorization in microservices

Authentication and Authorization in microservices using Keycloak

## Pre-requisites

1. Install Docker

2. Create a docker network

       docker network create authnet

3. Run mysql on docker 
```
Use a path on your local filesystem instead of /data/mysql8 in the command below so that mysql can persist data to disk

Also if you have an existing mysql instance, then just add a schema called keycloak and user keycloak with password as keycloak

docker run --rm --name mysql8 --net authnet -e MYSQL_DATABASE=keycloak -e MYSQL_USER=keycloak -e MYSQL_PASSWORD=keycloak -e MYSQL_ROOT_PASSWORD=root -d -p 3306:3306 -v /data/mysql8:/var/lib/mysql mysql:8.0.28
```

3. Run keycloak on docker
```
Change the parameters in the below command if your mysql credentials/database name/hostname are different.

docker run --rm --name keycloak16 --net authnet -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e DB_VENDOR=mysql -e DB_ADDR=mysql8 -e DB_PORT=3306 -e DB_USER=keycloak -e DB_PASSWORD=keycloak -e DB_DATABASE=keycloak -d -p 18080:8080 jboss/keycloak:16.1.1
```

## Configuring Keycloak

We will be setting up roles in keycloak and setting up users with these roles. The roles are viewer, editor and approver. We would then login with these users to retrieve access tokens and use these to invoke the APIs defined in service1.

### Setup keycloak realm and user settings
- Navigate to http://localhost:18080 with username and password as admin
- Use the add realm option to add a new realm - microservices
- Create a new client under the realm - microservices-client. Access Type - Confidential, Valid redirect URI - http://localhost:8000/login
- Create viewer, editor, approver roles under the realm. The editor is a composite role that includes viewer role as well. Approver role would be a composite role having viewer access but no editor access.
- Create 3 users and assign them with the corresponding roles. Set the password for these users while setting them up.

## Running the service1 microservice

This microservice has 3 endpoints and has the boilerplate code to setup spring security and keycloak integration. These integrations allow us use annotations to define the roles that are allowed to invoke each of the endpoints.

Run the service1 by importing it to any IDE as a maven project and running the Service1Application class. The application.properties has the defaults which would work with the above mentioned configuration

## Testing end to end
- Navigate to realm settings > General tab > OpenID Endpoint Configuration to get metadata about all the endpoints that we need. 
- The authorization_endpoint is used to login as a user. A query string has to be added to the authorization_endpoint to access the user login page. The redirect_uri provided in the query string should match the redirect URI(s) set above while creating the client in the realm. It is best to try this on an incognito window so that there are no other keycloak sessions on the browser. After login, you will get redirected to http://localhost:8000/login with a code. This call would fail as this is a fake endpoint that we have not implemented and is just to allow us to retrieve the code that we can exchange with keycloak to get an access token. In a real system, forwarding of the user to the authorization_endpoint and retrieving the code and exchanging it for the access/refresh token would be handled by the frontend by integrating with a keycloak/OIDC library.


```
http://localhost:18080/auth/realms/microservices/protocol/openid-connect/auth?client_id=microservices-client&state=abcd&redirect_uri=http%3A%2F%2Flocalhost:8000%2Flogin&scope=openid&response_type=code
```

- Use the code retrieved from the redirect callback to http://localhost:8000/login in the earlier step (failed redirect) and use the below curl to get the access token for the user. The code header in the curl is from the failed redirect. The client_secret needs to be replaced with the value from the credential tab of the microservices-client in keycloak.

```
curl -X POST 'http://localhost:18080/auth/realms/microservices/protocol/openid-connect/token' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'grant_type=authorization_code' \
 --data-urlencode 'client_id=microservices-client' \
 --data-urlencode 'client_secret=H6DchPZC3sOlKzfrpC9gjO4PHUI24FUS' \
 --data-urlencode 'code=498ac452-d212-45f0-a7fd-d5b7eb094ef1.7972fabe-0717-440e-bb88-4a65eb483e94.fa18ed5b-db33-4c30-a992-d57fc68afa1d' \
 --data-urlencode 'redirect_uri=http://localhost:8000/login'

```

- Now use the access token in the response from the above curl to access the service1 endpoints. You can verify that the viewer token can be used to only access the viewer endpoint, editor token can be used to access viewer and editor endpoints. The approver can invoke viewer and approver endpoints, but not the editor endpoint.

```
curl localhost:8000/api/viewer -H 'Authorization: bearer <insert-access-token-here'
curl localhost:8000/api/editor -H 'Authorization: bearer <insert-access-token-here'
curl localhost:8000/api/approver -H 'Authorization: bearer <insert-access-token-here'

```


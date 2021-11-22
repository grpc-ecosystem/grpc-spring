#!/bin/bash

echo "Generating certificates"
openssl req -x509 -nodes -subj "//CN=localhost" -newkey rsa:4096 -sha256 -keyout server.key -out server.crt -days 3650
openssl req -x509 -nodes -subj "//CN=client1" -newkey rsa:4096 -sha256 -keyout client1.key -out client1.crt -days 3650
openssl req -x509 -nodes -subj "//CN=client2" -newkey rsa:4096 -sha256 -keyout client2.key -out client2.crt -days 3650

echo "Generating trustCertCollection"
cat server.crt > trusted-servers-collection
cat client1.crt client2.crt > trusted-clients-collection

echo "Creating key and trust stores"
openssl pkcs12 -export -name server -in server.crt -inkey server.key -passout 'pass:' -out server.p12
openssl pkcs12 -export -name client1 -in client1.crt -inkey client1.key -passout 'pass:' -out client1.p12
openssl pkcs12 -export -name client2 -in client2.crt -inkey client2.key -passout 'pass:' -out client2.p12

# create trusted-clients.p12 and trusted-servers.p12 via keystore-explorer and java 8
echo "--- DONE ---"

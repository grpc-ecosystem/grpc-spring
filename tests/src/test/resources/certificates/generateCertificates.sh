#!/bin/bash

echo "Generating certificates"
openssl req -x509 -nodes -subj "//CN=localhost" -newkey rsa:4096 -sha256 -keyout server.key -out server.crt -days 3650
openssl req -x509 -nodes -subj "//CN=client1" -newkey rsa:4096 -sha256 -keyout client1.key -out client1.crt -days 3650
openssl req -x509 -nodes -subj "//CN=client2" -newkey rsa:4096 -sha256 -keyout client2.key -out client2.crt -days 3650

echo "Generating trustCertCollection"
rm trusted-servers-collection
rm trusted-clients-collection
cat server.crt >> trusted-servers-collection
cat client1.crt >> trusted-clients-collection
cat client2.crt >> trusted-clients-collection

echo "--- DONE ---"

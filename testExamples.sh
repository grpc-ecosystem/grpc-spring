#!/bin/bash
set -e # Fail on error
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT # Kill subprocesses on exit
./gradlew clean --console=plain
./gradlew build --console=plain
sleep 2s

## Local
localTest() {
	echo "Starting Local test"

	# Run environment
	./gradlew :example:local-grpc-server:bootRun -x jar -x classes --console=plain &
	LOCAL_SERVER=$!
	sleep 10s # Wait for the server to start
	./gradlew :example:local-grpc-client:bootRun -x jar -x classes --console=plain &
	LOCAL_CLIENT=$!
	sleep 30s # Wait for the client to start and the server to be ready

	# Test
	RESPONSE=$(curl -s localhost:8080/)
	echo "Response:"
	echo "$RESPONSE"
	EXPECTED=$(echo -e "Hello ==> Michael")
	echo "Expected:"
	echo "$EXPECTED"
	sleep 1s # Give the user a chance to look at the result

	# Shutdown
	echo "Triggering shutdown"
	kill -s TERM $LOCAL_SERVER
	kill -s TERM $LOCAL_CLIENT
	sleep 1s # Wait for the shutdown logs to pass 

	# Verify
	if [ "$RESPONSE" = "$EXPECTED" ]; then
		echo "#----------------------#"
		echo "| Local example works! |"
		echo "#----------------------#"
	else
		echo "#-----------------------#"
		echo "| Local example failed! |"
		echo "#-----------------------#"
		exit 1
	fi
}

## Cloud
cloudTest() {
	echo "Starting Cloud test"

	# Run environment
	./gradlew :example:cloud-eureka-server:bootRun -x jar -x classes --console=plain &
	EUREKA=$!
	sleep 10s # Wait for the server to start

	mkdir -p zipkin
	cd zipkin
	echo "*" > .gitignore
	if [ ! -f zipkin.jar ]; then
		curl -sSL https://zipkin.io/quickstart.sh | bash -s
	fi
	java -jar zipkin.jar &
	ZIPKIN=$!
	sleep 10s # Wait for the server to start
	cd ..

	./gradlew :example:cloud-grpc-server:bootRun -x jar -x classes --console=plain &
	CLOUD_SERVER=$!
	sleep 10s # Wait for the server to start

	./gradlew :example:cloud-grpc-client:bootRun -x jar -x classes --console=plain &
	CLOUD_CLIENT=$!
	sleep 30s # Wait for the client to start and the server to be ready
	sleep 60s # Wait for the discovery service to refresh

	# Test
	RESPONSE=$(curl -s localhost:8080/)
	echo "Response:"
	echo "$RESPONSE"
	EXPECTED=$(echo -e "Hello ==> Michael")
	echo "Expected:"
	echo "$EXPECTED"
	sleep 1s # Give the user a chance to look at the result

	# Crash server
	kill -s TERM $CLOUD_SERVER
	echo "The server crashed (expected)"
	sleep 1s # Wait for the shutdown logs to pass

	# and restart server
	./gradlew :example:cloud-grpc-server:bootRun -x jar -x classes --console=plain &
	CLOUD_SERVER=$!
	sleep 30s # Wait for the server to start
	sleep 60s # Wait for the discovery service to refresh
	
	# Test again
	RESPONSE2=$(curl -s localhost:8080/)
	echo "Response:"
	echo "$RESPONSE2"
	EXPECTED=$(echo -e "Hello ==> Michael")
	echo "Expected:"
	echo "$EXPECTED"
	sleep 1s # Give the user a chance to look at the result

	# Shutdown
	echo "Triggering shutdown"
	kill -s TERM $EUREKA
	kill -s TERM $ZIPKIN
	kill -s TERM $CLOUD_SERVER
	kill -s TERM $CLOUD_CLIENT
	sleep 1s # Wait for the shutdown logs to pass

	# Verify part 1
	if [ "$RESPONSE" = "$EXPECTED" ]; then
		echo "#-----------------------------#"
		echo "| Cloud example part 1 works! |"
		echo "#-----------------------------#"
	else
		echo "#------------------------------#"
		echo "| Cloud example part 1 failed! |"
		echo "#------------------------------#"
		exit 1
	fi

	# Verify part 2
	if [ "$RESPONSE2" = "$EXPECTED" ]; then
		echo "#-----------------------------#"
		echo "| Cloud example part 2 works! |"
		echo "#-----------------------------#"
	else
		echo "#------------------------------#"
		echo "| Cloud example part 2 failed! |"
		echo "#------------------------------#"
		exit 1
	fi
}

## Security Basic Auth
securityBasicAuthTest() {
	echo "Starting Security Basic Auth test"

	# Run environment
	./gradlew :example:security-grpc-server:bootRun -x jar -x classes --console=plain &
	LOCAL_SERVER=$!
	sleep 10s # Wait for the server to start
	./gradlew :example:security-grpc-client:bootRun -x jar -x classes --console=plain &
	LOCAL_CLIENT=$!
	sleep 30s # Wait for the client to start and the server to be ready

	# Test
	RESPONSE=$(curl -s localhost:8080/)
	echo "Response:"
	echo "$RESPONSE"
	EXPECTED=$(echo -e "Input:\n- name: Michael (Changeable via URL param ?name=X)\nRequest-Context:\n- auth user: user (Configure via application.yml)\nResponse:\nHello ==> Michael")
	echo "Expected:"
	echo "$EXPECTED"
	sleep 1s # Give the user a chance to look at the result

	# Shutdown
	echo "Triggering shutdown"
	kill -s TERM $LOCAL_SERVER
	kill -s TERM $LOCAL_CLIENT
	sleep 1s # Wait for the shutdown logs to pass

	# Verify
	if [ "$RESPONSE" = "$EXPECTED" ]; then
		echo "#------------------------------------#"
		echo "| Security Basic Auth example works! |"
		echo "#------------------------------------#"
	else
		echo "#-------------------------------------#"
		echo "| Security Basic Auth example failed! |"
		echo "#-------------------------------------#"
		exit 1
	fi
}

## Security Bearer Auth
securityBearerAuthTest() {
	echo "Starting Security Bearer Auth test"

	# Run environment
	./gradlew :example:security-grpc-bearerAuth-server:bootRun -x jar -x classes --console=plain &
	LOCAL_SERVER=$!
	sleep 10s # Wait for the server to start
	./gradlew :example:security-grpc-bearerAuth-client:bootRun -x jar -x classes --console=plain &
	LOCAL_CLIENT=$!
	sleep 30s # Wait for the client to start and the server to be ready

	# Test
	RESPONSE=$(curl -s localhost:8080/)
	echo "Response:"
	echo "$RESPONSE"
	EXPECTED=$(echo -e "Input:\nMessage: test, Bearer Token is configured in SecurityConfiguration Class\nResponse:\nHello ==> test")
	echo "Expected:"
	echo "$EXPECTED"
	sleep 1s # Give the user a chance to look at the result

	# Shutdown
	echo "Triggering shutdown"
	kill -s TERM $LOCAL_SERVER
	kill -s TERM $LOCAL_CLIENT
	sleep 1s # Wait for the shutdown logs to pass

	# Verify
	if [ "$RESPONSE" = "$EXPECTED" ]; then
		echo "#-------------------------------------#"
		echo "| Security Bearer Auth example works! |"
		echo "#-------------------------------------#"
	else
		echo "#--------------------------------------#"
		echo "| Security Bearer Auth example failed! |"
		echo "#--------------------------------------#"
		exit 1
	fi
}

## Tests
localTest
cloudTest
securityBasicAuthTest
#securityBearerAuthTest

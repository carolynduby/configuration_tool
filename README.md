# configuration_tool
Configuration tool creates environment configuration files based on templates.

## How to Use

Create the following directory structure:
```
templates
   < template >.sh and <template >.properties
envs
   env_name
       env.properties
```
Create templates for .sh and .properties.  Use ${VARIABLE_NAME} to substitute a variable value into the environment files.  In the template below ${KAFKA_BROKER} is  a variable.
```
kafka.bootstrap.servers=${KAFKA_BROKER}
```
All other file extensions are copied from the templates directory to the generated config files without any modifications.

Edit the env.properties file and add the values of the variables specific to the environment:
```
KAFKA_BROKER=mybroker:9092
```
Build the config program.
```
cd <repo directory>
mvn clean package
```

Run the config program. 
```
java -classpath target/configuration_tool-jar-with-dependencies.jar com.example.cybersec.CybersecConfigGenerator <template directory> <env directory containing env.properties>
```
The generated config files are created in <env dir>/configs.

## Example
```
# Defining the variables.
 % mkdir -p /tmp/envs/env_nam
 % echo 'KAFKA_BROKER=mykafkabroker:9092' > /tmp/envs/env_name/env.properties com.example.cybersec.CybersecConfigGenerator <template directory> <env directory containing env.properties>
 % cat /tmp/envs/env_name/env.properties 
 KAFKA_BROKER=mykafkabroker:9092

 # Creating the templates
 % mkdir /tmp/templates
 % echo 'kafka.bootstrap.servers=${KAFKA_BROKER}' > /tmp/templates/myproperties.properties
 % cat /tmp/templates/myproperties.properties 
 kafka.bootstrap.servers=${KAFKA_BROKER}

 # Run config tool to populate the template
 % java -classpath target/configuration_tool-jar-with-dependencies.jar com.example.cybersec.CybersecConfigGenerator /tmp/envs/env_name /tmp/templates
   Populating template /tmp/templates/myproperties.properties

 # The KAFKA_BROKER variable is replace with its value
 # config files are in the configs subdirectory of the env dir
 % cat /tmp/envs/env_name/configs/myproperties.properties 
kafka.bootstrap.servers=mykafkabroker:9092
```

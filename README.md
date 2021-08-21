# south-african-numbers

## Description

This project verifies South African Mobile Numbers

## Project setup

```bash
mvn eclipse:eclipse

mvn clean package install
```
## san-kernel

It's the base implementation. It's a black box with a java API

## san-cli

It's a console implementation. It uses san-kernel

You can find executable jar in "south-african-numbers/san-cli/target"


### Interact with cli

#### Help

```bash
java -jar san-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar 
```

```
usage: java -jar san-cli.jar
 -file <file>       file to check
 -number <number>   number to check
 -output <output>   file to save
```

#### Check xls file

xls file must use the following format:

id | number 
--- | --- 
1 | 27831234567
2 | 831234567
3 | 1242343ab

Returns a file with the following format

id | number | status | suggested
--- | --- | --- | --- 
1 | 27831234567 | OK |
2 | 831234567 | CORRECTED | 27831234567
3 | 831234567 | CORRECTED | 27831234567
3 | 1242343ab | WRONG |

example

```bash
java -jar san-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar -file ../src/test/resources/South_African_Mobile_Numbers.xlsx
```

#### Check single number

example

```bash
java -jar san-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar -number 1234
```

```
Number: 1234 - Status: WRONG
```

## san-web

It's a REST API implementation

### API checkNumbers

#### POST checkNumbers?format=<text/csv | text/html>

- format: url param can be  text/csv or text/html
- numbers: formdata param

- returns file in the requested format 

### API checkNumber

#### GET checkNumber?number

- number: number to check
- returns a json 

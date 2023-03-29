# Triplex

A utility for triple extraction.
 Extract from csv file obtained using preprocessing [scripts](https://github.com/semlab/tree/master/datasets). 

## Requirements 

The project rely on the following library:

* [Stanford CoreNLP library](https://github.com/stanfordnlp/CoreNLP).
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli) to parse parameters.
* [opencsv](https://opencsv.sourceforge.net/) and its dependencies to handle csv files

Necessary jars:

* commons-beanutils-1.9.4.jar
* commons-cli-1.5.0.jar
* commons-collections4-4.4.jar
* commons-lang3-3.12.0.jar
* commons-text-1.10.0.jar
* opencsv-5.7.1.jar
* protobuf-java-3.11.4.jar
* slf4j-api.jar
* slf4j-simple.jar
* stanford-corenlp-4.3.2.jar
* stanford-corenlp-4.3.2-models.jar

## Usage

For help run:

```
triplex --help
```

sample usage :

```
triplex --input reuters.csv --output triples.csv --tokens tokens.csv
```


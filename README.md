# Triplex

A utility for triple extraction.
The project is composed of a library **triplex** and a command line tool 
**triplextraction**. 

## Requirements 

The project rely on the following library:

* [Stanford CoreNLP library](https://github.com/stanfordnlp/CoreNLP).
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli) to parse parameters.
* [opencsv](https://opencsv.sourceforge.net/) and its dependencies to handle csv files

## Usage

For help run:

```
triplex --help
```

sample usage :

```
triplex --input reuters.csv --output triples.csv --tokens tokens.csv
```

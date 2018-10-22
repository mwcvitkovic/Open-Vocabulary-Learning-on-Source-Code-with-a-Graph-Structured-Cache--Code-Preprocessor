# What is this?
This library turns java source code (.java files) into Augmented ASTs (.gml ([graphml](http://graphml.graphdrawing.org/)) files) as per the paper [Open Vocabulary Learning on Source Code with a Graph-Structured Cache](http://arxiv.org/abs/1810.08305).

More specifically, you list the names of any java repos from the [Maven Repository](https://mvnrepository.com/) that you'd like to convert into a dataset, and then this library will automatically download those repos and generate Augmented ASTs of all their constituent files, one .gml file per .java file.

# How do I install it?
You'll need [Apache Maven](https://maven.apache.org/) installed.  (And the basic linux command line utilities.)

Then run
```
cd <root directory of this repo>
mvn install -DskipTests
cd <root directory of this repo>/javaparser-dloc
mvn install -DskipTests
```

# How do I use it?

## 1. Create list of maven repositories
There is a file called `repositories.txt` in `javaparser-dloc/scripts`. You should change this file to contain whatever repo names from the [Maven Repository](https://mvnrepository.com/) that you'd like to process into datapoints.  The format is one repo per line,  each line reading `<org name>:<repo name>:<version number>`.  At the moment, `repositories.txt` contains the names of the 18 Maven repos used in the Deep Learning On Code With A Graph Vocabulary.

Once you've edited `repositories.txt`, run the `createDatasets.sh` script as follows:

```
export dataset=<path to where you'd like the dataset to go>
cat repositories.txt | xargs -I{} <root directory of this repo>/javaparser-dloc/scripts/createDatasets.sh {} $dataset
```

## 2. Process all files in all repositories
Now that you've downloaded and built the repos, process them all into graphml-formatted files:
```
ls $dataset | xargs -I{} <root directory of this repo>/javaparser-dloc/scripts/processDataset.sh {} $dataset
```

# Questions?
Feel free to get in touch with [Milan Cvitkovic](mailto:mwcvitkovic@gmail.com) or any of the other paper authors.  We'd love to hear from you!

# Build library
```
cd ~/<workspace>
mvn install -DskipTests
cd ~/<workspace>/javaparser-dloc
mvn install -DskipTests
```

# Create Repository
```
export dataset=/Users/sbadal/datasets
cat repositories.txt | xargs -I{} ./createDatasets.sh {} $dataset
```

# Process all repository
```
ls $dataset | xargs -I{} ./processDataset.sh {} $dataset
```

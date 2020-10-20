## rdf-neo
Importer RDF2Neo4j

## Run
Change application.properties if needed, and then Run RdfToNeoImporter's main,
e.g. with
```bash
./gradlew run -DmainClass=convert.RdfToNeoImporter
```

The files are assumed to be in Turtle format.<br/>
The script assumes a running Neo4j server on port 7474 (the host is specified via properties).

## Import
RDF properties having literals as objects are imported as node properties, while properties linking IRIs are imported as relations.

While the current implementation preserves ground information, it only partially preserves the ontological schema, for it ignores blank nodes.

Any IRI that is imported as a property key or as a relation is saved in its abbreviated form (curie: <a hef="http://www.w3.org/TR/curie"> http://www.w3.org/TR/curie</a>) by using the namespaces present in the files. The import will break when trying to save a IRI as property key or relation without a prior curie transformation.

The name of the field hosting the IRI of the resource can be specified in the property field.

Types are used (as curies) to assign types ("labels", in Neo4j jargon) to the node. They are repeated as IRIs both as properties and as relations.
package convert.access;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.driver.v1.Session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static convert.NamespacesUtils.getCurie;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
public class NeoImporter {

  private final String iriField;
  private final Set<Namespace> nss;

  public NeoImporter(String iriField, Set<Namespace> nss) {
    this.iriField = iriField;
    this.nss = nss;
  }

  public NeoImporter importNodes(Map<String, Map<String, Set<String>>> objects, Session session) {

    objects.forEach(
        (subject, props) -> {

          Map<String, Object> params = newHashMap();
          params.put("name", subject.substring(subject.indexOf("#") + 1));
          params.put(iriField, subject);

          List<String> types = newArrayList();

          props.forEach(
              (predicate, listOfValues) -> {
                if (predicate.equals(RDF.TYPE.toString())) {
                  types.addAll(listOfValues.stream().map(t -> getCurie(nss, t)).collect(toList()));
                }
                params.put(getCurie(nss, predicate), listOfValues);
              });

          session.run(
              "CREATE (a" + formatTypesAsLabels(types) + formatStringParams(params) + ")",
              params);
        }
    );
    log.info("added nodes");
    return this;
  }


  public NeoImporter importRelations(Collection<Triple> relations, Session session) {
    relations.stream().forEach(rel -> {
      session.run(
          String.format("MATCH (n {%s:\"%s\"}) , (m {%s:\"%s\"}) create (n)-[:%s {%s:\"%s\"}]->(m);",
              iriField,
              rel.getLeft(),
              iriField,
              rel.getRight(),
              getCurie(nss, rel.getMiddle().toString()),
              iriField,
              rel.getMiddle().toString()
          )
      );
    });
    log.info("added relations");
    return this;
  }

  public NeoImporter clearDb(Session session) {
    session.run("MATCH ()-[r]-() DELETE r");
    session.run("MATCH (n) DELETE n");
    return this;
  }


  private String formatTypesAsLabels(List<String> types) {
    return types.stream().map(t -> getCurie(nss, t)).collect(joining(":", ":", "")) + " ";
  }

  private String formatStringParams(Map<String, Object> params) {
    return params.keySet().stream()
        .map(x -> x + ":{" + x + "}")
        .collect(joining(", ", "{", "}"));
  }


}

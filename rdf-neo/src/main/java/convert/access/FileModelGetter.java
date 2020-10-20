package convert.access;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.*;

@Slf4j
public class FileModelGetter {

  @Getter
  private final Model model = new LinkedHashModel();

  public FileModelGetter(DirectoryStream<Path> paths, RDFFormat format) {
    paths.forEach(
        path -> {
          try {
            Model parsedModel = Rio.parse(new FileInputStream(path.toFile()), "", format);
            model.addAll(parsedModel);
            parsedModel.getNamespaces().forEach(ns -> model.setNamespace(ns));
            model.setNamespace("rdf", RDF.NAMESPACE);
          } catch (IOException e) {
            log.warn("Problems in loading file " + path + ". It will be ignored.");
          }
        }
    );
  }

  public Map<String, Map<String, Set<String>>> getObjects() {
    return model
        .filter(null, null, null)
        .stream()
        .filter(st -> st.getSubject() instanceof IRI)
        .filter(st -> st.getObject() instanceof Literal || st.getPredicate().equals(RDF.TYPE))
        .collect(groupingBy(
            st -> st.getSubject().toString(),
            () -> newHashMap(),
            groupingBy(st -> st.getPredicate().toString(), () -> newHashMap(), mapping(st -> st.getObject().stringValue(), toSet())))
        );
  }

  public Collection<Triple> getRelations() {
    return model
        .filter(null, null, null)
        .stream()
        .filter(st -> st.getSubject() instanceof IRI && st.getObject() instanceof IRI)
        .map(st -> ImmutableTriple.of(st.getSubject(), st.getPredicate(), st.getObject()))
        .collect(toSet());
  }


  public Set<Namespace> getNss() {
    return model.getNamespaces();
  }

}

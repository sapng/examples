package convert;

import org.eclipse.rdf4j.model.Namespace;

import java.util.Set;

public class NamespacesUtils {

  public static String getCurie(Set<Namespace> nss, String iri) {
    return nss.stream()
        .filter(n -> iri.startsWith(n.getName()))
        .findFirst()
        .map(n -> n.getPrefix() + "__" + iri.replace(n.getName(), ""))
        .orElse(iri);
  }

}
package uk.ac.rothamsted.rdf.neo4j.idconvert;

import java.util.function.Function;

import uk.ac.ebi.utils.ids.IdUtils;
import uk.ac.rothamsted.rdf.neo4j.load.support.RdfDataManager;

/** 
 * <p>An IRI converter turns a full long IRI/URI into a more readable ID/Label.</p>
 * 
 * <p>They are used by the Neo4J converter, e.g., {@link RdfDataManager#getCyPropertyIdConverter()}, 
 * {@link RdfDataManager#getCyRelationTypeIdConverter()} and technically they are nothing but string/string
 * functions (where the input is an IRI).</p> 
 *   
 * <p>This is the default IRI converter, which is a wrapper of {@link IdUtils#iri2id(String)}, i.e., it returns the 
 * IRI fragment that follows the last '#' or '/' separator.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Dec 2017</dd></dl>
 *
 */
public class DefaultIri2IdConverter implements Function<String, String>
{
	@Override
	public String apply ( String iri )
	{
		return IdUtils.iri2id ( iri );
	}
}

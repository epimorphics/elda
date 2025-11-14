package com.epimorphics.lda.metadata;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.*;

public class MetaConfig {

    static class State {
        final boolean enabled;
        final Resource blockData;

        State(boolean enabled, Resource blockData) {
            this.enabled = enabled;
            this.blockData = blockData;
        }
    }

    protected boolean disableDefaultMetadata = false;

    public Set<Property> enabled = new HashSet<Property>();

    public Map<String, State> blocks = new HashMap<String, State>();

    public MetaConfig(boolean disableDefaultMetadata) {
        this.disableDefaultMetadata = disableDefaultMetadata;
    }

    public MetaConfig() {
        this(false);
    }

    public MetaConfig(Resource root, MetaConfig mc) {
        this.disableDefaultMetadata = mc.disableDefaultMetadata;
        this.enabled = new HashSet<Property>(mc.enabled);
        parseRoot(root);
    }

    public MetaConfig(Resource root) {
        parseRoot(root);
    }

    private void parseRoot(Resource root) {
        enableDefaultMetadata(root);
        disableDefaultMetadata(root);
        loadMetadataBlocks(root);
    }

    private void loadMetadataBlocks(Resource root) {
        List<Statement> mds = root.listProperties(ELDA_API.metadata).toList();
        for (Statement md : mds) loadMetadataBlock(md);
    }

    private void loadMetadataBlock(Statement md) {

        Resource block = md.getResource();
        String taggedName = RDFUtils.getStringValue(block, API.name);

        if (taggedName == null) {
            throw new RuntimeException("metadata block has no api:name.");
        }

        boolean plus = taggedName.startsWith("+");
        boolean minus = taggedName.startsWith("-");
        boolean enabled = plus || !minus;

        String name = plus || minus ? taggedName.substring(1) : taggedName;

        if (blocks.containsKey(name)) {
            throw new RuntimeException("api:name '" + name + "' has been reused.");
        }

        List<Statement> ss = block.listProperties().toList();

        Model target = ModelFactory.createDefaultModel();

        for (Statement s : ss) {
            if (s.getPredicate().equals(API.name)) {
                // discard
            } else {
                target.add(s);
            }
        }

        Resource outBlock = block.inModel(target);
        blocks.put(name, new State(enabled, outBlock));
    }

    private void disableDefaultMetadata(Resource root) {
        List<Statement> dd = root.listProperties(ELDA_API.disable_default_metadata).toList();
        for (Statement d : dd) {
            boolean disable = d.getBoolean();
            disableDefaultMetadata = disable;
        }
    }

    private void enableDefaultMetadata(Resource root) {
        List<Statement> ss = root.listProperties(ELDA_API.enable_default_metadata).toList();
        for (Statement s : ss) {
            List<RDFNode> l = s.getObject().as(RDFList.class).asJavaList();
            for (RDFNode n : l) {
                Property p = n.as(Property.class);
                enabled.add(p);
            }
        }
    }

    public boolean disableDefaultMetadata() {
        return disableDefaultMetadata;
    }

    public boolean drop(Property p) {
        return !keep(p);
    }

    protected boolean keep(Property p) {
        return
                enabled.contains(p)
                        || disableDefaultMetadata == false
                ;
    }

    @Override
    public String toString() {
        return "<mc " + disableDefaultMetadata + " " + enabled + ">" + blocks;
    }

    public void addMetadata(Resource root, Bindings b) {
        addMetadata(root, b, Collections.emptySet());
    }

    public void addMetadata(Resource root, Bindings b, Set<String> options) {
        Model target = root.getModel();

        Map<String, Boolean> decoded = new HashMap<String, Boolean>();

        for (String option : options) {
            if (option.startsWith("+")) {
                decoded.put(option.substring(1), true);
            } else if (option.startsWith("-")) {
                decoded.put(option.substring(1), false);
            } else {
                // ignore
            }
        }

        for (String k : blocks.keySet()) {
            State block = blocks.get(k);
            Boolean qp = decoded.get(k);
            if (block.enabled || qp != false) {
                for (Statement s : block.blockData.listProperties().toList()) {
                    target.add(root, s.getPredicate(), expand(b, s.getObject()));
                }
            }
        }

//		System.err.println(">> metadata setting ------------------");
//		target.write(System.err, "TTL");
//		System.err.println(">> -----------------------------------");
    }

    protected static Model litModel = ModelFactory.createDefaultModel();

    protected static final String RDFS_Resource = RDFS.Resource.getURI();

    private RDFNode expand(Bindings b, RDFNode O) {
        if (O.isResource()) return O;
        String lex = O.asNode().getLiteralLexicalForm();
        String typeURI = O.asNode().getLiteralDatatypeURI();
        String expanded = b.expandVariables(lex);
        if (RDFS_Resource.equals(typeURI)) return ResourceFactory.createResource(expanded);
        return litModel.createTypedLiteral(expanded, typeURI);
    }
}

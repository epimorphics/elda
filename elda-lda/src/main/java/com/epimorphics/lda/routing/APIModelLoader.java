package com.epimorphics.lda.routing;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.support.TDBManager;
import com.hp.hpl.jena.rdf.model.Model;

public class APIModelLoader implements ModelLoader {

    final String baseFilePathLocal;

    public APIModelLoader(String base) {
        baseFilePathLocal = base;
    }

    @Override public Model loadModel(String uri) {
        Loader.log.info( "loadModel: " + uri );
        if (uri.startsWith( Container.LOCAL_PREFIX )) {
            String specFile = "file:///" + baseFilePathLocal + uri.substring(Container.LOCAL_PREFIX.length());
            return EldaFileManager.get().loadModel( specFile );

        } else if (uri.startsWith( TDBManager.PREFIX )) {
            String modelName = uri.substring( TDBManager.PREFIX.length() );
            Model tdb = TDBManager.getTDBModelNamed( modelName );
            Loader.log.info( "get TDB model " + modelName );
            if (tdb.isEmpty()) Loader.log.warn( "the TDB model at " + modelName + " is empty -- has it been initialised?" );
            if (tdb.isEmpty()) throw new APIException( "the TDB model at " + modelName + " is empty -- has it been initialised?" );
            return tdb;

        } else {
            return EldaFileManager.get().loadModel( uri );
        }
    }
}
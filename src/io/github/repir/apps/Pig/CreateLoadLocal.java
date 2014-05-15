package io.github.repir.apps.Pig;

import io.github.repir.Repository.Pig.PigDoc;
import io.github.repir.Repository.Pig.PigTerm;
import io.github.repir.Repository.Pig.PigTermDoc;
import io.github.repir.Repository.Pig.PigTermDocPos;
import io.github.repir.Repository.Repository;
import static io.github.repir.apps.Pig.CreateTermDoc.getKeywords;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSDir;
import io.github.repir.tools.Lib.Log;
import java.util.HashSet;

/**
 * Retrieve all topics from the TestSet, and store in an output file. arguments:
 * <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class CreateLoadLocal {

   public static Log log = new Log(CreateLoadLocal.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      FSDir dir = new FSDir(repository.configuredString("rr.localdir") + "pig/" + repository.getPrefix());
      PigTerm term = (PigTerm)repository.getFeature(PigTerm.class);
      Datafile scriptfile = dir.getFile("terms");
      scriptfile.printf("terms = %s", term.loadLocalScript());
      scriptfile.closeWrite();
      PigDoc doc = (PigDoc)repository.getFeature(PigDoc.class);
      scriptfile = dir.getFile("docs");
      scriptfile.printf("docs = %s", doc.loadLocalScript());
      scriptfile.closeWrite();

      HashSet<String> keywords = getKeywords(repository);
      for (String w : keywords) {
         PigTermDoc termdoc = (PigTermDoc) repository.getFeature(PigTermDoc.class, w);
            scriptfile = dir.getFile("postings_" + w);
            scriptfile.printf("postings_%s = %s", w, termdoc.loadLocalScript());
            scriptfile.closeWrite();
         PigTermDocPos termdocpos = (PigTermDocPos) repository.getFeature(PigTermDocPos.class, w);
            scriptfile = dir.getFile("pospostings_" + w);
            scriptfile.printf("pospostings_%s = %s", w, termdocpos.loadLocalScript());
            scriptfile.closeWrite();
      }
   }
}

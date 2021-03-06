package io.github.repir.apps.Eval;

import io.github.repir.Repository.CollectionID;
import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermString;
import io.github.repir.Retriever.Document;
import io.github.htools.lib.Log;

/**
 * Show the contents of a document based on its CollectionID parameters:
 * <configfile> <collectionID>
 */
public class ShowDocumentByCollectionID {

   public static Log log = new Log(ShowDocumentByCollectionID.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "documentid partitionnr");
      int partition = repository.configuredInt("partitionnr");
      TermString termstring = TermString.get(repository);
      termstring.loadMem(100000);
      CollectionID collectionid = repository.getCollectionIDFeature();
      collectionid.setPartition(partition);
      int docid = collectionid.findLiteral( repository.configuredString("collectionid") );
      if (docid >= 0) {
         Document doc = new Document(docid, partition);
         DocForward fw = DocForward.get(repository, "all");
         fw.read(doc);
         int[] value = fw.getValue();
         DocTF doctf = DocTF.get(repository, "all");
         DocLiteral title = DocLiteral.get(repository, "literaltitle");
         doctf.read(doc);
         title.read(doc);
         log.printf("doctf[0]=%d", doctf.getValue());
         log.printf("doc %5d#%3d id %s title %s", docid, partition,
                 args[2],
                 title.getValue());
         log.printf("%s", termstring.getContentStr(value));
      }
   }
}

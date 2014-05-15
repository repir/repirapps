package io.github.repir.apps.Eval;

import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.TermCF;
import io.github.repir.tools.Lib.Log;

/**
 * Shows the top-10000 terms in the vocabulary
 */
public class ShowTerms {

   public static Log log = new Log(ShowTerms.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args[0]);
      Retriever retriever = new Retriever(repository);
      TermString termstring = (TermString) repository.getFeature(TermString.class);
      TermDF df = (TermDF) repository.getFeature(TermDF.class);
      TermCF cf = (TermCF) repository.getFeature(TermCF.class);
         for (int termid = 0; termid < repository.getVocabularySize(); termid++) {
          String s = termstring.readValue(termid);
          log.printf("%s df %d cf %d", s, df.readValue(termid), cf.readValue(termid));            
         }
   }
}
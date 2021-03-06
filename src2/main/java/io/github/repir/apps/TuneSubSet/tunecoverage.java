package io.github.repir.apps.TuneSubSet;

import io.github.htools.hadoop.Conf;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.ResultFileRR;
import io.github.repir.TestSet.TestSet;
import io.github.htools.search.ByteRegex;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.htools.lib.Log;
import java.util.Collection;
import java.util.HashSet;
import org.apache.hadoop.fs.FileSystem;

/**
 * Counts the number of Documents returned on the full set that are respectively in
 * and out of the smaller set used for tuning.
 * @author Jeroen Vuurens
 */
public class tunecoverage {

   public static Log log = new Log(tunecoverage.class);

   public static void main(String[] args) {
      String reps[] = args[1].split(",");
      String sys[] = args[2].split(",");
      ByteRegex cluewebidpattern = new ByteRegex("clueweb\\S*?(?=\\s)");
      HashSet<String> ids = new HashSet<String>();
      Conf conf = new Conf();
      Datafile subset = new Datafile(conf, args[0]);
      subset.openRead();
      subset.setBufferSize(100000000);
      try {
         while (subset.hasMore()) {
            String readString = subset.readString(cluewebidpattern);
            ids.add(readString);
         }
      } catch (EOCException ex) { }
      subset.close();
      log.info("count %d", ids.size());
      log.info("present %b", ids.contains("clueweb09-en0072-93-17757"));
      int countin = 0;
      int countout = 0;
      for (String r : reps) {
         Repository repository = new Repository(r);
         TestSet ts = new TestSet(repository);
         for (String file : sys) {
            log.info("results %s%s", r, file);
            Collection<Query> results = new ResultFileRR(ts, file).getResults();
            for (Query q : results) {
              for (Document d : q.getQueryResults()) {
                 if (ids.contains(d.getCollectionID()))
                    countin++;
                 else
                    countout++;
              }
            }
         }
      }
      log.info("in %d out %d", countin, countout);
   }
}

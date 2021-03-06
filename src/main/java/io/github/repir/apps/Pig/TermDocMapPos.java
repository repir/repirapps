package io.github.repir.apps.Pig;

import io.github.repir.Repository.Pig.PigDoc;
import io.github.repir.Repository.Pig.PigTermDocPos.Tuple;
import io.github.repir.Repository.Pig.PigTermDocPos;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermInverted;
import io.github.repir.Retriever.Document;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class TermDocMapPos extends Mapper<IntWritable, Text, NullWritable, NullWritable> {

   public static Log log = new Log(TermDocMapPos.class);
   RRConfiguration conf;
   Repository repository;
   int width = 10;
   ArrayList<Tuple> tuples;

   @Override
   protected void setup(Mapper.Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      conf = repository.getConf();
   }

   @Override
   public void map(IntWritable inkey, Text invalue, Context context) throws IOException, InterruptedException {
      log.info("term %s", invalue.toString());
      Term term = repository.getProcessedTerm(invalue.toString());
      TermInverted terminverted = TermInverted.get(repository, "all", term);
      if (term.exists()) {
         tuples = new ArrayList<Tuple>();
         terminverted.setPartition(inkey.get());
         terminverted.getFile().setBufferSize(1000000);
         terminverted.openRead();
         Document doc = new Document();
         doc.partition = inkey.get();
         while (terminverted.next()) {
            doc.docid = terminverted.docid;
            Tuple t = new PigTermDocPos.Tuple();
            t.docid = doc.docid;
            t.partition = doc.partition;
            t.term = term.getProcessedTerm();
            t.position = terminverted.getValue(doc);
            tuples.add(t);
            if (tuples.size() > 500000)
               flush(term);
         }
         flush(term);
      }
   }
   
   public void flush(Term term) {
         PigTermDocPos termdoc = PigTermDocPos.get(repository, term.getProcessedTerm());
         termdoc.openAppend();
         for (Tuple t : tuples) {
            termdoc.write(t);
            //log.info("write %s %d %d", term.getProcessedTerm(), t.docid, t.tf);
         }
         termdoc.closeWrite();
         tuples = new ArrayList<Tuple>();
   }
}

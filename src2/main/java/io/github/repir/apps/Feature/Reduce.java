package io.github.repir.apps.Feature;

import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.ReduciblePartitionedFeature;
import io.github.repir.Repository.Repository;
import io.github.htools.lib.Log;
import io.github.htools.hadoop.Job;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class Reduce extends Reducer<RecordKey, RecordValue, RecordKey, IntWritable> {

   public static Log log = new Log(Reduce.class);
   Repository repository;
   int partition;
   int MAXMEMORY = 100000000;
   HashMap<String, Integer> doclist = new HashMap<String, Integer>();
   ArrayList<EntityStoredFeature> documentfeatures = new ArrayList<EntityStoredFeature>();
   ArrayList<AutoTermDocumentFeature> termdocfeatures = new ArrayList<AutoTermDocumentFeature>();

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      partition = Job.getReducerId(context);
      
      for (String featurename : repository.configuredStrings("repository.constructfeatures")) {
         Feature f = repository.getFeature(featurename);
         if (f instanceof ReduciblePartitionedFeature) {
            if (f instanceof EntityStoredFeature) {
               documentfeatures.add((EntityStoredFeature) f);
            } else if (f instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) f);
               if (doclist == null)
                  doclist = repository.getCollectionIDFeature().getCollectionIDs(partition);
            }
         }
      }
      int mempart = MAXMEMORY / (4096 * (termdocfeatures.size() * 2 + documentfeatures.size()));
      for (EntityStoredFeature dc : documentfeatures) {
         dc.startReduce(partition, 4096 * mempart);
      }
      for (AutoTermDocumentFeature tc : termdocfeatures) {
         tc.setDocs(doclist);
         tc.startReduce(partition, 4096 * 2 * mempart);
      }
   }

   @Override
   public void reduce(RecordKey key, Iterable<RecordValue> values, Context context)
           throws IOException, InterruptedException {
      Job.reduceReport(context);
      if (key.getType() != RecordKey.Type.TERMDOCFEATURE) {
         documentfeatures.get(key.feature).writeReduce(key, values);
      } else if (key.getType() == RecordKey.Type.TERMDOCFEATURE) {
         key.docid = doclist.get(key.collectionid); // convert collectionid to docid
         termdocfeatures.get(key.feature).reduceInput(key, values);
      }
      context.progress();
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      for (EntityStoredFeature dc : documentfeatures) {
         dc.finishReduce();
      }
      for (AutoTermDocumentFeature tc : termdocfeatures) {
         tc.finishReduce();
      }
   }
}

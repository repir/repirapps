package io.github.repir.apps.Pig;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.MapReduce.Job;
import io.github.repir.tools.MapReduce.NullInputFormat;
import io.github.repir.tools.MapReduce.StringInputFormat;
import java.io.IOException;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

/**
 * Extension of Hadoop Job, used by JobManager to start multi-threaded 
 * rerieval.
 * @author jer
 */
public class NullPigJob extends Job {

   public static Log log = new Log(NullPigJob.class);
   public NullInputFormat inputformat;

   public NullPigJob(Repository repository) throws IOException {
      super(repository, "Context collector " + repository.configuredString("rr.conf"));
      inputformat = new NullInputFormat(repository);
      inputformat.add(null);
      setMapOutputKeyClass(NullWritable.class);
      setMapOutputValueClass(NullWritable.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(TermDocMap.class);
      setInputFormatClass(inputformat.getClass());
      setOutputFormatClass(NullOutputFormat.class);
      this.setNumReduceTasks(0);
   }
}
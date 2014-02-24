package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFile;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSet {

   public static Log log = new Log(RunTestSet.class);

   public static void main(String[] args) throws Exception {
      String property = System.getProperty("sun.arch.data.model");
      log.info("jvm %s", property);
      Configuration conf = HDTools.readConfig(args, "resultsfileext");
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      TestSet testset = new TestSet( repository );
      retriever.addQueue(testset.getQueries(retriever));
      RetrieverMRInputFormat.setSplitable(true);
      RetrieverMRInputFormat.setIndex(repository);
      ResultFile out = new ResultFile(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.getConfigurationString("resultsfileext")));
      log.info("outfile %s", out.getDatafile().getFullPath());
      ArrayList<Query> results = retriever.retrieveQueue();
      out.writeresults(results);
   }
}

package io.github.repir.apps.Eval;

import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricPrecision;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * Show Precision@10 for set of results files for same test set
 * arguments <configfile> { results_file_ext }
 * @author jeroen
 */
public class ShowP10 {

   public static Log log = new Log(ShowP10.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {resultsext}");
      TestSet testset = new TestSet(new Repository(parsedargs.get("configfile")));
      ResultSets resultset = new ResultSets( new QueryMetricPrecision( 20 ), testset, parsedargs.getRepeatedGroup("resultsext"));
      log.printf("baseline %f", resultset.get(0).getMean());
      for (int i = 1; i < args.length; i++) {
         log.printf("%s %f gain %f%% sig %f", args[i], resultset.get(i).getMean(), 
                 100 * (resultset.get(i).getMean() - resultset.get(0).getMean())/resultset.get(0).getMean(),
                 resultset.sigOver(0, i));
      }
   }
}
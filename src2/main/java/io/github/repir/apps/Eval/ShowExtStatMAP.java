package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.Metric.QueryMetricStatAP;
import io.github.repir.TestSet.ResultFileTREC;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.htools.io.Datafile;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * shows MAP of a resultsfile
 * arguments <configfile> <result_file_extension>
 * @author jeroen
 */
public class ShowExtStatMAP {

   public static Log log = new Log(ShowExtStatMAP.class);

   public static void main(String args[]) throws IOException {
      ArgsParser parsedargs = new ArgsParser(args, "configfile trecresultsfile");
      Repository repository = new Repository(parsedargs.get("configfile"));
      TestSet testset = new TestSet(repository);
      ResultFileTREC resultsfile = new ResultFileTREC(testset, new Datafile(parsedargs.get("trecresultsfile")));
      ResultSet resultset = new ResultSet( new QueryMetricStatAP(), testset, resultsfile.getResults());
      log.printf("%f", resultset.getMean());
   }
}

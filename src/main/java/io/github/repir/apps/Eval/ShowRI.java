package io.github.repir.apps.Eval;

import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * calculates MAP and significance for given results
 * arguments: <configfile> <extension_baselinefile> { extension_resultsfile }
 * @author jeroen
 */
public class ShowRI {

   public static Log log = new Log(ShowRI.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile baselineext {resultsext}");
      TestSet testset = new TestSet(new Repository(parsedargs.get("configfile")));
      testset.getQrels();
      testset.purgeTopics();
      log.info("valid topics %d", testset.topics.size());
      String systems[] = parsedargs.getRepeatedGroup();
      ResultSet resultset = new ResultSet( new QueryMetricAP(), testset, systems);
      resultset.calulateMeasure();
      double ri = resultset.calculateRI(1);
      log.printf("baseline %f", resultset.result[0].avg);
      for (int i = 1; i < systems.length; i++) {
         log.printf("%s %f gain %f%% RI %f queries %d", systems[i], resultset.result[i].avg, 
                 100 * (resultset.result[i].avg - resultset.result[0].avg)/resultset.result[0].avg,
                 ri, resultset.result[i].possiblequeries);
      }
   }
}

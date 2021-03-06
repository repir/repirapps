package io.github.repir.apps.Eval;

import io.github.repir.TestSet.ResultSets;
import io.github.jexperiments.excel.ExcelDoc;
import io.github.jexperiments.excel.ExcelSheet;
import io.github.repir.Repository.Repository;
import io.github.htools.lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.TestSet;
import java.util.TreeMap;
import io.github.jexperiments.excel.ExcelCell;
import io.github.htools.lib.ArgsParser;
import java.io.IOException;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class RIToExcel {

   public static Log log = new Log(RIToExcel.class);
   int cps = 4;
   ExcelDoc workbook = new ExcelDoc("sig.xlsx");
   public String collections[];
   public String systems[];
   public ResultSets sets[];

   public RIToExcel(String filename, String collections[], String systems[]) throws IOException {
      workbook = new ExcelDoc(filename);
      this.collections = collections;
      this.systems = systems;
      sets = new ResultSets[collections.length];
      double ri[][] = new double[collections.length][systems.length];
      for (int coll = 0; coll < collections.length; coll++) {
         Repository repository = new Repository(collections[coll]);
         sets[coll] = new ResultSets(new QueryMetricAP(), new TestSet(repository), systems);
         for (int s = 1; s < systems.length; s++) 
            ri[coll][s] = sets[coll].riOver(0, s);
      }

      ExcelSheet mapsheet = createSheetTable("map");

      TreeMap<Integer, String> output = new TreeMap<Integer, String>();
      for (int coll = 0; coll < collections.length; coll++) {
         for (int sys = 1; sys < systems.length; sys++) {
            ExcelCell sysavg = mapsheet.createCell(coll + 1, sys);
            sysavg.setFormatDouble(ri[coll][sys]);
         }
      }

      workbook.write();
   }

   public static void main(String[] args) throws IOException {
      ArgsParser ar = new ArgsParser( args , "collections systems");
      String collections[] = ar.get("collections").split(",");
      String systems[] = ar.get("systems").split(",");
      new RIToExcel("ri.xlsx", collections, systems);
   }

   public ExcelSheet createSheetTable(String sheetname) {
      ExcelSheet mapsheet = workbook.getSheet(sheetname);
      mapsheet.createCell(0, 0).set("testset");
      for (int s = 1; s < systems.length; s++) {
         mapsheet.createCell(0, s).set( systems[s] );
      }
      for (int c = 0; c < collections.length; c++) {
         mapsheet.createCell(c + 1, 0).set( collections[c] );
      }
      return mapsheet;
   }
}

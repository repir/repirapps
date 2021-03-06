package util;

import io.github.repir.Repository.Repository;
import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import io.github.repir.MapReduceTools.NullInputFormat;
import io.github.htools.io.struct.StructuredDataStream;
import io.github.htools.io.struct.StructuredFileSort;
import io.github.htools.io.struct.StructuredFileSortRecord;
import io.github.htools.hadoop.Job;
import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import util.TestSortMap.SortFile.Record;

/**
 *
 * @author jeroen
 */
public class TestSortMap extends Mapper<IntWritable, NullWritable, NullWritable, NullWritable> {
    public static Log log = new Log(TestSortMap.class);
    
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
      Repository repository = new Repository(args, "");
      
      Job job = new Job(repository.getConf(), repository.configurationName());
      job.setNumReduceTasks(0);
      job.setMapOutputKeyClass(NullWritable.class);
      job.setMapOutputValueClass(NullWritable.class);
      job.setMapperClass(TestSortMap.class);
      job.setOutputFormatClass(NullOutputFormat.class);
      
      // set input to <null, 0>, to execute mapper once
      NullInputFormat inputformat = new NullInputFormat(repository);
      inputformat.addSingle(0); 
      job.setInputFormatClass(inputformat.getClass());
      
      job.waitForCompletion(true);
    }
    
   @Override
   public void map(IntWritable inkey, NullWritable invalue, Context context) throws IOException, InterruptedException {
        SortFile f = new SortFile(new Datafile(FileSystem.get(context.getConfiguration()), "sortfile"));
        f.openWrite();
        Record r = new Record(f);
        for (long i = 0; i < 20000000; i++) {
            r.id = i;
            r.write();
        }
        f.closeWrite();
        int count = 0;
        f.openRead();
        while (f.nextRecord()) {
            count++;
        }
        f.closeRead();
        log.info("count %d", count);
    }

    public static class SortFile extends StructuredFileSort {

        public StructuredDataStream.LongField id = this.addLong("id");

        public SortFile(Datafile df) {
            super(df);
        }

        @Override
        public StructuredFileSortRecord createRecord() {
            Record record = new Record(this);
            record.id = id.value;
            return record;
        }

        @Override
        public int compare(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
            long comp = ((Record) o2).id - ((Record) o1).id;
            return (comp < 0) ? -1 : 1;
        }

        @Override
        public int compare(StructuredFileSort o1, StructuredFileSort o2) {
            long comp = ((SortFile) o2).id.value - ((SortFile) o1).id.value;
            return (comp < 0) ? -1 : 1;
        }

        @Override
        protected int spillThreshold() {
            return 1000000;
        }

        static class Record extends StructuredFileSortRecord {

            long id;

            public Record(SortFile t) {
                super(t);
            }

            @Override
            public void write() {
                writeFinal();
            }

            @Override
            public void writeFinal() {
                ((SortFile) file).id.write(id);
            }
        }
    }
}

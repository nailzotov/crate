import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.google.common.collect.Iterators;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.UnmodifiableIterator;
import org.cratedb.core.collections.LimitingCollectionIterator;
import org.cratedb.core.collections.SortedPriorityQueueIterator;
import org.junit.Test;

import java.util.*;

public class QueueMergePerfTest extends AbstractBenchmark {

    @Test
    public void testPerformanceMinMaxPriorityQueueMergeSmallLimit() {

        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);

        for (int run = 0; run < 5; run ++) {
            MinMaxPriorityQueue<Integer> q1 = MinMaxPriorityQueue.maximumSize(10).create();
            MinMaxPriorityQueue<Integer> q2 = MinMaxPriorityQueue.create();

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            q1.addAll(q2);
            List<Integer> test = new ArrayList<>(q1.size());
            Integer i;
            while ((i = q1.poll()) != null) {
                test.add(i);
            }
            measurements.add(new Date().getTime() - now);
        }

    }

    @Test
    public void testPerformanceMinMaxPriorityQueueMerge() {

        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);

        for (int run = 0; run < 5; run ++) {
            MinMaxPriorityQueue<Integer> q1 = MinMaxPriorityQueue.create();
            MinMaxPriorityQueue<Integer> q2 = MinMaxPriorityQueue.create();

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            q1.addAll(q2);
            List<Integer> test = new ArrayList<>(q1.size());
            Integer i;
            while ((i = q1.poll()) != null) {
                test.add(i);
            }
            measurements.add(new Date().getTime() - now);
        }

    }

    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    @Test
    public void testPerformanceArrayList() {

        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);

        for (int run = 0; run < 5; run ++) {
            List<Integer> q1 = new ArrayList<>(1000000);
            List<Integer> q2 = new ArrayList<>(1000000);

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            q1.addAll(q2);
            Collections.sort(q1);
            measurements.add(new Date().getTime() - now);
        }

    }

    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    @Test
    public void testPerformanceArrayListSmallLimit() {

        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);

        for (int run = 0; run < 5; run ++) {
            List<Integer> q1 = new ArrayList<>(1000000);
            List<Integer> q2 = new ArrayList<>(1000000);

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            q1.addAll(q2);
            Collections.sort(q1);
            LimitingCollectionIterator<Integer> iterator = new LimitingCollectionIterator<>(q1, 10);
            List<Integer> ti = new ArrayList<>(10);
            for (Integer i : iterator) {
                ti.add(i);
            }
            measurements.add(new Date().getTime() - now);
        }

    }

    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    @Test
    public void testPerformancePriorityQueueMerge() {

        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);

        for (int run = 0; run < 5; run ++) {
            PriorityQueue<Integer> q1 = new PriorityQueue<>();
            PriorityQueue<Integer> q2 = new PriorityQueue<>();

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            q1.addAll(q2);
            Integer i;
            List<Integer> test = new ArrayList<>(q1.size());
            while ((i = q1.poll()) != null) {
                test.add(i);
            }
            measurements.add(new Date().getTime() - now);
        }
    }

    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    @Test
    public void testPerformancePriorityQueueMergeSmallLimit() {

        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);

        for (int run = 0; run < 5; run ++) {
            PriorityQueue<Integer> q1 = new PriorityQueue<>();
            PriorityQueue<Integer> q2 = new PriorityQueue<>();

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            q1.addAll(q2);
            Integer i;
            SortedPriorityQueueIterator<Integer> qIterator =
                new SortedPriorityQueueIterator<Integer>(q1, 10);
            List<Integer> test = new ArrayList<>(q1.size());

            for (Integer integer : qIterator) {
                test.add(integer);
            }

            measurements.add(new Date().getTime() - now);
        }
    }

    @Test
    public void testPerformanceNewMerge() {
        long now = 0;
        List<Long> measurements = new ArrayList<>(1000);
        List<Iterator<Integer>> iterators = new ArrayList<>();

        for (int run = 0; run < 5; run ++) {
            iterators.clear();
            PriorityQueue<Integer> q1 = new PriorityQueue<>();
            PriorityQueue<Integer> q2 = new PriorityQueue<>();

            for (int i = 0; i < 1000000; i++) {
                q1.add(i);
            }
            for (int i = 1000001; i < 2000000; i++) {
                q2.add(i);
            }

            now = new Date().getTime();
            iterators.add(new SortedPriorityQueueIterator<Integer>(q1, q1.size()));
            iterators.add(new SortedPriorityQueueIterator<Integer>(q2, q2.size()));
            UnmodifiableIterator<Integer> iterator = Iterators.mergeSorted(iterators, new Comparator<Integer>() {
                @Override
                public int compare(Integer integer, Integer integer2) {
                    return integer.compareTo(integer2);
                }
            });
            List<Integer> test = new ArrayList<>(q1.size() + q2.size());
            while (iterator.hasNext()) {
                test.add(iterator.next());
            }
            measurements.add(new Date().getTime() - now);
        }


    }

    private Double calculateAverage(List<Long> measurements) {
        double sum = 0;
        for (Long measurement : measurements) {
            sum += measurement;
        }

        return sum / measurements.size();
    }
}

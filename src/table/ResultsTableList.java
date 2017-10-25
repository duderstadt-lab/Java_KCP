package table;

import ij.measure.ResultsTable;

import java.util.AbstractList;

public class ResultsTableList extends AbstractList<double[]> {

	private ResultsTable table;
	
	public ResultsTableList(ResultsTable table) {
		this.table = table;
	}
	
	@Override
	public double[] get(int row) {

		double[] values = new double[table.getLastColumn() + 1];
		
		for (int col = 0; col < values.length; col++)
			values[col] = table.getValueAsDouble(col, row);
		
		return values;
	}
	
	@Override
	public double[] set(int row, double[] values) {

		double[] old = get(row);
		
		for (int col = 0; col < values.length; col++)
			table.setValue(col, row, values[col]);
		
		return old;
	}

	@Override
	public int size() {
		return table.getCounter();
	}
	
	@Override
	public void removeRange(int fromIndex, int toIndex) {
		
		int n = toIndex - fromIndex;
		int m = size();
		
		// move range to the end of the table
		for (int row = fromIndex; row + n < m; row++)
			set(row, get(row + n));
		
		// delete last rows
		for (int row = m - 1; row >= m - n; row--)
			table.deleteRow(row);
		
	}
	
	public static void main(String[] args) {
		
		ResultsTable table = new ResultsTable();
		
		int n = 3000000;
		int rangeFrom = 100000;
		int rangeTo = 500000;
		
		System.out.printf("fill table with %d rows...\n", n);
		
		for (int i = 0; i < n; i++) {
			table.incrementCounter();
			table.addValue("a", i);
			table.addValue("b", Math.random());
		}
		
		
		ResultsTableList rtl = new ResultsTableList(table);
		
		System.out.println("sorting...");
		
		long start = System.currentTimeMillis();
		ResultsTableSorter.sort(table, true, "b");
		System.out.printf("elapsed time %dms\n", System.currentTimeMillis() - start);
		
		System.out.println("testing...");
		
		double[] prev = rtl.get(0);
		
		for (int i = 1; i < n; i++) {
			double[] curr = rtl.get(i);
			
			if (curr[1] < prev[1]) {
				System.out.println("not sorted properly");
				break;
			}
			
			prev = curr;
		}
		
		System.out.println("build new table...");
		
		table.reset();
		
		for (int i = 0; i < n; i++) {
			table.incrementCounter();
			table.addValue("a", i);
		}
		
		System.out.printf("remove range %d - %d...\n", rangeFrom, rangeTo);
		rtl.removeRange(rangeFrom, rangeTo);
		
		start = System.currentTimeMillis();
		
		for (int i = 0; i < rtl.size(); i++) {
			
			double[] values = rtl.get(i);
			
			if (values[0] >= rangeFrom && values[0] < rangeTo) {
				System.out.println("range not deleted");
				break;
			}
			
		}
		
		System.out.printf("elapsed time %dms\n", System.currentTimeMillis() - start);
		System.out.println("done");
	}

}

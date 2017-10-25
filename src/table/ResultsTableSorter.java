package table;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import ij.IJ;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;

public class ResultsTableSorter implements PlugIn, DialogListener {
	
	private String[] tableTitles;
	private String tableName;
	private ResultsTable table;
	
	String column, group;
	boolean ascending;
	
	public ResultsTableSorter() {
		table = Analyzer.getResultsTable();
	}
	
	public ResultsTableSorter(ResultsTable table) {
		this.table = table;
	}
	
	@Override
	public void run(String arg0) {
		tableTitles = ResultsTableUtil.getResultsTableTitles();
		
		if (tableTitles.length == 0) {
			IJ.showMessage("No results tables!");
			return;
		}
			
		tableName = tableTitles[0];
		
		String[] headings = ResultsTableUtil.getResultsTable(tableTitles[0]).getColumnHeadings().split(",|\\t+");
		String[] headings2 = new String[headings.length + 1];
		
		headings2[0] = "no grouping";
		
		for (int i = 0; i < headings.length; i++)
			headings2[i + 1] = headings[i];
		
		GenericDialog dialog = new GenericDialog("Results Table Sorter");
		dialog.addChoice("Table", tableTitles, "Results");
		dialog.addChoice("column", headings, headings[0]);
		dialog.addChoice("group", headings2, headings2[0]);
		dialog.addCheckbox("ascending", true);
		
		dialog.addDialogListener(this);
		dialog.showDialog();
		
		if (dialog.wasCanceled())
			return;
		
		tableName = dialog.getNextChoice();
		column = dialog.getNextChoice();
		group = dialog.getNextChoice();
		ascending = dialog.getNextBoolean();
		
		table = ResultsTableUtil.getResultsTable(tableName);
		
		sort(table, ascending, group, column);
		
		ResultsTableUtil.show(table, tableName);
	}
	
public static void sort(ResultsTable table, final boolean ascending, String... columns) {
		
		ResultsTableList list = new ResultsTableList(table);
		
		final int[] columnIndexes = new int[columns.length];
		
		for (int i = 0; i < columns.length; i++)
			columnIndexes[i] = table.getColumnIndex(columns[i]);
		
		Collections.sort(list, new Comparator<double[]>() {
			
			@Override
			public int compare(double[] o1, double[] o2) {
				
				for (int columnIndex: columnIndexes) {
					
					if (columnIndex != ResultsTable.COLUMN_NOT_FOUND) {
						
						int groupDifference = Double.compare(o1[columnIndex], o2[columnIndex]); 
					
						if (groupDifference != 0)
							return ascending ? groupDifference : -groupDifference;
						
					}
					
				}
				
				return 0;
			}
			
		});
		
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog dialog, AWTEvent arg1) {
		String newTable = dialog.getNextChoice();
		if (newTable != tableName) {
			tableName = newTable;
			String[] columns = ResultsTableUtil.getResultsTable(tableName).getColumnHeadings().split(",|\\t+");	
			@SuppressWarnings("rawtypes")
			Vector choice_boxes = dialog.getChoices();
			((Choice)choice_boxes.get(1)).removeAll();
			((Choice)choice_boxes.get(2)).removeAll();
			((Choice)choice_boxes.get(2)).add("no grouping");
			for (int i = 0; i < columns.length ; i++) {
				((Choice)choice_boxes.get(1)).add(columns[i]);
				((Choice)choice_boxes.get(2)).add(columns[i]);
			}
	    }
		column = dialog.getNextChoice();
		group = dialog.getNextChoice();
		ascending = dialog.getNextBoolean();
		
		return true;
	}

}

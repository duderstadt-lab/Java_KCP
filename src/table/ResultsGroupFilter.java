package table;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.util.Vector;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

import ij.gui.DialogListener;

public class ResultsGroupFilter implements PlugIn, DialogListener {

	private String[] tableTitles;
	private String tableName;
	
	@Override
	public void run(String arg0) {
		tableTitles = ResultsTableUtil.getResultsTableTitles();
		
		if (tableTitles.length == 0) {
			IJ.showMessage("No results tables!");
			return;
		}
			
		tableName = tableTitles[0];
		
		String[] columns = ResultsTableUtil.getResultsTable(tableName).getColumnHeadings().split(",|\\t+");
		int min = 0;
		int max = 100;
		
		GenericDialog dialog = new GenericDialog("filter on group length");
		dialog.addChoice("Table", tableTitles, tableTitles[0]);
		dialog.addChoice("group_column", columns, columns[0]);
		dialog.addNumericField("min_length", min, 2);
		dialog.addNumericField("max_length", max, 2);
		dialog.showDialog();
		
		if (dialog.wasCanceled())
			return;
		
		tableName = dialog.getNextChoice();
		ResultsTable table = ResultsTableUtil.getResultsTable(tableName);
		String group = dialog.getNextChoice();
		min = (int)dialog.getNextNumber();
		max = (int)dialog.getNextNumber();
		
		if (table.getColumnIndex(group) == ResultsTable.COLUMN_NOT_FOUND) {
			IJ.showMessage("column does not exist");
			return;
		}
		
		// sort on group column
		ResultsTableSorter.sort(table, true, group);
		
		ResultsTableList rtl = new ResultsTableList(table);
		
		int to = table.getCounter();
		double currentGroup = table.getValue(group, to - 1);
		int count = 1;
		
		for (int row = table.getCounter() - 2; row >= 0; row--) {
			
			if (table.getValue(group, row) == currentGroup) {
				count++;
			}
			else {
				if (count < min || count > max)
					rtl.removeRange(row + 1, to);
				
				to = row + 1;
				currentGroup = table.getValue(group, row);
				count = 1;
			}
			
		}
		
		if (count < min || count > max)
			rtl.removeRange(0, to);
		
		ResultsTableUtil.show(table, tableName);
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog dialog, AWTEvent e) {
		String newTable = dialog.getNextChoice();
		if (newTable != tableName) {
			tableName = newTable;
			String[] columns = ResultsTableUtil.getResultsTable(tableName).getColumnHeadings().split(",|\\t+");	
			@SuppressWarnings("rawtypes")
			Vector choice_boxes = dialog.getChoices();
			((Choice)choice_boxes.get(1)).removeAll();
			for (int i = 0; i < columns.length ; i++) {
				((Choice)choice_boxes.get(1)).add(columns[i]);
			}
	    }
		return true;
	}
}

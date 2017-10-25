package table;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.Vector;

import ij.IJ;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;

import util.*;

public class ResultsTableFilter implements PlugIn, DialogListener {
	
	private String[] tableTitles;
	private String tableName;
	private ResultsTable table, filterTable;
	
	private String[] columns;
	private String column;
	private int columnIndex = 0;
	private double min = 0;
	private double max = 1;
	private double N_STD = 2;
	
	private boolean TableFilter = false;
	private boolean STDFilter = false;
	private boolean includeSelection = true;
	
	public ResultsTableFilter() {
		table = Analyzer.getResultsTable();
	}
	
	public ResultsTableFilter(ResultsTable table) {
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
		
		columns = ResultsTableUtil.getResultsTable(tableTitles[0]).getColumnHeadings().split(",|\\t+");
		columnIndex = table.getColumnIndex(columns[0]);
		
		GenericDialog dialog = new GenericDialog("results filter");
		dialog.addChoice("Table", tableTitles,tableTitles[0]);
		dialog.addChoice("column", columns, columns[0]);
		
		dialog.addRadioButtonGroup("Filter Type",new String[] {"Min/Max","Standard Deviation","Table"}, 3, 1, "Min/Max");
		dialog.addRadioButtonGroup("Filter Selection",new String[] {"include","exclude"}, 2, 1, "include");
		
		dialog.addNumericField("min", min, 2);
		dialog.addNumericField("max", max, 2);
		
		dialog.addNumericField("Mean +/- (N * STD)", N_STD, 0);
		
		dialog.addChoice("Filter Table", tableTitles, tableTitles[0]);

		//Turn off STDFilter and Table options
		dialog.getComponent(13).setEnabled(false);
		dialog.getComponent(15).setEnabled(false);
		
		dialog.addDialogListener(this);
		dialog.showDialog();
		
		if (dialog.wasCanceled())
			return;
		
		double[] filterList = new double[0];
		if (TableFilter) {
			filterList = filterTable.getColumnAsDoubles(filterTable.getColumnIndex(column));
		}
		
		//If STDFilter was selected we have to calculate mean and STD before filtering
		double STD = 0;
		double mean = 0;
		if (STDFilter) {
			for (int i = 0; i < table.getCounter() ; i++) {
				mean += table.getValue(column, i);
			}
			mean /= table.getCounter();
			
			double diffSquares = 0;
			for (int i = 0; i < table.getCounter() ; i++) {
				diffSquares += (mean - table.getValue(column, i))*(mean - table.getValue(column, i));
			}
			
			STD = Math.sqrt(diffSquares/(table.getCounter()-1));		
		}
		
		//There is many better ways to do this....
		//Another option is to take care of includeSelection only at the end.
		ArrayList<Integer> deleteList = new ArrayList<Integer>(); 
		for (int i = 0; i < table.getCounter() ; i++) {
			double value = table.getValue(column, i);
			
			if (Double.isNaN(value)) {
                //Lets just remove all of the null values... They can't be filtered correctly
				deleteList.add(i);
			} else if (TableFilter) {
				if (includeSelection) {
					deleteList.add(i);
					for (int q=0; q<filterList.length; q++) {
						if (value == filterList[q]) {
							deleteList.remove(deleteList.size() - 1);
							break;
						}
					}
				} else {
					for (int q=0; q<filterList.length; q++) {
						if (value == filterList[q]) {
							deleteList.add(i);
							break;
						}
					}
				}
			} else if (STDFilter) {
				if (value < (mean - N_STD*STD) || value > (mean + N_STD*STD) ) {
					if (includeSelection)
						deleteList.add(i);
				} else {
					if (!includeSelection)
						deleteList.add(i);
				}
			} else if (value < min || value > max) {
				if (includeSelection)
					deleteList.add(i);
			} else if (value >= min && value <= max) {
				if (!includeSelection)
					deleteList.add(i);
			}
		
		}
		
		//I guess I put this since the delete method in ResultsTableUtil doesn't work with ArrayLists
		int[] delList = new int[deleteList.size()];
		for (int i=0 ; i < deleteList.size(); i++) {
			delList[i] = deleteList.get(i);
		}

		ResultsTableUtil.delete(table, delList);
		ResultsTableUtil.show(table, tableName);
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
			for (int i = 0; i < columns.length ; i++) {
				((Choice)choice_boxes.get(1)).add(columns[i]);
			}
	    }
		table = ResultsTableUtil.getResultsTable(tableName);
		column = dialog.getNextChoice();
		int newColumnIndex = table.getColumnIndex(column);
		
		String FilterType = dialog.getNextRadioButton();
		String selectionType = dialog.getNextRadioButton();
		
		min = dialog.getNextNumber();
		max = dialog.getNextNumber();
		
		N_STD = dialog.getNextNumber();
		
		filterTable = ResultsTableUtil.getResultsTable(dialog.getNextChoice());
		
		if (FilterType == "Table") {
			TableFilter = true;
			STDFilter = false;
		} else if (FilterType == "Standard Deviation") {
			STDFilter = true;
			TableFilter = false;
		} else {
			STDFilter = false;
			TableFilter = false;
		}
		
		if (selectionType == "include") {
			includeSelection = true;
		} else {
			includeSelection = false;
		}
		
		if (newColumnIndex != ResultsTable.COLUMN_NOT_FOUND && newColumnIndex != columnIndex) {
			
			double[] values = table.getColumnAsDoubles(newColumnIndex);
			min = max = values[0];
			
			for (double value: values) {
				if (value < min)
					min = value;
				else if (value > max)
					max = value;
			}
			
			@SuppressWarnings("unchecked")
			Vector<TextField> stringFields = (Vector<TextField>)dialog.getNumericFields();
			stringFields.get(0).setText(Double.toString(min));
			stringFields.get(1).setText(Double.toString(max));
			
			columnIndex = newColumnIndex;
		}
		
		if (TableFilter) {
			dialog.getComponent(9).setEnabled(false);
			dialog.getComponent(11).setEnabled(false);
			dialog.getComponent(13).setEnabled(false);
			dialog.getComponent(15).setEnabled(true);
		} else if (STDFilter) {
			dialog.getComponent(9).setEnabled(false);
			dialog.getComponent(11).setEnabled(false);
			dialog.getComponent(13).setEnabled(true);
			dialog.getComponent(15).setEnabled(false);
		} else {
			dialog.getComponent(9).setEnabled(true);
			dialog.getComponent(11).setEnabled(true);
			dialog.getComponent(13).setEnabled(false);
			dialog.getComponent(15).setEnabled(false);
		}
		
		return min <= max || TableFilter || STDFilter;
	}
}

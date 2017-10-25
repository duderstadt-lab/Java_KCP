package table;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import ij.WindowManager;
import ij.measure.ResultsTable;
import ij.text.TextPanel;
import ij.text.TextWindow;

import ij.IJ;

public class ResultsTableUtil {
	private static final long serialVersionUID = 1L;
	
	public static ArrayList<ResultsTableView> tableViews = new ArrayList<ResultsTableView>();
	public static ArrayList<String> tableViewTitles = new ArrayList<String>();
	//public static Random ran = new Random();
	//public static double serial = ran.nextDouble();
	
	public synchronized static void addResultsTable(ResultsTableView view, String title) {
		if (!tableViewTitles.contains(title)) {
			tableViews.add(view);
			tableViewTitles.add(title);
		}
		//IJ.log("Object serial " + serial);
		//IJ.log("Done adding ResultsTableView to Utils static vars");
		//IJ.log("table Views = " + tableViews.size());
		//IJ.log("table titles = " + tableViewTitles.size());
	}
	
	public synchronized static void removeResultsTable(String title) {
		if (tableViewTitles.contains(title)) {
			tableViews.remove(tableViewTitles.indexOf(title));
			tableViewTitles.remove(title);
		}
	}
	
	public synchronized static void rename(String oldName, String newName) {
		tableViewTitles.set(tableViewTitles.indexOf(oldName), newName);
	}
	
	public synchronized static void show(ResultsTable table, String title) {
		if (tableViewTitles.contains(title)) {
			tableViews.get(tableViewTitles.indexOf(title)).update();
		} else if (WindowManager.getFrame(title) instanceof TextWindow){
			table.show(title);	
		} else {
			new ResultsTableView(table, title);
		}
	}
	
	public synchronized static String[] getResultsTableTitles() {
		
		Frame[] nonImageWindows = WindowManager.getNonImageWindows();
		String[] titles = new String[nonImageWindows.length];
		
		int n = 0;
		
		for (Frame frame: nonImageWindows) {
			
			if (frame instanceof TextWindow) {
				
				TextWindow textWindow = (TextWindow)frame;
				TextPanel textPanel = textWindow.getTextPanel();
				ResultsTable table = textPanel.getResultsTable();
				
				if (table != null)
					titles[n++] = frame.getTitle();
			} 
		}

		for (int i=0;i<tableViewTitles.size();i++)
			titles[n++] = tableViewTitles.get(i);
				
		//IJ.log("Object serial " + serial);
		//IJ.log("Getting titles");
		//IJ.log("tableViewTitles " + tableViewTitles.size());
		
		return Arrays.copyOf(titles, n);
	}
	
	public synchronized static ResultsTable getResultsTable(String title) {
		
		if (tableViewTitles.contains(title)) {
			return tableViews.get(tableViewTitles.indexOf(title)).results;
		}
		
		Frame frame = WindowManager.getFrame(title);
		if (frame instanceof TextWindow) {
			TextWindow textWindow = (TextWindow)frame;
			TextPanel textPanel = textWindow.getTextPanel();
			return textPanel.getResultsTable();	
		}
		
		return null;
	}
	
	public synchronized static ResultsTableView getResultsTableView(String title) {
		if (tableViewTitles.contains(title)) {
			return tableViews.get(tableViewTitles.indexOf(title));
		}
		
		return null;
	}
	
	/*
	public static void delete(ResultsTable table, int[] rows) {
		
		if (rows.length == 0)
			return;
		
		ResultsTableList list = new ResultsTableList(table);
		
		int to = rows[rows.length - 1] + 1;
		
		for (int i = rows.length - 1; i > 0; i--) {
			
			if (rows[i] != rows[i - 1] + 1) {
				list.removeRange(rows[i], to);
				to = rows[i - 1] + 1;
			}
			
		}
		
		list.removeRange(rows[0], to);
	}*/
	
	public synchronized static void delete(ResultsTable table, int[] rows) {
		if (rows.length == 0)
			return;
		
		String[] columns = table.getColumnHeadings().split(",|\\t+");
		
		int pos = 0;
		int rowsIndex = 0;
		for (int i = 0; i < table.getCounter(); i++) {
			if (rowsIndex < rows.length) {
				if (rows[rowsIndex] == i) {
					rowsIndex++;
					continue;
				}
			}
			
			if (pos != i) {
				//means we need to move row i to position row.
				for (int j=1;j<columns.length;j++)
					table.setValue(columns[j], pos, table.getValue(columns[j], i));
			}
			pos++;
		}
		
		// delete last rows
		for (int row = table.getCounter() - 1; row > pos-1; row--)
			table.deleteRow(row);
	}
}

package kcp;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.util.ArrayList;
import java.util.Vector;

import table.ResultsTableSorter;
import table.ResultsTableUtil;

import ij.gui.DialogListener;

import ij.IJ;
import ij.plugin.PlugIn;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;

import table.*;

public class BatchKineticChangePoint implements PlugIn, DialogListener {
	
	private ArrayList<Integer> groupOffsets = new ArrayList<Integer>();
	private String[] tableTitles;
	private String tableName;
	ResultsTable table;
	boolean step_analysis = false;
	
	@Override
	public void run(String arg) {
		tableTitles = ResultsTableUtil.getResultsTableTitles();
		
		if (tableTitles.length == 0) {
			IJ.showMessage("No results tables!");
			return;
		}
			
		tableName = tableTitles[0];
		table = ResultsTableUtil.getResultsTable(tableName);
		
		// show dialog
		GenericDialog dialog = new GenericDialog("Batch Kinetic Change Point");
			
		String[] columns = table.getColumnHeadings().split("\\t");
		
		String xColumn = new String("x-Column");
		String yColumn = new String("y-Column");
		String groupColumn = new String("trajectory");
		double sigma = 100;
		double confidenceLevel = 0.99;
		
		dialog.addChoice("Table", tableTitles, tableName);
		dialog.addChoice("x_column", columns, xColumn);
		dialog.addChoice("y_column", columns, yColumn);
		dialog.addNumericField("Sigma", sigma, 2);	
		dialog.addNumericField("Confidence value", confidenceLevel, 2);
		dialog.addCheckbox("steps", step_analysis);
		
		dialog.addDialogListener(this);
		dialog.showDialog();
		
		if (dialog.wasCanceled())
			return;
		
		tableName = dialog.getNextChoice();
		xColumn = dialog.getNextChoice();
		yColumn = dialog.getNextChoice();
		sigma = dialog.getNextNumber();
		confidenceLevel = dialog.getNextNumber();
		step_analysis = dialog.getNextBoolean();
		table = ResultsTableUtil.getResultsTable(tableName);
		
		double[] xData = table.getColumnAsDoubles(table.getColumnIndex(xColumn));
		double[] yData = table.getColumnAsDoubles(table.getColumnIndex(yColumn));
		
		// determine the offset of each group
		groupOffsets.add(0);
				
		if (table.getColumnIndex(groupColumn) != ResultsTable.COLUMN_NOT_FOUND) {
			// make sure we sort on groupColumn
			ResultsTableSorter.sort(table, true, groupColumn);
					
			double[] group_num = table.getColumnAsDoubles(table.getColumnIndex(groupColumn));
					
			for (int i = 1; i < table.getCounter(); i++) {
				if (group_num[i] != group_num[i - 1]) {
					groupOffsets.add(i);
				}
			}
		}
		groupOffsets.add(table.getCounter());
		
		ResultsTable segsOutput = new ResultsTable();
		int Srow = 0;
		
		for (int i = 0; i < groupOffsets.size() - 1; i++) {
			IJ.showStatus("Processing trajectory " + (i + 1));

			    KineticChangePoint change = new KineticChangePoint(sigma, confidenceLevel, groupOffsets.get(i), groupOffsets.get(i+1) - groupOffsets.get(i), xData, yData, step_analysis);
				ArrayList<Segment> segments = change.generate_segments();
				for (int j = 0; j < segments.size(); j++) {
					segsOutput.incrementCounter();
					segsOutput.setValue("x1", Srow, segments.get(j).x1);
					segsOutput.setValue("y1", Srow, segments.get(j).y1);
					segsOutput.setValue("x2", Srow, segments.get(j).x2);
					segsOutput.setValue("y2", Srow, segments.get(j).y2);
					segsOutput.setValue("A", Srow, segments.get(j).A);
					segsOutput.setValue("sigma_A", Srow, segments.get(j).A_sigma);
					segsOutput.setValue("B", Srow, segments.get(j).B);
					segsOutput.setValue("sigma_B", Srow, segments.get(j).B_sigma);
					segsOutput.setValue("trajectory", Srow, table.getValue(groupColumn, groupOffsets.get(i)));
					Srow++;
				}
		}
		

		new ResultsTableView(segsOutput, "Segments Table");
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
			((Choice)choice_boxes.get(2)).removeAll();
			for (int i = 0; i < columns.length ; i++) {
				((Choice)choice_boxes.get(1)).add(columns[i]);
				((Choice)choice_boxes.get(2)).add(columns[i]);
			}
	    }
		
		return true;
	}
	
}

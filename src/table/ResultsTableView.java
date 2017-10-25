package table;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import ij.plugin.frame.Recorder;

public class ResultsTableView implements ActionListener, PlugIn {

	ResultsTable results;
	
	private JFrame frame;
	JTable table;
	JScrollPane scrollPane;
	private AbstractTableModel tableModel;
	private JMenuItem saveAsMenuItem = new JMenuItem("Save As", KeyEvent.VK_S);
	private JMenuItem renameMenuItem = new JMenuItem("Rename", KeyEvent.VK_R);
	private JMenuItem duplicateMenuItem = new JMenuItem("Duplicate", KeyEvent.VK_D);
	private JMenuItem cutMenuItem = new JMenuItem("Cut");
	private JMenuItem copyMenuItem = new JMenuItem("Copy", KeyEvent.VK_C);
	private JMenuItem clearMenuItem = new JMenuItem("Clear");
	private JMenuItem selectAllMenuItem = new JMenuItem("Select All", KeyEvent.VK_A);
	//private JMenuItem sortMenuItem = new JMenuItem("Sort", KeyEvent.VK_S);
	//private JMenuItem plotMenuItem = new JMenuItem("Plot", KeyEvent.VK_P);
	//private JMenuItem filterMenuItem = new JMenuItem("Filter", KeyEvent.VK_F);
	private JMenuItem toImageJResultsTableMenuItem = new JMenuItem("To ImageJ Results Table");
	
	//static so that table locations are offset...
	static int pos_x = 100;
	static int pos_y = 130;
	static int offsetX = 0;
	
	public ResultsTableView() {
		
	}
	
	public ResultsTableView(ResultsTable results, String title) {
		this.results = results;
		createFrame(title);
		
		// hide the original results table
		/*
		Window resultsTableWindow = WindowManager.getWindow(title);
		
		if (resultsTableWindow != null) {
			resultsTableWindow.setVisible(false);
			WindowManager.removeWindow(resultsTableWindow);
		}
		*/
		
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				if (Recorder.record) {
					Recorder.record("Ext.ResultsTableView_Close", frame.getTitle());
				}
				
				ResultsTableUtil.removeResultsTable(frame.getTitle());
				WindowManager.removeWindow(frame);
			}
			
		});
		
		// add window to window manager
		WindowManager.addWindow(frame);
		
		// add to global lists
		ResultsTableUtil.addResultsTable(this, title);
		//IJ.log("Done building ResultsTableView");
	}
	
	public void update() {
		for (int i = 0; i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(75);
		
		tableModel.fireTableStructureChanged();
	}
	
	private void createFrame(String title) {
		tableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (columnIndex == 0)
					return rowIndex + 1;
				
				return results.getStringValue(columnIndex - 1, rowIndex);
			}
			
			@Override
			public String getColumnName(int columnIndex) {
				if (columnIndex == 0)
					return "Row";
				
				return results.getColumnHeading(columnIndex - 1);
			}

			@Override
			public int getRowCount() {
				return results.getCounter();
			}
			
			@Override
			public int getColumnCount() {
				return results.getLastColumn() + 2;
			}
			
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				double value = Double.parseDouble((String)aValue);
				results.setValue(columnIndex - 1, rowIndex, value);
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex)  {
				return columnIndex > 0;
			}
			
		};
		
		table = new JTable(tableModel);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		for (int i = 0; i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(75);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane = new JScrollPane(table);
		JMenuBar mb = new JMenuBar();
		
		// file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		fileMenu.add(saveAsMenuItem);
		fileMenu.add(renameMenuItem);
		fileMenu.add(duplicateMenuItem);
		
		mb.add(fileMenu);
		
		// edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		editMenu.add(cutMenuItem);
		editMenu.add(copyMenuItem);
		editMenu.add(clearMenuItem);
		editMenu.add(selectAllMenuItem);
		//editMenu.addSeparator();
		//editMenu.add(filterMenuItem);
		//editMenu.add(sortMenuItem);
		//editMenu.add(plotMenuItem);
		editMenu.addSeparator();
		editMenu.add(toImageJResultsTableMenuItem);
		
		mb.add(editMenu);

		// set action listeners
		saveAsMenuItem.addActionListener(this);
		renameMenuItem.addActionListener(this);
		duplicateMenuItem.addActionListener(this);
		cutMenuItem.addActionListener(this);
		copyMenuItem.addActionListener(this);
		clearMenuItem.addActionListener(this);
		selectAllMenuItem.addActionListener(this);
		//filterMenuItem.addActionListener(this);
		//sortMenuItem.addActionListener(this);
		//plotMenuItem.addActionListener(this);
		toImageJResultsTableMenuItem.addActionListener(this);
		
		frame = new JFrame(title);
		frame.setSize(400, 300);
		frame.setLocation(pos_x, pos_y);
		pos_x += 10;
 		pos_y += 30;
 		if (pos_y > 600) {
 			offsetX += 200;
 			pos_x = offsetX;
 			pos_y = 130;
 		} else if (pos_x > 1000) {
 			offsetX = 0;
 			pos_x = 100;
 			pos_y = 130;
 		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(scrollPane);
		frame.setJMenuBar(mb);
		frame.setVisible(true);
	}
	
	public ResultsTable getResults() {
		return results;
	}
	
	@Override
	public void run(String arg0) {
		
		String[] resultsTableTitles = ResultsTableUtil.getResultsTableTitles();
		
		if (resultsTableTitles.length == 0) {
			IJ.showMessage("No open results tables!");
			return;
		}
		
		GenericDialog dialog = new GenericDialog("SMB Results Table");
		dialog.addChoice("Table", resultsTableTitles, resultsTableTitles[0]);
		dialog.showDialog();
		
		if (dialog.wasCanceled())
			return;
		
		String title = dialog.getNextChoice();
		ResultsTable rt = ResultsTableUtil.getResultsTable(title);
		
		// hide the original results table
		WindowManager.getWindow(title).setVisible(false);
		
		new ResultsTableView(rt, title);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveAsMenuItem)
			saveAs();
		else if (e.getSource() == renameMenuItem) {
			String title = JOptionPane.showInputDialog("Table name", frame.getTitle());
			rename(title);
		} else if (e.getSource() == duplicateMenuItem)
			duplicate();
		else if (e.getSource() == cutMenuItem)
			cut();
		else if (e.getSource() == copyMenuItem)
			copy();
		else if (e.getSource() == clearMenuItem)
			clear();
		else if (e.getSource() == selectAllMenuItem)
			selectAll();
		//else if (e.getSource() == filterMenuItem)
		//	filter();
		//else if (e.getSource() == sortMenuItem)
		//	sort();
		//else if (e.getSource() == plotMenuItem)
			//plot();
		else if (e.getSource() == toImageJResultsTableMenuItem)
			toImageJResultsTable();
		
	}
	
	protected void saveAs() {
		try {
			results.saveAs("");
		} catch (IOException e) {
			IJ.showMessage(e.getMessage());
		}
	}
	
	public void rename(String title) {
		if (title != null) {
			if (Recorder.record) {
				Recorder.record("Ext.ResultsTableView_Rename", frame.getTitle(), title);
			}
			
			WindowManager.removeWindow(frame);
			ResultsTableUtil.rename(frame.getTitle(), title);
			frame.setTitle(title);
			WindowManager.addWindow(frame);
		}
	}
	
	public void close() {
		ResultsTableUtil.removeResultsTable(frame.getTitle());
		WindowManager.removeWindow(frame);
		
		frame.setVisible(false);
		frame.dispose();
		
		results = null;
	}
	
	protected void duplicate() {
		new ResultsTableView((ResultsTable) results.clone(), WindowManager.getUniqueName(frame.getTitle()));
	}
	
	protected void cut() {
		copy();
		clear();
	}
	
	protected void copy() {
		
		final int[] selectedRows = table.getSelectedRows();
		
		Transferable transferable = new Transferable() {
			
			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);
			}
			
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{DataFlavor.stringFlavor};
			}
			
			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				
				if (flavor.equals(DataFlavor.stringFlavor)) {
					String csv = "";
					
					for (int i = 0; i < selectedRows.length; i++)
						csv += results.getRowAsString(selectedRows[i]) + "\n";
					
					return csv;
				}
				else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
		};
		
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
	}
	
	protected void clear() {
		ResultsTableUtil.delete(results, table.getSelectedRows());
		
		table.clearSelection();
		tableModel.fireTableDataChanged();
	}
	
	protected void selectAll() {
		table.selectAll();
	}
	
	protected void filter() {
		ResultsTableFilter filter = new ResultsTableFilter(results);
		filter.run("");
		tableModel.fireTableDataChanged();
	}
	
	protected void sort() {
		ResultsTableSorter sorter = new ResultsTableSorter(results);
		sorter.run("");
		tableModel.fireTableDataChanged();
	}
	
	protected void plot() {
		//ResultsTablePlotter plotter = new ResultsTablePlotter(results);
		//plotter.run("");
	}
	
	protected void toImageJResultsTable() {
		WindowManager.removeWindow(frame);
		ResultsTableUtil.removeResultsTable(frame.getTitle());
		
		frame.setVisible(false);
		frame.dispose();
		
		results.show(frame.getTitle());
	}
}

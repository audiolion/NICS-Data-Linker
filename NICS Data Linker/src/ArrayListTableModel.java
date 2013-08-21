import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

/**
 * Class serves as a table model for a JTable that
 * converts comma separated arraylist strings into 
 * a JTable.
 *
 * @contributor Ryan Castner
 */
@SuppressWarnings("serial")
public class ArrayListTableModel extends JFrame {
	
	private JTable table;
	private JTextField filterText;
	private TableRowSorter<MyModel> sorter;
	private final static String[] header = {"Patient Name", "Meditech ID", "Code", "Date/Time"};
	private static final String DEFAULT = "Search Table...";
	
	ArrayListTableModel(ArrayList<String> al) {
		MyModel mm = new MyModel(al, header);
		table = new JTable(mm);
		sorter = new TableRowSorter<MyModel>(mm);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
	    .setHorizontalAlignment(JLabel.CENTER);
		table.setRowSorter(sorter);
		TableColumn col = null;
		for(int i = 0; i < table.getColumnCount(); i++){
			col = table.getColumnModel().getColumn(i);
			if(i != 0){
				col.setCellRenderer(centerRenderer);
			}
			if(i == 2){
				col.setPreferredWidth(30);
			}else if(i == 0){
				col.setPreferredWidth(160);
			}else if(i == 1){
				col.setPreferredWidth(80);
			}else{
				col.setPreferredWidth(100);
			}
		}
		this.setSize(600,300);
		
		this.add(new JScrollPane(table));
		JPanel form = new JPanel();
		filterText = new JTextField(DEFAULT);
		filterText.setPreferredSize(new Dimension(120,30));
		filterText.getDocument().addDocumentListener(
				new DocumentListener(){
					public void changedUpdate(DocumentEvent e){
						newFilter();
					}
					public void insertUpdate(DocumentEvent e){
						newFilter();
					}
					public void removeUpdate(DocumentEvent e){
						newFilter();
					}
				});
		filterText.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				if(filterText.getText().equals(DEFAULT)) {
					filterText.setText("");
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(filterText.getText().equals("")) {
					filterText.setText(DEFAULT);
				}
			}
		});
		filterText.setMinimumSize(filterText.getPreferredSize());
		form.add(filterText);
		this.add(form, BorderLayout.SOUTH);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private void newFilter(){
		if(!filterText.getText().equals(DEFAULT)){
			RowFilter<MyModel, Object> rf = null;
			try{
				rf = RowFilter.regexFilter("(?i)" + filterText.getText());
			}catch(java.util.regex.PatternSyntaxException e){
				return;
			}
			sorter.setRowFilter(rf);
		}
	}
	
	class MyModel extends AbstractTableModel {
		
		private ArrayList<String> al;
		private String[] header;
		
		MyModel(ArrayList<String> al, String[] header) {
			this.al = al;
			this.header = header;
		}

		public int getColumnCount() {
			return header.length;
		}

		public int getRowCount() {
			return al.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			String[] token = al.get(rowIndex).split(",");
			return token[columnIndex];
		}

		public String getColumnName(int col) {
			return header[col];
		}	
		
		public boolean isCellEditable(int row, int col){
			return false;
		}
	}
}


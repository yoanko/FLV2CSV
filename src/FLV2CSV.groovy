
class Column {
	def position
	def length
	def label
	Column(aPosition,aLength,aLabel) {
		position = Integer.decode(aPosition) - 1
		length = Integer.decode(aLength)
		label = aLabel
	}
}
//@groovy.transform.TypeChecked
def List createColumns(BufferedReader aBufferedReader) {
	String currentLine;
	def columns = [] 
	while((currentLine = aBufferedReader.readLine()) != null) {
		def strings = currentLine.split(";")
		if(strings.size() == 3)
		{
			columns << (new Column(strings[1],strings[2],strings[0]))
		}}
	return columns
}

def generateCSV(aColumns, aBufferedReader,out) {
	def currentLine;
	def currentValue;

	gTableModel = new DefaultTableModel();
	
	aColumns.each { currentColumn -> 
		out.write(currentColumn.label + ";")   
		gTableModel.addColumn(currentColumn.label)
	}
	out.write("\n")
    
	def lNewRow = new String[aColumns.size()]
	while((currentLine = aBufferedReader.readLine()) != null) {
		def i=0
		for(currentColumn in aColumns) {  
			currentValue = ""
			if(currentColumn.getPosition() <= currentLine.length() - 1) {
				if(currentColumn.getPosition() + currentColumn.getLength() <= currentLine.length() ) {
					currentValue = currentLine.substring(currentColumn.getPosition(),currentColumn.getPosition()+currentColumn.getLength()) 
				}
				else {
					currentValue = currentLine.substring(currentColumn.getPosition(),currentLine.getLength()) 
				}
			}
			out.write(currentValue + ";")
			lNewRow[i++]= currentValue
		}
		out.write("\n")
		gTableModel.addRow(lNewRow)
	}
}


import groovy.swing.SwingBuilder
import javax.swing.BoxLayout
import javax.swing.border.LineBorder
import java.awt.Color;
import javax.swing.JPanel
import javax.swing.TransferHandler
import javax.swing.TransferHandler.TransferSupport
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.DataFlavor
import java.io.BufferedReader
import javax.swing.JFrame
import javax.swing.table.DefaultTableModel
import javax.swing.JTable
import java.awt.Dimension
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.bind.JAXBElement
import javax.swing.JComboBox
import java.awt.event.WindowListener
    
gTabbedPane = null
gTextArea = null
JTable gTable = null
gTableModel = null

@XmlType
class State {
	@XmlElement
	List<String> formatFiles = new ArrayList()
	//TODO add a property change when adding an element
}
State state = null
File stateFile = new File("state.xml")
if(stateFile.exists())
{
	JAXBContext jaxbContext = JAXBContext.newInstance(State.class)
	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
	JAXBElement<State> e = unmarshaller.unmarshal(stateFile)
	state = e.getValue()
}
else {
	state = new State()
}
JComboBox comboBox = null
new SwingBuilder().edt {
	JFrame jframe = frame(title:'FLV2CSV', show: true,minimumSize: [300,300],defaultCloseOperation:JFrame.EXIT_ON_CLOSE) {
		gTabbedPane = tabbedPane() {
			panel(title:"control") {
				boxLayout(axis: BoxLayout.Y_AXIS)

				rigidArea()
				JPanel panelFLVFormat = panel( border : new LineBorder(Color.black) ) {
					comboBox = comboBox()
					for(String formatFile : state.formatFiles)
					{
						comboBox.addItem(formatFile)
					}
					label(text:'drag fixed length values file format')
				}
				panelFLVFormat.setTransferHandler(new TransferHandler()
					{
						public boolean canImport(TransferSupport aTS)
						{
							if (!aTS.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
								return false;
							}
							return true; 
						}
						public boolean importData(TransferSupport supp) {
                     
							Transferable t = supp.getTransferable();
							List<File> lFiles = t.getTransferData(DataFlavor.javaFileListFlavor);
							importFLVFileFormat(lFiles.get(0))

							state.formatFiles.add(lFiles.get(0).getCanonicalName())
							return true;
						}
					}
				)
				rigidArea()
				JPanel panelFLV = panel( border : new LineBorder(Color.black)) {
					label(text:'drag fixed length values file')
				}
				panelFLV.setTransferHandler(new TransferHandler()
					{
						public boolean canImport(TransferSupport aTS)
						{
							return true;
						}

						public boolean importData(TransferSupport supp) {
							if(!canImport(supp)) 
							{
								return false;
							}
							Transferable t = supp.getTransferable();
							List<File> lFiles = t.getTransferData(DataFlavor.javaFileListFlavor);
							importFLVFile(lFiles.get(0))
							return true;
						}
					}
				)
			}
		
			scrollPane(title:"csv file") {
				gTextArea = textArea() {}
			}
			scrollPane(title:"tabular data") {
				gTable = table() {}
				gTable.setShowGrid(true)
			}
		}
	}
	jframe.addWindowListener(new WindowListener() {
			
	}) 
		
	
	
}

gColumns = null 

def importFLVFileFormat(aFile) {
	println ("importing format file " + aFile)
	gColumns = createColumns(new BufferedReader(new InputStreamReader(new FileInputStream(aFile))))
}
def importFLVFile(File aFile) {
	println ("importing data file " + aFile)
	StringWriter out = new StringWriter()
	generateCSV(gColumns,new BufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(aFile)))),out)
	gTextArea.setText(out.toString())
	gTable.setModel(gTableModel)
	gTable.setShowGrid(true)
	
	out.close()
}

//importFLVFileFormat(new File("/Users/Johan/NetBeansProjects/FLV2CSV/format1.txt"))
//importFLVFile(new File("/Users/Johan/NetBeansProjects/FLV2CSV/value1.txt"))

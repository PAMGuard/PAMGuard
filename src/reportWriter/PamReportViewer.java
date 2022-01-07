package reportWriter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

//
//import sl.docx.DocxDocument;
//import sl.docx.DocxEditorKit;

public class PamReportViewer {

	/**
	 * doesnt' work !
	 * @param docName
	 * @return
	 */
//	public static boolean viewWordDocument(String docName) {
//		if (docName == null) {
//			return false;
//		}
//		File docFile = new File(docName);
//		if (docFile.exists() == false) {
//			return false;
//		}
//
////		WordprocessingMLPackage wordMLPackage = null;
////		try {
////			wordMLPackage = Docx4J.load(docFile);
////		} catch (Docx4JException e) {
////			e.printStackTrace();
////			return false;
////		}
//		
//		JFrame docFrame = new JFrame(docName);
//		docFrame.setSize(new Dimension(600,600));
//		docFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		JTextPane edPane = new JTextPane();
////		edPane.setBackground(Color.PINK);
//		DocxEditorKit docKit;
//		edPane.setEditorKit(docKit = new DocxEditorKit());
//		docFrame.setLayout(new BorderLayout());
////		docFrame.add(BorderLayout.CENTER, textArea);
//		JScrollPane scrollPane = new JScrollPane(edPane);
//		scrollPane.setBackground(Color.green);
//		docFrame.add(scrollPane, BorderLayout.CENTER);
//		try {
//			sl.docx.DocxDocument doc = new DocxDocument();
//			docKit.read(new FileInputStream(docFile), doc, 0);
//		} catch (IOException | BadLocationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
////		textArea.setd
////		wordMLPackage.getDocumentModel();
//		
//		
//		docFrame.setVisible(true);
//		return true;
//	}
//	
}

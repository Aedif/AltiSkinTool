package ui;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import image.Header;
import image.ImageReader;
import image.ImageWriter;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ImageWindow {

	public JFrame frame;
	
	private final JFileChooser fc = new JFileChooser();
	
	private String imgPath = null;
	private String dist_path = null;
	
	private JLabel lblImage;
	private JButton btnSaveImg;
	private JButton btnSelFile;
	private BufferedImage buffImg;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ImageWindow window = new ImageWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ImageWindow() {
		this("C:\\Users\\Aedifico\\Desktop\\alti_image_adventures\\resources\\dist\\.image\\images\\player_ranks\\tiny");
	}
	
	public ImageWindow(String dist_path){
		this.dist_path = dist_path;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		if(dist_path != null)
			fc.setCurrentDirectory(new File(dist_path + "/.image"));
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnSelFile = new JButton("Sel File");
		btnSelFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fc.showOpenDialog(frame);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            imgPath = file.getAbsolutePath();
		            readImage(imgPath);
		        } else {
		            //log.append("Open command cancelled by user." + newline);
		        }
			}
		});
		panel.add(btnSelFile);
		
		btnSaveImg = new JButton("Save IMG");
		btnSaveImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fc.showSaveDialog(frame);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            if(file.getName().endsWith(".png") || file.getName().endsWith(".PNG"))
		            	ImageWriter.saveAsPNG(buffImg, file);
		            else
		            	ImageWriter.saveAsPNG(buffImg, new File(file.getAbsolutePath()+".png"));
		        } else {

		        }
			}
		});
		panel.add(btnSaveImg);
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		lblImage = new JLabel();
		scrollPane.setViewportView(lblImage);
	}
	
	private void readImage(String imgPath){
		File imgFile = new File(imgPath);
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(imgFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(fileBytes == null){ System.out.println("fileBytes is NULL"); return;}
		
		Header header = new Header(fileBytes);
		buffImg = ImageReader.readImage(header, fileBytes);
		lblImage.setIcon(new ImageIcon(buffImg));
		System.out.println(header);
	}
}

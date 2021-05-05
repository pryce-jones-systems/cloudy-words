package WordCloud;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class WordCloudFrame {
	
	// Global variables
	private static BufferedImage imageBuffer;
	private static JFrame frame;
	private static JPanel controlPanel, textInputPanel, imagePanel, leftPanel, rightPanel, copyrightPanel, colorRadioButtonPanel;
	private static JButton openButton, saveButton;
	private static JTextArea wordInputBox;
	private static JLabel wordCloudImage, inputTextLabel, imageTextLabel, sliderLabel, copyrightLabel, colorLabel;
	private static JSlider sizeSlider;
	private static JScrollPane textBoxScrollArea;
	private static JRadioButton pastelColorButton, blueToRedColorButton, cyanToMagentaColorButton, blackColorButton;
	private static ButtonGroup colorRadioButtons;
	private static String colorMode;
	private static Random rng;
	
	/**
	 * Main method
	 * @param args arg[0] is a path to a text file to be turned into a word cloud
	 */
	public static void main(String[] args) {
		
		// Use system look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { }
		
		// Create random number generator
		rng = new Random();
		
		imageBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		
		// Create frame
		frame = new JFrame();
		frame.setTitle("Cloudy Words");
		frame.setSize(new Dimension(800, 800));
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(1, 2));
		
		createImagePanel();
		createInputPanel();
		createControlPanel();
		createCopyrightPanel();
		
		
		if (args.length == 1) {
			try {
				
				// Read input file
				File inFile = new File(args[0]);
				Scanner fileScanner = new Scanner(inFile);
				String fileText = new String();
				while (fileScanner.hasNextLine()) {
					String curLine = fileScanner.nextLine();
					fileText += curLine;
				}
				fileScanner.close();
				
				// Put into input box
				wordInputBox.setText(fileText);
				
			} catch (IOException e) { }
		}
		updateWordCloud();
		
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(2, 1));
		leftPanel.add(imagePanel);
		leftPanel.add(textInputPanel);
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(controlPanel, BorderLayout.NORTH);
		rightPanel.add(copyrightPanel, BorderLayout.SOUTH);
		
		
		
		frame.add(leftPanel);
		frame.add(rightPanel);
		
		// Show
		frame.setVisible(true);
	}
	
	/**
	 * Recalculates the word frequencies, creates a new word cloud and displays it
	 */
	private static void updateWordCloud() {
		
		// Get array of all words
		String cleanedWords = getCleanWordString(wordInputBox.getText());
		String[] words = cleanedWords.split(" ");
		
		// Calculate frequency of words
		Map<String, Integer> wordFreq = new HashMap<String, Integer>();
		Arrays.stream(words).collect(Collectors.groupingBy(s -> s)).forEach((k, v) -> wordFreq.put(k, v.size()));
		
		// Calculate normalised frequency of words
		int minFreq = Collections.min(wordFreq.values());
		int maxFreq = Collections.max(wordFreq.values());
		int totalFreq = words.length;
		Map<String, Double> normFreq = new HashMap<String, Double>();
		for (String word: wordFreq.keySet()) {
			double freq = (double) wordFreq.get(word);
			if (maxFreq == minFreq) {
				normFreq.put(word, 1.0);
			} else {
				double b = (freq - minFreq) / (maxFreq - minFreq);
				normFreq.put(word, b);
			}			
		}
		
		// Generate image
		imageBuffer = getWordCloud(normFreq, sizeSlider.getValue(), sizeSlider.getValue());
		ImageIcon icon = new ImageIcon(imageBuffer.getScaledInstance(400, 400, Image.SCALE_SMOOTH));
		wordCloudImage.setIcon(icon);
		
		// Update controls
		imageTextLabel.setVisible(false);
		saveButton.setEnabled(true);
	}
	
	/**
	 * Gets a word cloud
	 * @param normFreq HashMap containing all words and their normalised frequencies
	 * @param w width in pixels
	 * @param h height in pixels
	 * @return a BufferedImage representing the word map
	 */
	private static BufferedImage getWordCloud(Map<String, Double> normFreq, int w, int h) {
		
		// Create blank image
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		g2.setBackground(Color.WHITE);
		g2.fillRect(0, 0, w, h);
		double totalArea = ((double) w) * ((double) h);

		// Iterate over words
		for (String word: normFreq.keySet()) {
			
			// Calculate size of word
			double relativeArea = normFreq.get(word) * totalArea;
			int rectW = (int) Math.round(Math.sqrt(relativeArea));
			int rectH = (int) Math.round(Math.sqrt(relativeArea));
			
			// Calculate position of word
			int rectX = randIntInRange((0.05 * w), w * 0.9);
			int rectY = randIntInRange((0.05 * h), h * 0.9);
			
			// Draw background for word
			g2.setColor(Color.RED);
			
			// Calculate font
			AffineTransform af = new AffineTransform();
			af.rotate(Math.toRadians(randIntInRange(-45, 45)), 0, 0);
			Font font = new Font(null, Font.BOLD, (int) Math.round((normFreq.get(word) * 48) + 16));			
			Font rotatedFont = font.deriveFont(af);
			
			// Calculate colour
			Color color;
			switch (colorMode) {
			case "Pastel":
				int r = rng.nextInt(10);
				if (r == 0) {
					color = new Color(0xf274bc);
				} else if (r == 1) {
					color = new Color(0xda836e);
				} else if (r == 2) {
					color = new Color(0x129a7d);
				} else if (r == 3) {
					color = new Color(0x9bd3cb);
				} else if (r == 4) {
					color = new Color(0xb5dccd);
				} else if (r == 5) {
					color = new Color(0xdc7684);
				} else if (r == 6) {
					color = new Color(0xe4ca99);
				} else if (r == 7) {
					color = new Color(0x2d7f9d);
				} else if (r == 8) {
					color = new Color(0xa4c9d7);
				} else if (r == 9) {
					color = new Color(0x717d84);
				} else {
					color = new Color(0x000000);
				}
				break;
			case "BlueToRed":
				color = new Color(lerp(0x0000ff, 0xff0000, normFreq.get(word)));
				break;
			case "CyanToMagenta":
				color = new Color(lerp(0x00ffff, 0xff00ff, normFreq.get(word)));
				break;
			case "Black":
				color = new Color(0x000000);
				break;
			default:
				color = new Color(0x000000);
				break;
			}
			
			// Draw text
			g2.setFont(rotatedFont);
			g2.setColor(color);
			g2.drawString(word, rectX, rectY);
		}
		
		g2.dispose();
		return img;
	}
	
	/**
	 * Linear interpolation
	 * @param colorA an integer representing a colour
	 * @param colorB an integer representing a colour
	 * @param i how far between the two colours to interpolate
	 * @return an integer representing the interpolated colour
	 */
	private static int lerp(int colorA, int colorB, double i) {
		int r1 = (colorA >> 16) & 0xff;
        int r2 = (colorB >> 16) & 0xff;
        int g1 = (colorA >> 8) & 0xff;
        int g2 = (colorB >> 8) & 0xff;
        int b1 = colorA & 0xff;
        int b2 = colorB & 0xff;
        return (int) ((r2 - r1) * i + r1) << 16 | (int) ((g2 - g1) * i + g1) << 8 | (int) ((b2 - b1) * i + b1);
	}
	
	/**
	 * Gets a random integer in a given range (inclusive)
	 * @param l lower limit
	 * @param h upper limit
	 * @return a random integer in the given range
	 */
	private static int randIntInRange(double l, double h) {
		int min, max;
		if (l < h) {
			max = (int) Math.round(h);
			min = (int) Math.round(l);
		} else {
			min = (int) Math.round(h);
			max = (int) Math.round(l);
		}
		return rng.nextInt((max - min) + 1) + min;
	}
	
	/**
	 * Cleans the word string
	 * @param u the un-cleaned word string
	 * @return the cleaned word string
	 */
	private static String getCleanWordString(String u) {
		String cleaned = new String();
		
		for (int i = 0; i < u.length(); i++) {
			if (Character.isLetter(u.charAt(i))) {
				cleaned += Character.toLowerCase(u.charAt(i));
			} else if (Character.isWhitespace(u.charAt(i))) {
				cleaned += u.charAt(i);
			}
		}
		
		return cleaned;
	}
	
	/**
	 * Creates the panel that contains the copyright information
	 */
	private static void createCopyrightPanel() {
		
		// Copyright notice
		copyrightLabel = new JLabel();
		copyrightLabel.setText("<html><div style='text-align: center;'>Cloudy Words is &copy; Copyright Pryce-Jones Systems 2021. Released under GNU GPL 3.0.</div></html>");
		
		copyrightPanel = new JPanel();
		copyrightPanel.setLayout(new BorderLayout());
		copyrightPanel.add(new JSeparator(), BorderLayout.NORTH);
		copyrightPanel.add(copyrightLabel, BorderLayout.SOUTH);
	}
	
	/**
	 * Creates the panel that contains the text input stuff
	 */
	private static void createInputPanel() {
		
		// Label
		inputTextLabel = new JLabel();
		inputTextLabel.setText("<html>Type your words here</html>");
		
		// Box that user types words into
		wordInputBox = new JTextArea();
		wordInputBox.setSize(400, 400);
		wordInputBox.setLineWrap(true);
		wordInputBox.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
				updateWordCloud();
			}
			public void keyReleased(KeyEvent arg0) {
				updateWordCloud();
			}
			public void keyTyped(KeyEvent arg0) {
				updateWordCloud();
			}
			
		});
		textBoxScrollArea = new JScrollPane(wordInputBox);
		textBoxScrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		textBoxScrollArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// Panel that contains controls
		textInputPanel = new JPanel();
		textInputPanel.setLayout(new BorderLayout());
		textInputPanel.add(inputTextLabel, BorderLayout.NORTH);
		textInputPanel.add(textBoxScrollArea, BorderLayout.CENTER);
	}
	
	/**
	 * Creates the panel that contains the controls (buttons and sliders)
	 */
	private static void createControlPanel() {
		
		// Button to open a text file
		openButton = new JButton();
		openButton.setText("Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser openDialogue = new JFileChooser();
				openDialogue.setFileFilter(new FileFilter() {
					public String getDescription() {
						return "Text files (.txt)";
					}
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						String name = f.getName().toLowerCase();
						return name.endsWith(".txt"); 
					}
				});
				openDialogue.setDialogTitle("Open");
				int selection = openDialogue.showSaveDialog(new JFrame());
				if (selection == JFileChooser.APPROVE_OPTION) {
					try {
						File f = openDialogue.getSelectedFile();
						Scanner fileScanner = new Scanner(f);
						String fileText = new String();
						while (fileScanner.hasNextLine()) {
							String curLine = fileScanner.nextLine();
							fileText += curLine;
						}
						fileScanner.close();
						
						// Put into input box
						wordInputBox.setText(fileText);
						updateWordCloud();
					} catch (IOException e) { }
				}
			}
		});
		
		// Button to save word cloud
		saveButton = new JButton();
		saveButton.setText("Save");
		saveButton.setEnabled(false);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser saveAsDialogue = new JFileChooser();
				saveAsDialogue.setFileFilter(new FileFilter() {
					public String getDescription() {
						return "Portable Network Graphics (.png)";
					}
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						String name = f.getName().toLowerCase();
						return name.endsWith(".png"); 
					}
				});
				saveAsDialogue.setDialogTitle("Save As");
				int selection = saveAsDialogue.showSaveDialog(new JFrame());
				if (selection == JFileChooser.APPROVE_OPTION) {
					File f = saveAsDialogue.getSelectedFile();
					try {
						ImageIO.write(imageBuffer, "png", f);
					} catch (IOException e) { }
				}
			}
		});
		
		// Slider to control image size
		sizeSlider = new JSlider(JSlider.HORIZONTAL, 500, 10000, 1000);
		sizeSlider.setMajorTickSpacing(500);
		sizeSlider.setMinorTickSpacing(250);
		sizeSlider.setSnapToTicks(true);
		sizeSlider.setPaintTicks(true);
		sizeSlider.setPaintLabels(false);
		sliderLabel = new JLabel();
		sliderLabel.setText(String.format("<html>Image size<br/>%dx%d px</html>", sizeSlider.getValue(), sizeSlider.getValue()));
		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				sliderLabel.setText(String.format("<html>Image size<br/>%dx%d px</html>", sizeSlider.getValue(), sizeSlider.getValue()));		
				updateWordCloud();
			}
		});
		
		// Text colour controls
		colorMode = "Pastel";
		colorLabel = new JLabel();
		colorLabel.setText("<html>Text colour</html>");
		pastelColorButton = new JRadioButton();
		pastelColorButton.setText("Pastel");
		pastelColorButton.setSelected(true);
		pastelColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				colorMode = "Pastel";
				updateWordCloud();
			}
		});
		blueToRedColorButton = new JRadioButton();
		blueToRedColorButton.setText("Blue to red");
		blueToRedColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				colorMode = "BlueToRed";
				updateWordCloud();
			}
		});
		cyanToMagentaColorButton = new JRadioButton();
		cyanToMagentaColorButton.setText("Cyan to magenta");
		cyanToMagentaColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				colorMode = "CyanToMagenta";
				updateWordCloud();
			}
		});
		blackColorButton = new JRadioButton();
		blackColorButton.setText("Black");
		blackColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				colorMode = "Black";
				updateWordCloud();
			}
		});
		colorRadioButtons = new ButtonGroup();
		colorRadioButtons.add(pastelColorButton);
		colorRadioButtons.add(blueToRedColorButton);
		colorRadioButtons.add(cyanToMagentaColorButton);
		colorRadioButtons.add(blackColorButton);
		colorRadioButtonPanel = new JPanel();
		colorRadioButtonPanel.setLayout(new GridLayout(4, 1));
		colorRadioButtonPanel.add(pastelColorButton);
		colorRadioButtonPanel.add(blueToRedColorButton);
		colorRadioButtonPanel.add(cyanToMagentaColorButton);
		colorRadioButtonPanel.add(blackColorButton);
		
		// Panel that contains the buttons
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(5, 2));
		controlPanel.add(openButton);
		controlPanel.add(saveButton);
		controlPanel.add(sliderLabel);
		controlPanel.add(sizeSlider);
		controlPanel.add(colorLabel);
		controlPanel.add(colorRadioButtonPanel);
	}
	
	/**
	 * Creates the panel that contains the word cloud image
	 */
	private static void createImagePanel() {
		
		// Label
		imageTextLabel = new JLabel();
		imageTextLabel.setText("<html>Start typing...</html>");
		
		// Image
		wordCloudImage = new JLabel();		
		
		// Panel that contains word cloud
		imagePanel = new JPanel();
		imagePanel.setLayout(new BorderLayout());
		imagePanel.add(imageTextLabel, BorderLayout.NORTH);
		imagePanel.add(wordCloudImage, BorderLayout.CENTER);
		
		// The image of the word cloud
		try {
			imageBuffer = ImageIO.read(new File(WordCloudFrame.class.getResource("pjs-logo.jpg").toURI()));
			ImageIcon icon = new ImageIcon(imageBuffer.getScaledInstance(400, 400, Image.SCALE_SMOOTH));
			wordCloudImage.setIcon(icon);
		} catch (Exception e) {	}
	}
}

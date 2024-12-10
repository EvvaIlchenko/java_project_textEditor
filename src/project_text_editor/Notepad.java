package project_text_editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtilities;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

/**
 * Головний клас програми "Блокнот".
 * Реалізує текстовий редактор із функціями аналізу тексту,
 * збереження у PDF, пошуку за регулярними виразами та іншими можливостями.
 */

public class Notepad extends JFrame implements ActionListener{
	// Основні елементи інтерфейсу користувача
	private final RSyntaxTextArea textArea; // Текстова область з підтримкою підсвічування синтаксису
	private final JScrollPane scrollPane; // Скролнер для текстової області
	private final JLabel fontLabel; // Мітка для вибору шрифту
	private final JSpinner fontSizeSpinner; // Спінер для вибору розміру шрифту
	private final JButton fontColorButton, analyzeButton; // Кнопки для зміни кольору тексту та аналізу тексту
	private final JComboBox<String> fontBox; // Випадаючий список для вибору шрифту
	
	// Елементи меню
	JMenuBar menuBar;
	JMenu fileMenu;
	JMenuItem openItem, saveItem, exitItem;
	JMenuItem regexSearchItem;
	JMenuItem saveAsRTFItem, saveAsPDFItem;
	
	/**
     * Конструктор класу Notepad.
     * Ініціалізує інтерфейс користувача та налаштовує події.
     */

	private Notepad(){
		
		// Налаштування основного вікна програми
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("text editor");
		this.setSize(500, 500);
		this.setLayout(new FlowLayout());
		this.setLocationRelativeTo(null);

		// Налаштування RSyntaxTextArea
        textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // Установка стилю підсвічування для Java
        textArea.setCodeFoldingEnabled(true); // Увімкнення згортання коду
        textArea.setAntiAliasingEnabled(true); // Покращений вигляд тексту
	    textArea.setPreferredSize(new Dimension(450, 450));
	    textArea.setLineWrap(true);
	    textArea.setWrapStyleWord(true);

	    // Додавання скролнера до текстової області
	    scrollPane = new RTextScrollPane(textArea); 
        this.add(scrollPane, BorderLayout.CENTER);
	    scrollPane.setPreferredSize(new Dimension(450, 450));
	    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	    // Ініціалізація елементів керування шрифтом
	    fontLabel = new JLabel("Font: ");
		fontSizeSpinner = new JSpinner();
	    fontSizeSpinner.setPreferredSize(new Dimension(50,25));
		fontSizeSpinner.setValue(20); // Початковий розмір шрифту
		fontSizeSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
	            textArea.setFont(new Font(textArea.getFont().getFamily(), Font.PLAIN, (int) fontSizeSpinner.getValue()));
	        }
	    });

		fontColorButton = new JButton("Color");
		fontColorButton.addActionListener(this); // Слухач подій для вибору кольору тексту

		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		fontBox = new JComboBox<>(fonts);
		fontBox.addActionListener(this); // Слухач подій для вибору шрифту
		fontBox.setSelectedItem("Arial");

		analyzeButton = new JButton("Analyze"); // Створення кнопки аналізу тексту
	    analyzeButton.addActionListener(this);  // Додавання слухача дій до analyzeButton 

	        // Меню програми
			menuBar = new JMenuBar();
			fileMenu = new JMenu("File");
			openItem = new JMenuItem("Open");
			saveItem = new JMenuItem("Save");
			exitItem = new JMenuItem("Exit");
			regexSearchItem = new JMenuItem("Search by Regex");
			saveAsPDFItem = new JMenuItem("Save as PDF");

			openItem.addActionListener(this); // Слухач подій для відкриття файлів
			saveItem.addActionListener(this); // Слухач подій для збереження файлів
			exitItem.addActionListener(this); // Слухач подій для виходу з програми
			regexSearchItem.addActionListener(this); // Слухач подій для пошуку за регулярними виразами
			saveAsPDFItem.addActionListener(this); // Слухач подій для збереження у PDF


			fileMenu.add(openItem);
			fileMenu.add(saveItem);
			fileMenu.add(regexSearchItem);
			fileMenu.add(exitItem);
			fileMenu.add(saveAsPDFItem);

			menuBar.add(fileMenu);
		    this.setJMenuBar(menuBar);

		 // Додавання елементів на головне вікно
		this.add(fontLabel);
		this.add(fontSizeSpinner);
		this.add(fontColorButton);
		this.add(fontBox);
		this.add(analyzeButton);
		this.add(scrollPane);
		this.setVisible(true); // Відображення вікна
	}
	
	/**
     * Обробка подій від елементів інтерфейсу.
     * * @param e Об'єкт події
     */

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == fontColorButton) {

			Color color = JColorChooser.showDialog(null, "Choose a color", Color.black);
		    textArea.setForeground(color); // Зміна кольору тексту
		}

		if(e.getSource() ==fontBox) {
			textArea.setFont(new Font((String)fontBox.getSelectedItem(),Font.PLAIN, textArea.getFont().getSize()));
		}

		if (e.getSource() == analyzeButton) {
            analyzeText(); // Виклик аналізу тексту
        }
		if (e.getSource() == regexSearchItem) {
		    searchByRegex(); // Виклик методу пошуку за регулярними виразами
		}
		if (e.getSource() == saveAsPDFItem) {
		        saveAsPDF(); // Виклик збереження у PDF
		}


		// Відкриття файлу
		if(e.getSource() == openItem) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
			
			 // Фільтр файлів для відображення тільки текстових файлів (*.txt)
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
			fileChooser.setFileFilter(filter);

			// Відображення діалогового вікна для вибору файлу та отримання відповіді користувача
			int response = fileChooser.showOpenDialog(null);

			// Перевірка, чи користувач підтвердив вибір файлу
			if(response == JFileChooser.APPROVE_OPTION) {
				File file = new File(fileChooser.getSelectedFile().getAbsolutePath());

				// Читання вмісту файлу та відображення його у текстовій області
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    textArea.read(br, null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
		
		// Збереження файлу
		if(e.getSource() == saveItem) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));

			// Відображення діалогового вікна для вибору місця збереження
			int response = fileChooser.showSaveDialog(null);

			// Перевірка, чи користувач підтвердив вибір місця збереження
			if(response == JFileChooser.APPROVE_OPTION) {
				// Отримання шляху до файлу
				File file = new File(fileChooser.getSelectedFile().getAbsolutePath());

				// Запис тексту у файл
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    textArea.write(bw);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
		
		// Вихід із програми
		if(e.getSource() == exitItem) {
			System.exit(0);

		}
	}

	// Метод збереження тексту як PDF
	private void saveAsPDF() {
		JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setCurrentDirectory(new File("."));
	    
	 // Фільтр файлів для відображення тільки PDF-файлів
	    fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

	    int response = fileChooser.showSaveDialog(null);

	    if (response == JFileChooser.APPROVE_OPTION) {
	    	
	    	// Додавання розширення ".pdf" до імені файлу
	    	File file = new File(fileChooser.getSelectedFile().getAbsolutePath() + ".pdf");

	    	// Створення PDF-документа та запис тексту у нього
	    	try (PdfWriter pdfWriter = new PdfWriter(file);
	             Document document = new Document(
	                     new PdfDocument(pdfWriter))) {

	            String text = textArea.getText();  // Отримання тексту з текстової області
	            Paragraph paragraph = new Paragraph(text);
	            document.add(paragraph);

	            JOptionPane.showMessageDialog(this, "File saved as PDF: " + file.getAbsolutePath());
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            JOptionPane.showMessageDialog(this, "Error saving file as PDF.", "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }

	}
	
	// Метод для створення папки, якщо її ще не існує
	private void createDirectoryIfNotExists(String folderPath) {
		
		// Створення об'єкта File для вказаного шляху
		File directory = new File(folderPath);
	    if (!directory.exists()) {
	        directory.mkdirs(); // Створює всі відсутні папки в шляху
	    }
	}


	// Метод аналізу тексту
	private void analyzeText() {
        String text = textArea.getText();


     // Підрахунок частот літер
        int[] letterFrequencies = new int[26];
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letterFrequencies[Character.toLowerCase(c) - 'a']++;
            }
        }

     // Підрахунок частот слів
        String[] words = text.split("\\s+");
        java.util.Map<String, Integer> wordFrequencies = new java.util.HashMap<>();
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (!word.isEmpty()) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
            }
        }

     // Створення текстового звіту
        createReport(text, letterFrequencies, wordFrequencies);

     // Побудова гістограми літер
        DefaultCategoryDataset letterDataset = new DefaultCategoryDataset();
        for (int i = 0; i < 26; i++) {
            if (letterFrequencies[i] > 0) {
                char letter = (char) ('a' + i);
                letterDataset.addValue(letterFrequencies[i], "Frequency", String.valueOf(letter));
            }
        }
        JFreeChart letterChart = ChartFactory.createBarChart(
            "Letter Frequency", "Letters", "Frequency", letterDataset);
        saveChartAsImage(letterChart, "letter_histogram.png");

        // Побудова гістограми слів
        DefaultCategoryDataset wordDataset = new DefaultCategoryDataset();
        wordFrequencies.entrySet().stream()
            .sorted(java.util.Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10) // Виводимо топ-10 слів
            .forEach(entry -> wordDataset.addValue(entry.getValue(), "Frequency", entry.getKey()));
        JFreeChart wordChart = ChartFactory.createBarChart(
            "Word Frequency", "Words", "Frequency", wordDataset);
        saveChartAsImage(wordChart, "word_histogram.png");

        JOptionPane.showMessageDialog(this, "Analysis complete. Histograms saved as images.",
            "Analysis Complete", JOptionPane.INFORMATION_MESSAGE);
    }
	
	// Метод для збереження графіка у файл
	private void saveChartAsImage(JFreeChart chart, String fileName) {
		String folderPath = "analysis_results"; // Назва папки для збереження
	    createDirectoryIfNotExists(folderPath); // Перевірка існування/створення папки
	    
	    File file = new File(folderPath + File.separator + fileName);

	    // Збереження графіка у форматі PNG
	    try {
	        ChartUtilities.saveChartAsPNG(file, chart, 800, 600);
	        System.out.println("Saved chart: " + file.getAbsolutePath());
	    } catch (IOException e) {
	        e.printStackTrace(); // Виведення стека помилок у разі виникнення виключення
	    }
	}

	// Метод для створення текстового звіту про аналіз
	private void createReport(String text, int[] letterFrequencies, java.util.Map<String, Integer> wordFrequencies) {
		int vowels = 0; // Лічильник для голосних літер
		int specialChars = 0; // Лічильник для спеціальних символів
		String folderPath = "analysis_results"; // Назва папки для збереження
	    createDirectoryIfNotExists(folderPath); // Перевірка існування/створення папки
	    
	    // Підрахунок кількості голосних та спеціальних символів у тексті
	    for (char c: text.toCharArray()) {
	    	
	    	// Перевірка на голосну літеру
	    	if("AEIOUaeiou".indexOf(c) != -1) {
				vowels++;
			} 
	    	
	    	// Перевірка на спеціальний символ
	    	else if(!Character.isLetterOrDigit(c)&&!Character.isWhitespace(c)){
				specialChars++;
			}
		}
		
	    // Створення файлу для збереження звіту
	    File reportFile = new File(folderPath + File.separator + "text_analysis_report.txt");
	 
	    // Запис звіту у файл
        try (PrintWriter writer = new PrintWriter(reportFile)) {
            writer.println("Text Analysis Report:");
            writer.println("Number of vowels: " + vowels);
            writer.println("Number of special characters: " + specialChars);
            writer.println("Original Text: " + text);

         // Запис частот літер
            writer.println("\nLetter Frequency:");
            for (int i = 0; i < 26; i++) {
                char letter = (char) ('a' + i);
                writer.println(letter + ": " + letterFrequencies[i]);
            }

         // Запис частот слів
            writer.println("\nWord Frequency:");
            wordFrequencies.entrySet().stream()
            
         // Сортування за частотою
                .sorted(java.util.Map.Entry.<String, Integer>comparingByValue().reversed())
         // Виведення слова та його частоти
                .forEach(entry -> writer.println(entry.getKey() + ": " + entry.getValue()));


            writer.flush(); // Очищення потоку
            JOptionPane.showMessageDialog(this, "Analysis report saved to " + reportFile.getAbsolutePath(),
                    "Analysis Complete", JOptionPane.INFORMATION_MESSAGE);  // Повідомлення про успішне збереження звіту
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // Обробка виключення у разі помилки доступу до файлу
        }
	}

	// Метод для пошуку у тексті за регулярним виразом
	private void searchByRegex() {
		
		 // Запит у користувача регулярного виразу через діалогове вікно
        String regex = JOptionPane.showInputDialog(this, "Enter a regular expression:", "Search by Regex", JOptionPane.PLAIN_MESSAGE);
        
        // Перевірка, чи користувач ввів регулярний вираз
        if (regex != null && !regex.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(regex); // Компіляція регулярного виразу
                Matcher matcher = pattern.matcher(textArea.getText()); // Пошук відповідностей у тексті

                StringBuilder results = new StringBuilder("Matches found:\n"); // Результати пошуку
                int matchCount = 0; // Лічильник знайдених відповідностей

             // Перебір усіх відповідностей
                while (matcher.find()) {
                    matchCount++;
                    results.append("Match ").append(matchCount).append(": ").append(matcher.group())
                    		.append(" at position ").append(matcher.start()).append("\n");  // Додавання інформації про відповідність
                }

             // Якщо відповідності не знайдено
                if (matchCount == 0) {
                    results.append("No matches found.");
                }

             // Відображення результатів пошуку
                JOptionPane.showMessageDialog(this, results.toString(), "Search Results", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
            	
            	// Обробка помилки у разі некоректного регулярного виразу
                JOptionPane.showMessageDialog(this, "Invalid regular expression.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


	// Основний метод для запуску програми
	public static void main(String[] args) {
		SwingUtilities.invokeLater(Notepad::new); // Запуск графічного інтерфейсу у потоці Swing

	}

	}





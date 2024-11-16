import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Objects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;


public class ImageToPdfConverter extends JFrame {
    private JTextField folderField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private String authorName = "Noor Elahi Ali Shibly";

    // Pattern to match numbers in filename
    private static final Pattern NUMBER_PATTERN = Pattern.compile("output_(\\d+)\\.(?:jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE);

    //Code for make it portable friendly (1)
    private static final Path APP_PATH = resolveApplicationPath();
    private static final Path CONFIG_PATH = APP_PATH.resolve("config");
    private static final Path OUTPUT_PATH = APP_PATH.resolve("output");

    private static Path resolveApplicationPath() {
        try {
            // Get the directory where the application is running from
            Path appPath = Paths.get(ImageToPdfConverter.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent();

            // Create necessary directories
            Files.createDirectories(appPath.resolve("config"));
            Files.createDirectories(appPath.resolve("output"));

            return appPath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize application directories", e);
        }
    }

    // Modify your existing file operations to use these paths
//    private void browseFolder() {
//        JFileChooser fileChooser = new JFileChooser(OUTPUT_PATH.toFile());
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        int option = fileChooser.showOpenDialog(this);
//        if (option == JFileChooser.APPROVE_OPTION) {
//            folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
//        }
//    }

    private void saveSettings() {
        try {
            Properties props = new Properties();
            props.setProperty("last_folder", folderField.getText());
            props.setProperty("author_name", authorName);

            Path configFile = CONFIG_PATH.resolve("settings.properties");
            try (OutputStream out = Files.newOutputStream(configFile)) {
                props.store(out, "ImageToPdfConverter Settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        try {
            Path configFile = CONFIG_PATH.resolve("settings.properties");
            if (Files.exists(configFile)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(configFile)) {
                    props.load(in);
                }
                folderField.setText(props.getProperty("last_folder", ""));
                authorName = props.getProperty("author_name", authorName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Main code start
    public ImageToPdfConverter() {
        setTitle("Image to PDF Converter");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set custom icon using classpath resource
        try {
            // Load icon from classpath resources
            BufferedImage icon = ImageIO.read(Objects.requireNonNull(
                    getClass().getClassLoader().getResourceAsStream("icon.png"),
                    "Icon resource not found in classpath"
            ));
            setIconImage(icon);
        } catch (IOException | NullPointerException e) {
            System.err.println("Icon could not be loaded: " + e.getMessage());
        }

        // Top Panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        folderField = new JTextField(25);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> browseFolder());

        topPanel.add(new JLabel("Folder Path:"));
        topPanel.add(folderField);
        topPanel.add(browseButton);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1, 10, 10));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        JButton convertButton = new JButton("Convert to PDF");
        convertButton.addActionListener(e -> convertToPdf());

        centerPanel.add(progressBar);
        centerPanel.add(convertButton);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Status: Ready");
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        JLabel footerLabel = new JLabel(authorName);
        footerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        bottomPanel.add(footerLabel, BorderLayout.SOUTH);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Modify your existing file operations to use these paths
    private void browseFolder() {
        JFileChooser fileChooser = new JFileChooser(OUTPUT_PATH.toFile());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void convertToPdf() {
        String folderPath = folderField.getText();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid folder path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png)"));
        if (imageFiles == null || imageFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No images found in the folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // New sorting implementation
        Arrays.sort(imageFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                Matcher m1 = NUMBER_PATTERN.matcher(f1.getName());
                Matcher m2 = NUMBER_PATTERN.matcher(f2.getName());

                // If both filenames match our expected pattern
                if (m1.find() && m2.find()) {
                    try {
                        // Extract and compare the numbers
                        int num1 = Integer.parseInt(m1.group(1));
                        int num2 = Integer.parseInt(m2.group(1));
                        return Integer.compare(num1, num2);
                    } catch (NumberFormatException e) {
                        // Fallback to string comparison if number parsing fails
                        return f1.getName().compareTo(f2.getName());
                    }
                }
                // If one or both don't match the pattern, fall back to string comparison
                return f1.getName().compareTo(f2.getName());
            }
        });

        progressBar.setValue(0);
        progressBar.setMaximum(imageFiles.length);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                PDDocument document = new PDDocument();
                int count = 0;

                // Print sorted filenames for verification (can be removed in production)
                System.out.println("Processing files in order:");
                for (File f : imageFiles) {
                    System.out.println(f.getName());
                }

                for (File imageFile : imageFiles) {
                    try {
                        PDImageXObject image = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), document);
                        PDPage page = new PDPage(new org.apache.pdfbox.pdmodel.common.PDRectangle(image.getWidth(), image.getHeight()));
                        document.addPage(page);

                        PDPageContentStream contentStream = new PDPageContentStream(document, page);
                        contentStream.drawImage(image, 0, 0);

                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                        contentStream.newLineAtOffset(10, 10);
                        contentStream.showText(authorName + " | '" + imageFile.getName() + "'");
                        contentStream.endText();

                        contentStream.close();

                        count++;
                        final int currentCount = count;
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(currentCount);
                            statusLabel.setText("Processing: " + imageFile.getName());
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        final String errorFile = imageFile.getName();
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Error processing: " + errorFile);
                        });
                    }
                }


//                String outputPath = folderPath + File.separator + "output.pdf";
//                document.save(outputPath);
//                document.close();



                // Save the PDF
                final String[] outputPath = new String[1]; // Use an array to allow modification inside the lambda

                // Show file save dialog
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save PDF File");
                fileChooser.setSelectedFile(new File("output.pdf")); // Default file name

                int userSelection = fileChooser.showSaveDialog(null);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    outputPath[0] = fileToSave.getAbsolutePath();

                    // Ensure the file has a .pdf extension
                    if (!outputPath[0].endsWith(".pdf")) {
                        outputPath[0] += ".pdf";
                    }

                    // Save the document
                    try {
                        document.save(outputPath[0]);
                        document.close();
                        JOptionPane.showMessageDialog(null, "PDF saved successfully at: " + outputPath[0], "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to save the PDF file.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Save operation was cancelled.", "Cancelled", JOptionPane.WARNING_MESSAGE);
                }

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("PDF saved to: " + outputPath[0]);
                });

                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(ImageToPdfConverter.this, "Conversion complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageToPdfConverter converter = new ImageToPdfConverter();
            converter.setVisible(true);
        });
    }
}
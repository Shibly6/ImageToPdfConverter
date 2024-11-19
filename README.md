# Image to PDF Converter

The **Image to PDF Converter** is a user-friendly Java-based desktop application that converts images (JPG, JPEG, PNG) from a folder into a single PDF file. It is designed with portability in mind and includes advanced features like image sorting, custom annotations, and persistent settings for a seamless user experience.

---

## Features

- Convert multiple images into a single PDF file.
- Supports image formats: JPG, JPEG, PNG.
- Sorts images based on numerical values in filenames (e.g., `output_1.jpg`, `output_2.jpg`).
- Adds custom annotations (e.g., author name) to the PDF.
- Saves settings persistently for later use (e.g., last folder used, author name).
- Portable application with minimal external dependencies.
- Intuitive and user-friendly GUI.

---

## Requirements

- **Java**: JDK 17 or higher.
- Libraries used:
  - `Apache PDFBox`: For PDF creation.
  - `Swing`: For GUI development.
- Optional: Java-compatible IDE (e.g., IntelliJ IDEA, Eclipse) for development.

---

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/shibly6/ImageToPdfConverter.git
   cd ImageToPdfConverter/build
   java -jar ImageToPdfConverter.jar   

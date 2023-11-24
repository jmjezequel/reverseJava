package fr.irisa.diverse.plantUml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.sourceforge.plantuml.SourceStringReader;

public class UmlViewer extends ModelBuilder {

    public UmlViewer(String... globalOptions) {
        super(globalOptions);
    }

    public void generateUMLasPNG(String filename) {
        String model = getDiagramSpec();
        try (FileOutputStream png = new FileOutputStream(new File(filename))) {
            SourceStringReader reader = new SourceStringReader(model);
            // Write the first image to "png"
            String desc = reader.outputImage(png).getDescription();
            System.out.println("Wrote for " + filename + " :" + desc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewPNG(String filename) {
        JFrame frame = new JFrame(filename);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon(filename);

        JLabel label = new JLabel(icon);

        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(scrollPane);
        frame.setSize(800, 600); // Set an initial size
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0] == "-h" || args[0] == "--help") {
            System.out.println("Syntax : java UmnViewer file.jar option1 option2 ...");
        } else {
            String jarfile = args[0];
            args[0] = "hide circle";
            UmlViewer uv = new UmlViewer(args);
            uv.addJar(1, 1, jarfile);
            Path tmp = Files.createTempFile(null, ".png");
            uv.generateUMLasPNG(tmp.toString());
            uv.viewPNG(tmp.toString());
        }
    }
}

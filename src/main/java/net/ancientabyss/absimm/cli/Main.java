package net.ancientabyss.absimm.cli;

import net.ancientabyss.absimm.core.Loader;
import net.ancientabyss.absimm.core.ReactionClient;
import net.ancientabyss.absimm.core.Story;
import net.ancientabyss.absimm.core.StoryException;
import net.ancientabyss.absimm.parser.Parser;
import net.ancientabyss.absimm.parser.TxtParser;
import net.ancientabyss.absimm.parser.XmlParser;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;

public class Main implements ReactionClient {

    public static void main(String[] args) {
        Main main = new Main();
        Story story = main.init(args);
        main.run(story);
    }

    private void run(Story story) {
        if (story == null) return;
        try {
            story.tell();
        } catch (StoryException e) {
            System.err.println("Failed telling story: " + e.getMessage());
        }
        while (true) {
            String user_input = System.console().readLine();
            if (user_input.equals(story.getSettings().getSetting("quit_command"))) break;

            try {
                story.interact(user_input);
            } catch (StoryException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private Story init(String[] args) {
        System.out.println("Welcome to Absimm-Cli!");
        if (args.length < 1) {
            printUsage();
            return null;
        }
        if (System.console() == null) {
            System.err.println("Failed getting a console.");
            return null;
        }
        Story story;
        try {
            story = new Loader(getParser(getFileExtension(args[0]))).fromFile(args[0]);
        } catch (StoryException | NotSupportedException e) {
            System.err.println("Failed loading story: " + e.getMessage());
            return null;
        }
        story.addClient(this);
        return story;
    }

    private static void printUsage() {
        System.out.println("Usage: absimm-cli <storyfile>");
    }

    @Override
    public void reaction(String text) {
        String[] lines = text.split("\\\\n");
        for (String line : lines) System.out.println("" + WordUtils.wrap(line.trim(), 80, "\n", true));
    }

    private Parser getParser(String extension) throws NotSupportedException {
        HashMap<String, Parser> parsers = new HashMap<>();
        parsers.put("txt", new TxtParser());
        parsers.put("xml", new XmlParser());
        String cleanedUpExtension = extension.toLowerCase();
        if (parsers.containsKey(cleanedUpExtension)) {
            return parsers.get(cleanedUpExtension);
        }
        throw new NotSupportedException();
    }

    private String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }
}

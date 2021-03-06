
package alda.repl.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.FileAlreadyExistsException;

import java.io.IOException;
import java.io.UncheckedIOException;

import alda.AldaServer;
import alda.Util;
import alda.AldaResponse.AldaScore;
import java.util.function.Consumer;
import jline.console.ConsoleReader;

/**
 * Handles the :save command
 * Takes an argument of where to save to. This argument can be omitted if we have saved to a location before.
 * When we have saved to somewhere before, we will not prompt for overwrite.
 */
public class ReplSave implements ReplCommand {
  private ReplCommandManager cmdManager;

  public ReplSave(ReplCommandManager m) {
    cmdManager = m;
  }

  private String oldSaveFile() {
    return cmdManager.getSaveFile();
  }
  private void setOldSaveFile(String s) {
    cmdManager.setSaveFile(s);
  }

  @Override
  public void act(String args, StringBuffer history, AldaServer server,
                  ConsoleReader reader, Consumer<AldaScore> newInstrument) {
    // Turn ~ into home
    args = args.replaceFirst("^~",System.getProperty("user.home"));
    try {
      if (args.length() == 0) {
        if (oldSaveFile() != null && oldSaveFile().length() != 0) {
          // Overwrite by default if running :save
          Files.write(Paths.get(oldSaveFile()), history.toString().getBytes());
        } else {
          usage();
        }
        return;
      }

      try {
        Files.write(Paths.get(args), history.toString().getBytes(),
                    StandardOpenOption.CREATE_NEW);
        setOldSaveFile(args);
      } catch (FileAlreadyExistsException e) {
        if (Util.promptWithChoices(reader,
                                            "File already present, overwrite?",
                                            "yes", "no").equals("yes")) {
          Files.write(Paths.get(args), history.toString().getBytes(),
                      StandardOpenOption.CREATE,
                      StandardOpenOption.TRUNCATE_EXISTING);
          setOldSaveFile(args);
        }
      }
    } catch (IOException|UncheckedIOException e) {
      e.printStackTrace();
      System.err.println("There was an error writing to '" + args + "'");
    }
  }
  @Override
  public String docSummary() {
    return "Saves the current REPL session as an Alda score file.";
  }
  @Override
  public String docDetails() {
    return "Usage:\n\n" +
      "  :save test/examples/bach_cello_suite_no_1.alda\n" +
      "  :save ~/Scores/love_is_alright_tonite.alda\n\n" +
      "Once :save/:load has been executed once:\n" +
      "  :save";
  }
  @Override
  public String key() {
    return "save";
  }
}

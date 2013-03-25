package uk.ac.susx.tag.wag;

import java.io.PrintWriter;

/**
 * @author Hamish Morgan
 */
public class WriteToStringAliasHandler implements AliasHandler {


    private final PrintWriter out;

    public WriteToStringAliasHandler(final PrintWriter out) {
        this.out = out;
    }

    public WriteToStringAliasHandler() {
        this(new PrintWriter(System.out, true));
    }

    @Override
    public void handle(Alias alias) {
        out.println(alias.toString());
    }
}

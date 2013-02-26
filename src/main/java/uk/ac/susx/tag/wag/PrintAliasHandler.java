package uk.ac.susx.tag.wag;

import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 22/02/2013
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public class PrintAliasHandler implements AliasHandler {

    public enum Format {
        TOSTRING() {
            @Override
            public void format(Alias alias, PrintWriter out) {
                out.println(alias.toString());
            }
        },
        TSV() {
            @Override
            public void format(Alias alias, PrintWriter out) {
                out.print(alias.getType());
                out.print("\t");
                out.print(alias.getSubType());
                out.print("\t");
                out.print(alias.getSource());
                out.print("\t");
                out.print(alias.getTarget());
                out.println();
            }
        },
        CSV() {
            @Override
            public void format(Alias alias, PrintWriter out) {
                out.print(alias.getType());
                out.print(", ");
                out.print(alias.getSubType());
                out.print(", ");
                out.print(alias.getSource());
                out.print(", ");
                out.print(alias.getTarget());
                out.println();
            }
        };


        public abstract void format(Alias alias, PrintWriter dest);
    }

    private final PrintWriter out;
    private final Format format;

    public PrintAliasHandler(final PrintWriter out, final Format format) {
        this.out = out;
        this.format = format;
    }

    public PrintAliasHandler() {
        this(new PrintWriter(System.out, true), Format.TSV);
    }

    @Override
    public void handle(Alias alias) {
        format.format(alias, out);
    }
}

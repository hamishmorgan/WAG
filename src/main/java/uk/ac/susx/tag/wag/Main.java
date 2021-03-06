package uk.ac.susx.tag.wag;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.BaseConverter;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.io.*;
import uk.ac.susx.tag.util.IOUtils;
import uk.ac.susx.tag.util.StringConverterFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 * @since 18/02/2013 14:33
 */
public class Main {

    private enum OutputFormat {
        /**
         * Produce standard Tab-Separated-Values format. Values which contains tabs or new-line characters, are quoted.
         * Internal quotes are escaped.
         */
        TSV,

        /**
         * Produce a simplified Tab-Separated-Values format. White-space within values and condensed and replaced with
         * a single space character. Trailing and leading space is removed. The net result is that the output file
         * sound never require quoted strings.
         */
        TSV_SIMPLIFIED,

        /**
         * Produce standard Comma-Separated-Values format. Values which contain a comma or new-line characters, are
         * quoted. Internal quotes are escaped.
         */
        CSV
    }

    private final List<ByteSource> sources;
    private final CharSink sink;
    private final EnumSet<AliasType> producedTypes;
    private final int pageLimit;
    private final boolean produceIdentityAliases;
    private final OutputFormat outputFormat;
    private final EnumSet<WriteTabulatedAliasHandler.Column> outputColumns;

    /**
     * Private constructor. Use the builder to instantiate: {@link #builder()}.
     *
     * @param sources
     * @param sink
     * @param producedTypes
     * @param pageLimit
     * @param produceIdentityAliases
     */
    private Main(List<ByteSource> sources, CharSink sink,
                 EnumSet<AliasType> producedTypes, int pageLimit, boolean produceIdentityAliases,
                 OutputFormat outputFormat, EnumSet<WriteTabulatedAliasHandler.Column> outputColumns) {
        this.sources = sources;
        this.sink = sink;
        this.producedTypes = producedTypes;
        this.pageLimit = pageLimit;
        this.produceIdentityAliases = produceIdentityAliases;
        this.outputFormat = outputFormat;
        this.outputColumns = outputColumns;
    }

    public static Builder builder() {
        return new Builder();
    }

    private AliasHandler newOutputHandler(
            PrintWriter outWriter) throws IOException {
        final AliasHandler handler;
        switch (outputFormat) {
            case TSV_SIMPLIFIED:
                handler = new SpaceTrimmingAliasHandlerAdapter(
                        WriteTabulatedAliasHandler.newTsvInstance(outWriter, outputColumns));
                break;
            case TSV:
                handler = WriteTabulatedAliasHandler.newTsvInstance(outWriter, outputColumns);
                break;
            case CSV:
                handler = WriteTabulatedAliasHandler.newCsvInstance(outWriter, outputColumns);
                break;
            default:
                throw new AssertionError(outputFormat);
        }
        return handler;
    }

    void processSource(ByteSource source, WikiAliasGenerator generator) throws IOException {
        final Closer inCloser = Closer.create();
        try {
            // Set up the input stuff
            final BufferedInputStream in = inCloser.register(source.openBufferedStream());
            generator.process(in, pageLimit, source.size());

        } catch (Throwable throwable) {
            throw inCloser.rethrow(throwable);
        } finally {
            inCloser.close();
        }
    }

    void run() throws Exception {
        final Closer outCloser = Closer.create();
        try {
            // Set up the output stuff
            final Writer writer = outCloser.register(sink.openBufferedStream());
            final PrintWriter outWriter = outCloser.register(new PrintWriter(writer));

            final AliasHandler handler = newOutputHandler(outWriter);
            if (handler instanceof Closeable)
                outCloser.register(((Closeable) handler));

            final WikiAliasGenerator generator =
                    new WikiAliasGenerator(handler, producedTypes);
            generator.setIdentityAliasesProduced(produceIdentityAliases);

            for (final ByteSource source : sources) {
                processSource(source, generator);
            }

            if (handler instanceof Flushable)
                ((Flushable) handler).flush();
            outWriter.flush();
        } catch (Throwable throwable) {
            throw outCloser.rethrow(throwable);
        } finally {
            outCloser.close();
        }
    }

    public static void main(String[] args) throws Exception {


        final Builder builder = Main.builder();

        IStringConverterFactory converter = StringConverterFactory.newInstance(true);


        final JCommander jc = new JCommander();
        jc.setProgramName("wag");
        jc.addObject(builder);
        jc.addConverterFactory(converter);
        jc.parse(args);

        if (builder.globals.isUsageRequested()) {
            jc.usage();
        } else {
            Main m = builder.build();
            m.run();
        }
    }

    public static class GlobalCommandDelegate {

        @Parameter(
                names = {"-h", "--help"},
                description = "Display this usage screen.",
                help = true)
        private boolean usageRequested = false;

        public GlobalCommandDelegate() {
        }

        public boolean isUsageRequested() {
            return usageRequested;
        }
    }

    /**
     */
    @Parameters(commandDescription = "Wikipedia Alias Generator (WAG) extras various form or semantic relations that " +
            "indicative of a page title alias.")
    public static class Builder {

        /**
         *
         */
        private static final Logger LOG = Logger.getLogger(Builder.class.getName());
        private static final FileWriteMode[] FILE_WRITE_MODE_TRUNCATE = {};
        /**
         *
         */
        @ParametersDelegate
        private final GlobalCommandDelegate globals = new GlobalCommandDelegate();
        /**
         * The wiki-dumps to parse as specified either by a file or URL
         */
        @Parameter(description = "FILE1 [FILE2 [...]]",
                required = true)
        private List<String> inputs = Lists.newArrayList();


        /**
         * The destination file for discovered aliases.
         */
        @Parameter(names = {"-o", "--output"},
                description = "Output file to write aliases to. (\"-\" for stdout.)")
        private File outputFile = new File("-");

        /**
         * Character encoding to use for writing data.
         */
        private Charset outputCharset = Charset.defaultCharset();

        /**
         * Whether or not the output file can be overwritten (if it exists)
         */
        private boolean outputClobberingEnabled = false;

        /**
         * Whether or not to produce identity aliases; relations that point the themselves.
         */
        @Parameter(names = {"-I", "--identityAliases"},
                description = "Produce identity aliases (relations that point to themselves.)",
                variableArity = true)
        private boolean produceIdentityAliases = true;

        /**
         *
         */
        @Parameter(names = {"-t", "--types"},
                description = "Set of alias types to produce, as a comma-separated subset of {TITLE, LOWERCASE_TITLE," +
                        " LINK, REDIRECT, P1BOLD, DAB_TITLE, DAB_REDIRECT, HAT_NOTE, TRUNCATED, PERSON_ALT_NAME, " +
                        "P2BOLD, S1BOLD}",
                converter = AliasTypeStringConverter.class)
        private List<AliasType> producedAliasTypes = Lists.newArrayList(AliasType.STANDARD);

        /**
         *
         */
        @Parameter(names = {"-l", "--limit"},
                description = "Limit the number of pages which will be processed from each input file. " +
                        "(Set to -1 for no limit)")
        private int pageLimit = -1;

        /**
         *
         */
        @Parameter(names = {"-of", "--outputFormat"},
                description = "Output format. One of TSV, CSV, or TSV_SIMPLIFIED. TSV and CSV are well-formed escaped" +
                        "output. TSV_SIMPLIFIED pre-strips tokens so escaping is not required (compatible with Byblo.)",
                converter = OutputFormatStringConverter.class)
        private OutputFormat outputFormat = OutputFormat.TSV;
        /**
         *
         */
        @Parameter(names = {"-oc", "--outputColumns"},
                description = "Set of output columns to produce. Comma-separated subset of {TYPE, SUBTYPE, SOURCE, TARGET}",
                converter = ColumnStringConverter.class)
        private List<WriteTabulatedAliasHandler.Column> outputColumns
                = Lists.newArrayList(EnumSet.allOf(WriteTabulatedAliasHandler.Column.class));


        /**
         *
         */
        public Builder() {
        }

        /**
         * Set the character encoding to use when writing aliases to the output.
         * <p/>
         * Note that the input sinkCharset should be set within the XML file, in the encoding deceleration. For
         * example,
         * to read UTF-8 the XML file should start: {@code  <?xml version="1.0" encoding="UTF-8"?>}
         *
         * @param outputCharset character encoding to use for writing files.
         */
        @Parameter(names = {"-c", "--charset"},
                description = "Character encoding to use for writing aliases. " +
                        "(Input encoding should be set in the xml file.)")
        public Builder setOutputCharset(Charset outputCharset) {
            this.outputCharset = checkNotNull(outputCharset, "outputCharset");
            return this;
        }

        /**
         * Add the specified files to the list of input resource.
         * <p/>
         *
         * @param inputFiles wiki xml dump resources
         * @return this builder (for method chaining)
         * @throws NullPointerException of {@code inputFiles} is {@code null}
         */
        public Builder addInputFiles(Iterable<File> inputFiles) {
            for (File input : inputFiles)
                this.inputs.add(input.toString());
            return this;
        }

        /**
         * Add the specified URLs to the list of input resource.
         *
         * @param inputUrls wiki xml dump resourcess
         * @return this builder (for method chaining)
         * @throws NullPointerException of {@code inputUrls} is {@code null}
         */
        public Builder setInputUrls(Iterable<URL> inputUrls) {
            for (URL inputUrl : inputUrls)
                this.inputs.add(inputUrl.toString());
            return this;
        }

        /**
         * Set the output file to write aliases to.
         *
         * @param outputFile destination to write discovered aliases
         * @return this builder (for method chaining)
         * @throws NullPointerException of {@code outputFile} is {@code null}
         */
        public Builder setOutputFile(File outputFile) {
            this.outputFile = checkNotNull(outputFile, "outputFile");
            return this;
        }


        /**
         * Set whether or not the output file can be overwritten if it already exists.
         *
         * @param outputClobberingEnabled overwrite output files
         * @return this builder (for method chaining)
         */
        @Parameter(names = {"-C", "--clobber"},
                description = "Overwrite output files if they already exist")
        public Builder setOutputClobberingEnabled(boolean outputClobberingEnabled) {
            this.outputClobberingEnabled = outputClobberingEnabled;
            return this;
        }

        /**
         * Whether or not to produce identity aliases; relations that point the themselves.
         *
         * @param produceIdentityAliases true to produce identity aliases, false otherwise.
         * @return this builder (for method chaining)
         */
        public Builder setProduceIdentityAliases(boolean produceIdentityAliases) {
            this.produceIdentityAliases = produceIdentityAliases;
            return this;
        }

        /**
         * @return throw IllegalArgumentException if one of the required arguments is unspecified.
         */
        public Main build() throws IOException {

            // Check the input file and setup the source
            final ImmutableList.Builder<ByteSource> sourcesBuilder = ImmutableList.builder();


            for (final String input : inputs) {
                try {
                    final URL inputUrl = new URL(input);

                    LOG.log(Level.INFO, "Setting source to URL: " + inputUrl);
                    sourcesBuilder.add(Resources.asByteSource(inputUrl));

                } catch (MalformedURLException ex) {
                    final File inputFile = new File(input);

                    if (!inputFile.exists())
                        throw new IllegalArgumentException("The input file does not exists: " + inputFile);
                    if (!inputFile.isFile())
                        throw new IllegalArgumentException("The input file is not a regular file: " + inputFile);
                    if (!inputFile.canRead())
                        throw new IllegalArgumentException("The input file is not readable: " + inputFile);

                    LOG.log(Level.INFO, "Setting source to file: " + inputFile);
                    sourcesBuilder.add(Files.asByteSource(inputFile));
                }
            }

            // Check the output character encoding
            if (!outputCharset.canEncode()) {
                // Note: This is extremely unlikely to happen. Only auto-decoders do not
                // support encoding, and it would be a silly user who requests such.
                throw new IllegalArgumentException("Output character set does not support encoding: " + outputCharset);
            }


            // Check the output file and setup the sink
            final CharSink sink;

            if (outputFile.toString().equals("-")) {
                // Stdout
                LOG.log(Level.INFO, "Setting sink to file stdout.");

                sink = new CharSink() {
                    @Override
                    public Writer openStream() throws IOException {
                        return new PrintWriter(System.out);
                    }
                };
            } else {
                // To a file

                if (outputFile.exists()) {
                    if (!outputFile.isFile()) {
                        throw new IllegalArgumentException("The output file already exists, " +
                                "and is not a regular file: " + outputFile);
                    } else if (!outputFile.canWrite()) {
                        throw new IllegalArgumentException("The output file already exists," +
                                " and is not writable: " + outputFile);
                    } else if (outputClobberingEnabled) {
                        LOG.log(Level.WARNING, "Overwriting output file that already exists: {0}",
                                outputFile);
                    } else {
                        throw new IllegalArgumentException("The output file already exists and " +
                                "clobbering is disabled: " + outputFile);
                    }
                } else {
                    // Output does not exist so check it is creatable
                    if (!IOUtils.isCreatable(outputFile))
                        throw new IllegalArgumentException("Output file is not creatable." + outputFile);

                    // Make parent directories
                    if (outputFile.getParentFile() != null) {
                        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                            throw new IllegalArgumentException("Output file parent directory does not exist, " +
                                    "and is not creatable: " + outputFile);
                        }
                    }

                }

                LOG.log(Level.INFO, "Setting sink to file: " + outputFile);

                sink = Files.asCharSink(outputFile, outputCharset, FILE_WRITE_MODE_TRUNCATE);
            }


            if (producedAliasTypes.isEmpty()) {
                throw new IllegalArgumentException("Produced alias types list is empty.");
            }

            return new Main(
                    sourcesBuilder.build(),
                    sink,
                    EnumSet.copyOf(producedAliasTypes),
                    pageLimit,
                    produceIdentityAliases,
                    outputFormat,
                    EnumSet.copyOf(outputColumns));
        }

    }


    public static final class AliasTypeStringConverter extends EnumStringConverter<AliasType> {

        public AliasTypeStringConverter(String name) {
            super(name, AliasType.class);
        }

        public AliasTypeStringConverter() {
            super(AliasType.class);
        }

    }


    public static final class ColumnStringConverter extends EnumStringConverter<WriteTabulatedAliasHandler.Column> {

        public ColumnStringConverter(String name) {
            super(name, WriteTabulatedAliasHandler.Column.class);
        }

        public ColumnStringConverter() {
            super(WriteTabulatedAliasHandler.Column.class);
        }

    }

    public static final class OutputFormatStringConverter extends EnumStringConverter<OutputFormat> {

        public OutputFormatStringConverter(String name) {
            super(name, OutputFormat.class);
        }

        public OutputFormatStringConverter() {
            super(OutputFormat.class);
        }

    }

    public static class FileOrUrlConverter extends BaseConverter<Object> {

        public FileOrUrlConverter(String optionName) {
            super(optionName);
        }

        @Override
        public Object convert(String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException ex) {
                return new File(value);
            }
        }
    }


}

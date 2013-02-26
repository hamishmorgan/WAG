package uk.ac.susx.tag.wag;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.*;

import com.google.common.io.*;
import uk.ac.susx.tag.util.IOUtils;
import uk.ac.susx.tag.util.StringConverterFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 18/02/2013
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    private final ByteSource source;
    private final ByteSink sink;
    private final Charset sinkCharset;
    private final EnumSet<AliasType> producedTypes;
    private final int pageLimit;
    private final boolean produceIdentityAliases;


    /**
     * Private constructor. Use the builder to instantiate: {@link #builder()}.
     *
     * @param source
     * @param sink
     * @param sinkCharset
     * @param producedTypes
     * @param pageLimit
     * @param produceIdentityAliases
     */
    private Main(ByteSource source, ByteSink sink, Charset sinkCharset,
                 EnumSet<AliasType> producedTypes, int pageLimit, boolean produceIdentityAliases) {
        this.source = source;
        this.sink = sink;
        this.sinkCharset = sinkCharset;
        this.producedTypes = producedTypes;
        this.pageLimit = pageLimit;
        this.produceIdentityAliases = produceIdentityAliases;
    }

    public static Builder builder() {
        return new Builder();
    }

    void run() throws Exception {
        final Closer closer = Closer.create();
        try {
            // Set up the input stuff
            final BufferedInputStream in = closer.register(source.openBufferedStream());

            // Set up the output stuff
            final BufferedOutputStream out = closer.register(sink.openBufferedStream());
            final Writer writer = closer.register(new OutputStreamWriter(out, sinkCharset));
            final PrintWriter outWriter = closer.register(new PrintWriter(writer));
            final PrintAliasHandler handler = new PrintAliasHandler(
                    outWriter, PrintAliasHandler.Format.TSV);

            final WikiAliasGenerator generator =
                    new WikiAliasGenerator(handler, producedTypes);
            generator.setIdentityAliasesProduced(produceIdentityAliases);

            generator.process(in, pageLimit, source.size());

            outWriter.flush();
        } catch (Throwable throwable) {
            throw closer.rethrow(throwable);
        } finally {
            closer.close();
        }
    }

    public static void main(String[] args) throws Exception {
//
//        final File dataDir = new File("/Volumes/LocalScratchHD/LocalHome/Data/");
//        final File xmlFile = new File(dataDir, "enwiki-20130204-pages-articles.xml.bz2");
//        final File out = new File(dataDir, "enwiki-20130204-pages-articles.xml.bz2-aliases");
//


        final Builder builder = Main.builder();


        StringConverterFactory converter = StringConverterFactory.newInstance(true);


        final JCommander jc = new JCommander();
        jc.setProgramName("WikiAlias");
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
     * Note that some fields are prefixed with an underscore so JCommander can't tell
     */
    @Parameters()
    public static class Builder {

        /**
         *
         */
        private static final Logger LOG = Logger.getLogger(Builder.class.getName());
        /**
         *
         */
        @ParametersDelegate
        private final GlobalCommandDelegate globals = new GlobalCommandDelegate();
        /**
         * The wiki-dump to parse as specified by a file
         *
         * @see #inputUrl
         */
        private Optional<File> inputFile = Optional.absent();
        /**
         * The wiki-dump to parse as specified by a URL
         *
         * @see #inputFile
         */
        private Optional<URL> inputUrl = Optional.absent();

        /**
         * The destination file for discovered aliases.
         */
        private Optional<File> outputFile = Optional.absent();

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
                arity = 1)
        private boolean produceIdentityAliases = true;

        /**
         *
         */
        @Parameter(names = {"-t", "--types"},
                description = "Set of alias types to produce.")
        private Set<AliasType> producedAliasTypes = AliasType.STANDARD;

        /**
         *
         */
        @Parameter(names = {"-l", "--limit"},
                description = "Limit the job to process on the first pages. (Set to -1 for no limit)")
        private int pageLimit = -1;

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
                description = "character encoding to use for writing aliases")
        public Builder setOutputCharset(Charset outputCharset) {
            this.outputCharset = checkNotNull(outputCharset, "outputCharset");
            return this;
        }


        /**
         * Set the input to be read from the resource denoted by {@code input}.
         * <p/>
         * The input string can either represent a path of the file-system, or a URL to some other resource. Generally
         * if the string contains a protocol prefix then it will be assumed to be a URL, otherwise it will be assumed
         * to be file.
         * <p/>
         * If a resource is already set (either by File or URL), it is
         * cleared and the new resource is used instead.
         *
         * @param input wiki xml dump resource
         * @return this builder (for method chaining)
         * @throws NullPointerException of {@code input} is {@code null}
         */
        @Parameter(names = {"-i", "--input"},
                description = "input file/url to read the wiki-xml dump from",
                required = true)
        public Builder setInput(final String input) {
            try {
                final URL url = new URL(input);
                return setInputUrl(url);
            } catch (MalformedURLException ex) {
                return setInputFile(new File(input));
            }
        }


        /**
         * Set the input to be read from the resource denoted by {@code inputFile}.
         * <p/>
         * If a resource is already set (either by File or URL), it is
         * cleared and the new resource is used instead.
         *
         * @param inputFile wiki xml dump resource
         * @return this builder (for method chaining)
         * @throws NullPointerException of {@code inputFile} is {@code null}
         */
        public Builder setInputFile(File inputFile) {
            this.inputUrl = Optional.absent();
            this.inputFile = Optional.of(inputFile);
            return this;
        }

        /**
         * Set the input to be read from the resource denoted by {@code inputUrl}.
         * <p/>
         * If a resource is already set (either by File or URL), it is
         * cleared and the new resource is used instead.
         *
         * @param inputUrl wiki xml dump resource
         * @return this builder (for method chaining)
         * @throws NullPointerException of {@code inputUrl} is {@code null}
         */
        public Builder setInputUrl(URL inputUrl) {
            this.inputFile = Optional.absent();
            this.inputUrl = Optional.of(inputUrl);
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
            this.outputFile = Optional.of(outputFile);
            return this;
        }

        /**
         * XXX: Nasty hack to stop JCommander picking up the default value.
         */
        @Parameter(names = {"-o", "--output"},
                description = "output file to write aliases to",
                required = true)
        @Deprecated
        @Beta
        public void __jcSetOutputFile(File outputFile) {
            setOutputFile(outputFile);
        }

        /**
         * Set whether or not the output file can be overwritten if it already exists.
         *
         * @param outputClobberingEnabled overwrite output files
         * @return this builder (for method chaining)
         */
        @Parameter(names = {"-C", "--clobber"},
                description = "overwrite output files if they already exist")
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
            final ByteSource source;
            if (inputFile.isPresent()) {
                assert !inputUrl.isPresent();

                if (!inputFile.get().exists())
                    throw new IllegalArgumentException("The input file does not exists: " + inputFile.get());
                if (!inputFile.get().isFile())
                    throw new IllegalArgumentException("The input file is not a regular file: " + inputFile.get());
                if (!inputFile.get().canRead())
                    throw new IllegalArgumentException("The input file is not readable: " + inputFile.get());

                LOG.log(Level.INFO, "Setting source to file: " + inputFile.get());
                source = Files.asByteSource(inputFile.get());
            } else if (inputUrl.isPresent()) {
                assert !inputFile.isPresent();

                LOG.log(Level.INFO, "Setting source to URL: " + inputUrl.get());
                source = Resources.asByteSource(inputUrl.get());
            } else {
                throw new IllegalArgumentException("Either the input file or URL must be specified.");
            }

            // Check the output file and setup the sink
            final ByteSink sink;
            if (outputFile.isPresent()) {

                if (outputFile.get().exists()) {
                    if (!outputFile.get().isFile()) {
                        throw new IllegalArgumentException("The output file already exists, " +
                                "and is not a regular file: " + outputFile.get());
                    } else if (!outputFile.get().canWrite()) {
                        throw new IllegalArgumentException("The output file already exists," +
                                " and is not writable: " + outputFile.get());
                    } else if (outputClobberingEnabled) {
                        LOG.log(Level.WARNING, "Overwriting output file that already exists: {0}",
                                outputFile.get());
                    } else {
                        throw new IllegalArgumentException("The output file already exists and " +
                                "clobbering is disabled: " + outputFile.get());
                    }
                } else {
                    // Output does not exist so check it is creatable
                    if (!IOUtils.isCreatable(outputFile.get()))
                        throw new IllegalArgumentException("Output file is not creatable." + outputFile.get());

                    // Make parent directories
                    if (outputFile.get().getParentFile() != null) {
                        if (!outputFile.get().exists() && !outputFile.get().mkdirs()) {
                            throw new IllegalArgumentException("Output file parent directory does not exist, " +
                                    "and is not creatable: " + outputFile.get());
                        }
                    }

                }

                LOG.log(Level.INFO, "Setting sink to file: " + outputFile.get());
                sink = Files.asByteSink(outputFile.get());
            } else {
                throw new IllegalArgumentException("The output file must be specified.");
            }

            // Check the output character encoding
            if (!outputCharset.canEncode()) {
                // Note: This is extremely unlikely to happen. Only auto-decoders do not
                // support encoding, and it would be a silly user who requests such.
                throw new IllegalArgumentException("Output character set does not support encoding: " + outputCharset);
            }

            if (producedAliasTypes.isEmpty()) {
                throw new IllegalArgumentException("Produced alias types list is empty.");
            }

            return new Main(source, sink, outputCharset,
                    EnumSet.copyOf(producedAliasTypes), pageLimit, produceIdentityAliases);
        }

    }

}

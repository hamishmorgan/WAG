![WAG Logo](http://i.imgur.com/YuuRUJI.png)

# Wikipedia Alias Generator (WAG)

WAG is a tool for extracting aliases from a Wikipedia xml dump or export. It parses the XML and WikiText markup
to discover synonomy, hyponomy, and other types of potentially usefull alias relations.

## Download

You can get the latest snaptshot from our maven repository:

  http://kungf.eu:8081/nexus/content/repositories/snapshots/uk/ac/susx/mlcl/wag/
  
(You probably want one of the `bin-with-deps` artifacts. These include all the require libraries and are good to go.)

## Usage

WAG is primarily a command line tool. It is invoked as follows:

```
Usage: wag [options] FILE1 [FILE2 [...]]
  Options:
    -c, --charset
       Character encoding to use for writing aliases. (Input encoding should be
       set in the xml file.)
       Default: UTF-8
    -C, --clobber
       Overwrite output files if they already exist
       Default: false
    -h, --help
       Display this usage screen.
       Default: false
    -I, --identityAliases
       Produce identity aliases (relations that point to themselves.)
       Default: true
    -l, --limit
       Limit the number of pages which will be processed from each input file.
       (Set to -1 for no limit)
       Default: -1
    -o, --output
       Output file to write aliases to. ("-" for stdout.)
       Default: -
    -oc, --outputColumns
       Set of output columns to produce. Comma-separated subset of {TYPE,
       SUBTYPE, SOURCE, TARGET}
       Default: [TYPE, SUBTYPE, SOURCE, TARGET]
    -of, --outputFormat
       Output format. One of TSV, CSV, or TSV_SIMPLIFIED. TSV and CSV are
       well-formed escapedoutput. TSV_SIMPLIFIED pre-strips tokens so escaping is not
       required (compatible with Byblo.)
       Default: TSV
    -t, --types
       Set of alias types to produce, as a comma-separated subset of {TITLE,
       LOWERCASE_TITLE, LINK, REDIRECT, P1BOLD, DAB_TITLE, DAB_REDIRECT, HAT_NOTE, 
       TRUNCATED, PERSON_ALT_NAME, P2BOLD, S1BOLD}
       Default: [TITLE, LOWERCASE_TITLE, LINK, REDIRECT, P1BOLD, DAB_TITLE, 
            HAT_NOTE, TRUNCATED, PERSON_ALT_NAME, S1BOLD]
```

### Example 1: Page titles

A very simple thing one might want to do is get a list of every page in the wikipedia
dump file. To print such a list on `stdout` run WAG with the following options.

```sh
$ ./wag.sh -c UTF-8 -I -o - -t TITLE -of TSV_SIMPLIFIED \
    -oc TARGET enwiki-[timestamp]-pages-articles.xml.bz2 2> output.log
```

Some notes on what we're doing here:

 * Specify the output character encoding as UTF-8 (`-c UTF-8`).
 * Rather than write to a file, the result will printing to `stdout` (`-o -`).
 * Since we are going to producing just to title alias type `-t TITLE`, which is always an identity alias (i.e target and
source are just the page title), so we need to enable identity pairs (`-I`). 
 * We don't need either the type information (because it's always the same), not the source, so just produce the target column
(`-oc TARGET`). 
 * Read the wiki xml dump from the given file `enwiki-[timestamp]-pages-articles.xml.bz2`.
 * Finally let's hide all the logging and progress information by writing it to a file (`2> output.log`).

Obviously this is all rather complicated for such simple task, but then with great power comes
great obfuscation.

### Example 2: Distributional model

You might want to produce a dsitributional model out of the aliases data, by accociating 
those target entries that correlate on source referrers. This can be done using Byblo, if
the correct output format is specified for WAG:

To produce all the default aliases in a simplified TSV format (compatible with Byblo)
from a wikipedia dump file `enwiki-20061130-pages-articles.xml.bz2`, saving the results
in `aliases.tsv`. Note that we produce only the source and target columns (we don't want 
the type), and identity aliases are produced so we get distribution entries for pages
that have no referrers.

```sh
$ ./wag.sh --identityAliases --outputColumns SOURCE,TARGET --outputFormat TSV_SIMPLIFIED \
      --output aliases.tsv --charset UTF-8 enwiki-20061130-pages-articles.xml.bz2
```

After aliases have been extracted, `aliases.tsv` will contain two entries per line, first
the source (more ambiguous term), then the target (less ambiguous term, usually an actual page 
title.) To build a distributional model of page titles, using Byblo, we probably want to
reverse the columns, since the first column will be the thesaurus entry and the second will
be the feature. this can be achieved using the bash commands `cut` and `paste`.

The provided script `thesaurus.sh` does all this and more. You will probably need to 
modify it somewhat to you system, but hopefull it will give you some ideas.



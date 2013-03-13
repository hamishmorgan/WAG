# ![WAG Logo](http://i.imgur.com/YuuRUJI.png)

WAG is a tool for extracting aliases from a Wikipedia xml dump or export. It parses the XML and WikiText markup
to discover synonymy, hyponymy, and other types of potentially useful alias relations.

## Download

You can get the latest snapshot from our maven repository:

[http://kungf.eu:8081/nexus/content/repositories/snapshots/uk/ac/susx/mlcl/wag/](http://kungf.eu:8081/nexus/content/repositories/snapshots/uk/ac/susx/mlcl/wag/)
  
(You probably want one of the `bin-with-deps` artefacts. These include all the require libraries and are good to go.)

## Alias types

The software produces aliases from wiki data, using a number of different heuristics. Each type can be enabled and disabled independently. Some type have a number of sub-types; the specifics of which are context dependent.

#### TITLE

Exact match to the page title. Relation source and target will be the same. Note this will always be an *identity alias* so that option must be enabled or it will never be produced.

#### LOWERCASE_TITLE

When a page contains the `{{lowercase title}}` template (e.g [iPod](http://en.wikipedia.org/wiki/iPod), [gzip](http://en.wikipedia.org/wiki/gzip), ...) then the upper case title will be denoted as an alias of the lower-case variant (since lowercase is less ambiguous.) I.e source is upper-case, target is lower-case.

#### LINK

From wiki internal links (to other pages) we extract the link surface text as an alias for the title of the target page.

#### REDIRECT
    
When a page that contains `#REDIRECT [[target]]` directive it indicates that it should permanently redirect to the target page. We exact the redirecting page title as an alias of the target.

#### P1BOLD

Common and canonical names for a topic are conventionally listed in bold in the articles first paragraph. Note however that this is purely syntactic markup, and so is liable to be used for other meanings. When this type is enabled, the bold surface text is produced as an alias of the page title.

#### DAB_TITLE

Disambiguation (DAB) pages contains internal links to articles that each represent a single sense of the page title. When this type is enabled, the DAB title is extracted as an alias for each of the linked articles.

#### DAB_REDIRECT

There may be multiple terms that refer to the same disambiguation page (e.g [AMP](http://en.wikipedia.org/wiki/AMP) redirects to [Amp](http://en.wikipedia.org/wiki/Amp)). This is indicated with bold text at the start of the disambiguation page, or hat-note redirects. These are extracted (as types `??BOLD` and `HAT_NOTE`, respectively) but are not distinguished from normal pages. Consequently this type is never produced; enabling or disabling it does nothing. It is included for the sake of completeness with prior work.

#### HAT_NOTE

Articles frequently contain special templates to indicate related content. For example the template `{{About|USE1||PAGE2}}` transcludes to *"This page is about USE1. For other uses, see PAGE2."* There are a large number of variations on this theme, each of which is handled slightly differently, but in general we extract aliases where a more ambiguous terms references a less ambiguous term.

#### TRUNCATED

Page titles frequently contains disambiguation suffixes as bracketed terms or after a comma. When this alias type is enabled we permute page titles by stripping all combinations of disambiguation suffix, and include each variation as an alias of the full title.

Note there is one exception: the suffix `(disambiguation)`, which is used to denote a disambiguation page. If this suffix is encountered it is *always* stripped.

#### PERSON_ALT_NAME

Articles about people usually contain a special infobox denoted by the `{{Persondata}}` template. This includes, among other things, a list of `ALTERNATIVE NAMES` for the individual. When this type is enabled we extra each alternative name as an alias of the page title.

When processing this alias, we handle commas differently from other articles. In addition to including truncated variants we also re-order the name, broken by commas. E.g, for the name "Augustine, Saint, Bishop of Hippo", we
also produce "Saint Augustine", "Bishop of Hippo", and "Augustine, Bishop of Hippo".

#### P2BOLD

Same as `P1BOLD` but for the second paragraph, moderately improves recall at the expense of precision.


#### S1BOLD

Extract all bold text in the first section (preceding the first sub-title) as aliases of the page title. This type is an alternative to first and second paragraph bold text extraction (`P1BOLD` and `P2BOLD`) types. 

The first section of a page is the portion of text before the first section title. This may include multiple paragraphs, or it may be just one. Therefore is will have equal to or greater recall than `P1BOLD`, but may have lower recall than `P2BOLD`. 

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

This process takes about 30 minutes (on a full ~10 GB bzip2 compressed wikipedia dump) on my desktop 
computer.

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



# Redlink NLP

[![Build, Test & Publish](https://github.com/redlink-gmbh/redlink-nlp/actions/workflows/maven-build-and-deploy.yaml/badge.svg)](https://github.com/redlink-gmbh/redlink-nlp/actions/workflows/maven-build-and-deploy.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=redlink-gmbh_redlink-nlp&metric=alert_status)](https://sonarcloud.io/dashboard?id=redlink-gmbh_redlink-nlp)

[![Maven Central](https://img.shields.io/maven-central/v/io.redlink.nlp/redlink-nlp.png)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.redlink.nlp%22)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/io.redlink.nlp/redlink-nlp.png)](https://oss.sonatype.org/#nexus-search;gav~io.redlink.nlp~~~~)
[![Javadocs](https://www.javadoc.io/badge/io.redlink.nlp/redlink-nlp.svg)](https://www.javadoc.io/doc/io.redlink.nlp/redlink-nlp)
[![Apache 2.0 License](https://img.shields.io/github/license/redlink-gmbh/redlink-nlp.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Redlink NLP was started based on code from [Apache Stanbol](https://stanbol.apache.org/) but with
the following changes:

* switched from OSGI to Spring Boot
* relaced the RDF representation in `ContentItems` with a Java API and JSON serialization support
* Redlink NLP only covers Stanbol Enhancer functionality. No Entityhub, Contenthub ... modules

There was also an API overhaul for both ContentItems (see `api` module) and AnalyzedText (see `nlp-model`) module.

Redlink NLP also includes some additional analyzing components such as an integration with the 
[Stanza Server](https://github.com/redlink-gmbh/stanza-server).

## NLP Model

The basic building block of the AnalysedText is the Span. A Span defines `type`and the `[start,end)`. For the `type` an
enumeration with the members `Text`, `TextSection`, `Sentence`, `Chunk` and `Token` is used. The `[start,end)` define
the character positions of the span within the text. The start position is inclusive and the end position is exclusive.

Analog to the type of the Span there are also Java interfaces representing those types and providing additional
convenience methods. An additional Section interface was introduced as common parent for all types that may have
enclosed Spans. The AnalysedText is the interface representing `SpanTypeEnum#Text`. The main intension of those Java
classes are to have convenience methods that ease the use of the API.

### Uniqueness of Spans

A Span is considered equals to another Span if `[start, end)` and type are the same. The natural oder of Spans is defined
by

* smaller start index first
* bigger end index first
* higher ordinal number of the SpanTypeEnum first

This order is used by all Iterators returned by the AnalysedText API

### Concurrent Modifications and Iterators

Iterators returned by the AnalysedText API do __not__ throw `ConcurrentModificationExceptions` but reflect changes to
the underlying model. While this is not constant with the default behaviour of Iterators in Java this is central for
the effective usage of the AnalysedText API - e.g. when Iterating over Sentences while adding Tokens.

### Code Samples

The following Code Snippet shows some typical usages of the API:

```java
AnalysedText at; //typically retrieved from the contentPart
Iterator<Sentence> sentences = at.getSentences;
while (sentences.hasNext) {
   Sentence sentence = sentences.next();
   String sentText = sentence.getSpan();
   Iterator<SentenceToken> tokens = sentence.getTokens();
   while (tokens.hasNext()) {
       Token token = tokens.next();
       String tokenText = token.getSpan();
       Value<PosTag> pos = token.getAnnotation(
               Annotations.POS_ANNOTATION);
       String tag = pos.value().getTag();
       double confidence = pos.probability();
   }
}
```

Code that adds new Spans looks like follows

```java
//Tokenise an Text
Iterator<Sentence> sentences = at.getSentences();
Iterator<? extends Section> sections;
if (sentences.hasNext()) { //sentence Annotations present
   sections = sentences;
} else { //if no sentences tokenise the text at once
   sections = Collections.singelton(at).iterator();
}
//Tokenise the sections
for (Section section : sentenceList) {
   //assuming the Tokeniser returns tokens as 2dim int array
   int[][] tokenSpans = tokeniser.tokenise(section.getSpan());
   for (int ti = 0; ti < tokenSpans.length; ti++) {
      Token token = section.addToken(
              tokenSpans[ti][0], tokenSpans[ti][1]);
   }
}
```

For all `#add(start,end)` methods in the API the parsed `start` and `end` indexes are relative to the parent span (the
one the `#add(...)` method is called). If `#add**(...)` method is called for an existing `[start,end):type` combination the
existing instance is returned.

### Annotation Support

Annotation support is provided by two interfaces `Annotated`, `Annotation` and the `Value` class. `Annotated` provides
an API for adding information to the annotated object. Those annotations are represented by key value mappings
where `String` is used as key and the `Value` class as values. The `Value` class provides the generically typed value as
well as a `double` probability in the range `[0..1]` or `-1` if not known. Finally, the `Annotation` class can optionally
be used to provide type safety when working with annotations.

The following example shows the intended usage of the API

1. `Annotations` can defined to provide type safety:

   ```java
   public interface MyAnnotations {
       //a Part of Speech Annotation using a String key
       //and the PosTag class as value
       Annotation<String, PosTag> MY_ANNOTATION = new Annotation<String, My>(
               "stanbol.enhancer.nlp.pos", PosTag.class);
       //...
   }
   ```

2. Defined _Annotation_ are used to add information to an _Annotated_ instance (like a Span). For adding annotations the
   use of _Annotations_ is required to ensure type safety. The following code snippet shows how to add an PosTag with
   the probability 0.95.

   ```java
   PosTag tag=new PosTag("N"); //a simple POS tag
   Token token; //The Token we want to add the tag
   token.addAnnotations(POS_ANNOTATION,Value.value(tag),0.95);
   ```

3. For consuming annotations there are two options. First the possibility to use the _Annotation_ object and second by
   directly using the key. While the 2nd option is not as nicely to use (as it does not provide type safety) it allows
   consuming annotations without the need to have the used _Annotation_ in the classpath. The following examples show
   both options

   ```java
   Iterator<Token> tokens = sentence.getTokens();
   while(tokens.hasNext){
       Token token = tokens.next();
       //use the POS_ANNOTATION to get the PosTag
       PosTag tag = token.getAnnotation(POS_ANNOTATION);
       if(tag != null){
           log.info("{} has PosTag {}",token,tag.value());
       } else {
           log.info("{} has no PosTag",token);
       }
       //(2) use the key to retrieve values
       String key = "urn:test-dummy";
       Value<?> value = token.getValue(key);
       //the programmer needs to know the type!
       if(v.probability() > 0.5){
           log.info("{}={}",key,value.value());
       }
   }
   ```

The _Annotated_ interface supports multivalued annotations. For that it defines methods for adding/setting and getting
multiple values. Values are sorted first by the probability (unknown probability last) and secondly by the insert
order (first in first out). So calling the single value getAnnotation() method on a multivalued field will return the
first item (the highest probability and first added in case of multiple items with the same/no probabilities)

## License

Free use of this software is granted under the terms of the Apache License Version 2.0. See the [License](LICENSE.txt)
for more details.

### Considerations for the Stanford NLP Modules

[Stanford Core NLP](https://stanfordnlp.github.io/CoreNLP/) is a GPLv3 licensed 
language analysis tool suite that supports several languages.

So please consider GPLv3 related requirements when using one of the modules
that require Stanford Core NLP

* `nlp-stanfordnlp`
* `nlp-stanfordnlp-de`
* `nlp-truecase-de`

The code for those Modules is Licensed under Apache License Version 2.0. Actually
using those modules requires GPLv3 licensed Dependencies.



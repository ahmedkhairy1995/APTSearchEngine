package com.APT.SearchEngine.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Data {

    private static Set<String> documents=new HashSet<>();
    private static HashMap<String,Integer> myDocuments=new HashMap<>();

    public static Set<String> getNewsSite() {
        return newsSite;
    }

    private static Set<String> newsSite = Stream.of("www.ap.org",
            "nytimes.com",
            "www.washingtonpost.com",
            "www.wsj.com",
            "www.reuters.com",
            "www.latimes.com/",
            "cnn.com",
            "bbc.com",
            "www.telegraph.co.uk",
            "www.theguardian.com",
            "abcnews.go.com",
            "forbes.com",
            "Stackoverflow.com").collect(Collectors.toSet());


    private static Set<String> stopWords = Stream.of("i" ,
            "me" ,
            "my" ,
            "myself" ,
            "we" ,
            "our" ,
            "ours" ,
            "ourselves" ,
            "you" ,
            "your" ,
            "yours" ,
            "yourself" ,
            "yourselves" ,
            "he" ,
            "him" ,
            "his" ,
            "himself" ,
            "she" ,
            "her" ,
            "hers" ,
            "herself" ,
            "it" ,
            "its" ,
            "itself" ,
            "they" ,
            "them" ,
            "their" ,
            "theirs" ,
            "themselves" ,
            "what" ,
            "which" ,
            "who" ,
            "whom" ,
            "this" ,
            "that" ,
            "these" ,
            "those" ,
            "am" ,
            "is" ,
            "are" ,
            "was" ,
            "were" ,
            "be" ,
            "been" ,
            "being" ,
            "have" ,
            "has" ,
            "had" ,
            "having" ,
            "do" ,
            "does" ,
            "did" ,
            "doing" ,
            "a" ,
            "an" ,
            "the" ,
            "and" ,
            "but" ,
            "if" ,
            "or" ,
            "because" ,
            "as" ,
            "until" ,
            "while" ,
            "of" ,
            "at" ,
            "by" ,
            "for" ,
            "with" ,
            "about" ,
            "against" ,
            "between" ,
            "into" ,
            "through" ,
            "during" ,
            "before" ,
            "after" ,
            "above" ,
            "below" ,
            "to" ,
            "from" ,
            "up" ,
            "down" ,
            "in" ,
            "out" ,
            "on" ,
            "off" ,
            "over" ,
            "under" ,
            "again" ,
            "further" ,
            "then" ,
            "once" ,
            "here" ,
            "there" ,
            "when" ,
            "where" ,
            "why" ,
            "how" ,
            "all" ,
            "any" ,
            "both" ,
            "each" ,
            "few" ,
            "more" ,
            "most" ,
            "other" ,
            "some" ,
            "such" ,
            "no" ,
            "nor" ,
            "not" ,
            "only" ,
            "own" ,
            "same" ,
            "so" ,
            "than" ,
            "too" ,
            "very" ,
            "s" ,
            "t" ,
            "can" ,
            "will" ,
            "just" ,
            "don" ,
            "should" ,
            "now").collect(Collectors.toSet());

    public static Set<String> getDocuments() {
        return documents;
    }

    public static HashMap<String, Integer> getMyDocuments() {
        return myDocuments;
    }

    public static Set<String> getStopWords() {
        return stopWords;
    }
}

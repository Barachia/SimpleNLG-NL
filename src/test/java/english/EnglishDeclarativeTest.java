package english;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import simplenlg.features.*;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.Realiser;

import static org.hamcrest.CoreMatchers.equalTo;

public class EnglishDeclarativeTest {

    final private static Lexicon lexicon_en = new simplenlg.lexicon.english.XMLLexicon();
    final private static NLGFactory factory_en = new NLGFactory(lexicon_en);
    final private static Realiser realiser_en = new Realiser();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void basicDeclarativeTransitiveFuture(){
        SPhraseSpec clause = factory_en.createClause();
        NPPhraseSpec subject = factory_en.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("motivate");
        clause.setObject("John");
        clause.setFeature(Feature.TENSE, Tense.FUTURE);
        String output = realiser_en.realiseSentence(clause);
        collector.checkThat(output, equalTo("You will motivate John."));
    }

    @Test
    public void basicDeclarativeDitransitivePast(){
        SPhraseSpec clause = factory_en.createClause();
        NPPhraseSpec subject = factory_en.createNounPhrase("YOU");
        subject.setFeature(Feature.PRONOMINAL, true);
        subject.setFeature(Feature.PERSON, Person.SECOND);
        clause.setSubject(subject);
        clause.setVerb("give");
        clause.setObject("the letter");
        NPPhraseSpec dObject = factory_en.createNounPhrase("HER");
        dObject.setFeature(Feature.PRONOMINAL, true);
        dObject.setFeature(Feature.PERSON, Person.THIRD);
        dObject.setFeature(LexicalFeature.GENDER, Gender.FEMININE);
        clause.setIndirectObject(dObject);
        clause.setFeature(Feature.TENSE, Tense.PAST);
        String output = realiser_en.realiseSentence(clause);
        collector.checkThat(output, equalTo("You gave her the letter."));
    }
}

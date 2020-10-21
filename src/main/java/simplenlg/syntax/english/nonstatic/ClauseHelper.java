/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is "Simplenlg".
 *
 * The Initial Developer of the Original Code is Ehud Reiter, Albert Gatt and Dave Westwater.
 * Portions created by Ehud Reiter, Albert Gatt and Dave Westwater are Copyright (C) 2010-11 The University of Aberdeen. All Rights Reserved.
 *
 * Contributor(s): Ehud Reiter, Albert Gatt, Dave Wewstwater, Roman Kutlak, Margaret Mitchell, Pierre-Luc Vaudry.
 */
package simplenlg.syntax.english.nonstatic;

import simplenlg.features.*;
import simplenlg.framework.*;
import simplenlg.phrasespec.AdvPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.syntax.AbstractClauseHelper;

import java.util.List;

/**
 * <p>
 * This is a helper class containing the main methods for realising the syntax
 * of clauses for English. It is a non static version by vaudrypl of the class
 * of the same name in the <code>simplenlg.syntax.english</code> package.
 * </p>
 * modified by vaudrypl :
 * abstract class replaced by public class
 * private static methods replaced by protected methods
 * parent.realise(element) replaced by element.realiseSyntax()
 * SyntaxProcessor parent arguments removed
 * PhraseHelper replaced by phrase.getPhraseHelper()
 * now implements AbstractClauseHelper
 * 
 * most methods now moved to AbstractClauseHelper
 * 
 * @author D. Westwater, University of Aberdeen.
 * @version 4.0
 */
public class ClauseHelper extends AbstractClauseHelper {

	/**
	 * Adds <em>to</em> to the end of interrogatives concerning indirect
	 * objects. For example, <em>who did John give the flower <b>to</b></em>.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 */
	@Override
	protected void addEndingTo(PhraseElement phrase,
			ListElement realisedElement,
			NLGFactory phraseFactory) {

		if (InterrogativeType.WHO_INDIRECT_OBJECT.equals(phrase
				.getFeature(Feature.INTERROGATIVE_TYPE))) {
			NLGElement word = phraseFactory.createWord(
					"to", LexicalCategory.PREPOSITION); //$NON-NLS-1$
			realisedElement.addComponent(word.realiseSyntax());
		}
	}

	/**
	 * This is the main controlling method for handling interrogative clauses.
	 * The actual steps taken are dependent on the type of question being asked.
	 * The method also determines if there is a subject that will split the verb
	 * group of the clause. For example, the clause
	 * <em>the man <b>should give</b> the woman the flower</em> has the verb
	 * group indicated in <b>bold</b>. The phrase is rearranged as yes/no
	 * question as
	 * <em><b>should</b> the man <b>give</b> the woman the flower</em> with the
	 * subject <em>the man</em> splitting the verb group.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 * @return an <code>NLGElement</code> representing a subject that should
	 *         split the verb
	 */
	@Override
	protected NLGElement realiseInterrogative(PhraseElement phrase,
			ListElement realisedElement,
			NLGFactory phraseFactory, NLGElement verbElement) {
		NLGElement splitVerb = null;

		if (phrase.getParent() != null) {
			phrase.getParent().setFeature(InternalFeature.INTERROGATIVE, true);
		}
		Object type = phrase.getFeature(Feature.INTERROGATIVE_TYPE);

		if (type instanceof InterrogativeType) {
			switch ((InterrogativeType) type) {
			case YES_NO:
				splitVerb = realiseYesNo(phrase, verbElement, phraseFactory, realisedElement);
				break;
			case WHO_SUBJECT:
			case HOW_PREDICATE:
			case WHAT_SUBJECT:
				realiseInterrogativeKeyWord(((InterrogativeType) type).getString(), LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
					phraseFactory);
				// Commented out by vaudrypl to keep phrase intact.
				// Modified addSubjectsToFront() accordingly.
				 phrase.removeFeature(InternalFeature.SUBJECTS);
				break;
			case HOW_MANY:
				realiseInterrogativeKeyWord("how", LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
						phraseFactory);
				realiseInterrogativeKeyWord("many", LexicalCategory.ADVERB, realisedElement, //$NON-NLS-1$
						phraseFactory);
				addDoAuxiliary(phrase, phraseFactory, realisedElement);
				break;
			case HOW:
			case WHY:
			case WHEN:
			case WHERE:
			case WHO_OBJECT:
			case WHO_INDIRECT_OBJECT:
			case WHAT_OBJECT:
				splitVerb = realiseObjectWHInterrogative(((InterrogativeType) type).getString(),
						phrase,
						realisedElement,
						phraseFactory);
				break;
//			case HOW_PREDICATE:
//				splitVerb = realiseObjectWHInterrogative("how", phrase, realisedElement, phraseFactory);
//				break;
			case HOW_ADJECTIVE:
				realiseInterrogativeKeyWord(((InterrogativeType) type).getString(), LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
						phraseFactory);
				break;
			case WHICH:
			case WHOSE:
				realiseInterrogativeKeyWord(((InterrogativeType) type).getString(), LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
						phraseFactory);
				addDoAuxiliary(phrase, phraseFactory, realisedElement);
				break;
			case HOW_COME:
				realiseInterrogativeKeyWord("how", LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
						phraseFactory);
				realiseInterrogativeKeyWord("come", LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
						phraseFactory);
				break;
			default:
				break;
			}
		}
		return splitVerb;
	}

	/**
	 * Adds the subjects to the beginning of the clause unless the clause is
	 * infinitive, imperative or passive, the subjects split the verb or,
	 * in French, the relative phrase discourse function is subject.
	 *
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param splitVerb
	 *            an <code>NLGElement</code> representing the subjects that
	 *            should split the verb
	 */
	@Override
	protected void addSubjectsToFront(PhraseElement phrase,
									  ListElement realisedElement,
									  NLGElement splitVerb) {

		if (!phrase.hasRelativePhrase(DiscourseFunction.SUBJECT)) {
			if(phrase.hasFeature(Feature.INTERROGATIVE_TYPE)){
				ListElement realisedSubject = new ListElement(phrase);
				//Realising subject to add later
				super.addSubjectsToFront(phrase, realisedSubject,splitVerb);
				if(this.moveSubjectAfterVerb(realisedElement,realisedSubject)){
					return;
				}
			}
			super.addSubjectsToFront(phrase, realisedElement, splitVerb);
		}
	}

	/**
	 * Method for adding the subject directly after the verb if necessary. Mainly used for interrogatives
	 * @param realisedElement, the current list of realised elements
	 * @param realisedSubject, the subject that has to go behind the verb, if no current subject is found
	 */
	private boolean moveSubjectAfterVerb(ListElement realisedElement, ListElement realisedSubject){
		//Search for the subject in the sentence
		List<NLGElement> alreadyRealisedElements = realisedElement.getChildren();
		// Check if the sentence has children:
		if(alreadyRealisedElements != null){
			for(int vpIndex = 0; vpIndex < alreadyRealisedElements.size(); vpIndex++) {
				//Get the children of each element
				NLGElement alreadyRealisedElement = alreadyRealisedElements.get(vpIndex);
				//Check if one of them is a subject, otherwise, check if one of the children is a subject
				if (alreadyRealisedElement.hasFeature(InternalFeature.DISCOURSE_FUNCTION) && alreadyRealisedElement.getFeature(InternalFeature.DISCOURSE_FUNCTION).equals(DiscourseFunction.SUBJECT)) {
					realisedSubject = (ListElement) alreadyRealisedElements.remove(vpIndex);
					realisedElement.setComponents(alreadyRealisedElements);

				}
				else {
					List<NLGElement> elementComponents = alreadyRealisedElement.getChildren();
					//Check if there are children if we couldn't find a discourse function yet
					if(elementComponents != null && !alreadyRealisedElement.hasFeature(InternalFeature.DISCOURSE_FUNCTION)){
						for (int cIndex = 0; cIndex < elementComponents.size(); cIndex++) {
							NLGElement component = elementComponents.get(cIndex);
							//If a child is a subject, set it to the realisedSubject
							List<NLGElement> parts = component.getChildren();
							if(parts != null){
								for(int partIndex = 0; partIndex < parts.size(); partIndex++){
									NLGElement part = parts.get(partIndex);
									if (part.hasFeature(InternalFeature.DISCOURSE_FUNCTION) && part.getFeature(InternalFeature.DISCOURSE_FUNCTION).equals(DiscourseFunction.SUBJECT)) {
										((ListElement) alreadyRealisedElement).setComponents(elementComponents);
										realisedElement.setComponents(alreadyRealisedElements);
									}

								}
							}
							if (component.hasFeature(InternalFeature.DISCOURSE_FUNCTION) && component.getFeature(InternalFeature.DISCOURSE_FUNCTION).equals(DiscourseFunction.SUBJECT)) {
								if(elementComponents.size() > 0){
									((ListElement) alreadyRealisedElement).setComponents(elementComponents);
								}
								// Remove the subject from the sentence
								realisedSubject = (ListElement) alreadyRealisedElements.remove(vpIndex);
								realisedElement.setComponents(alreadyRealisedElements);
							}
						}
					}

				}
			}
			//Move the subject after the finite verb in the sentence
			for(int vpIndex = 0; vpIndex < alreadyRealisedElements.size(); vpIndex++) {
				//Get the children of each element
				NLGElement alreadyRealisedElement = alreadyRealisedElements.get(vpIndex);
				//Check if one of them is a finite verb, otherwise, check if one of the children is a VERB_PHRASE
				if (alreadyRealisedElement.hasFeature("discourse_function") &&
						alreadyRealisedElement.getFeature("discourse_function").equals(DiscourseFunction.VERB_PHRASE) ||
						alreadyRealisedElement.getCategory().equalTo(PhraseCategory.VERB_PHRASE)){
					List<NLGElement> elementComponents = alreadyRealisedElement.getChildren();
					if(elementComponents != null){
						if (addSubjectAfterChildVerb(realisedElement, realisedSubject, alreadyRealisedElements, vpIndex, alreadyRealisedElement, elementComponents))
							return true;
					}
					else{
						alreadyRealisedElements.add(vpIndex+1,realisedSubject);
						realisedElement.setComponents(alreadyRealisedElements);
						return true;
					}
				}
				else {
					List<NLGElement> elementComponents = alreadyRealisedElement.getChildren();
					if (elementComponents != null) {
						if (addSubjectAfterChildVerb(realisedElement, realisedSubject, alreadyRealisedElements, vpIndex, alreadyRealisedElement, elementComponents))
							return true;
					}
				}
			}
		}
		return false;
	}

	private boolean addSubjectAfterChildVerb(ListElement realisedElement, ListElement realisedSubject, List<NLGElement> alreadyRealisedElements, int vpIndex, NLGElement alreadyRealisedElement, List<NLGElement> elementComponents) {
		for (int cIndex = 0; cIndex < elementComponents.size(); cIndex++) {
			NLGElement component = elementComponents.get(cIndex);
			//If a child is a VERB, add the realisedSubject after it
			if (component.getCategory().equalTo(PhraseCategory.VERB_PHRASE) || component.getCategory().equalTo(LexicalCategory.VERB)) {
				elementComponents.add(cIndex+1,realisedSubject);
				((ListElement) alreadyRealisedElement).setComponents(elementComponents);
				alreadyRealisedElements.set(vpIndex, alreadyRealisedElement);
				realisedElement.setComponents(alreadyRealisedElements);
				return true;
			}
		}
		return false;
	}

	/**
	 * Controls the realisation of <em>wh</em> object questions.
	 *
	 * @param keyword
	 *            the wh word
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 * @return an <code>NLGElement</code> representing a subject that should
	 *         split the verb
	 */
	private NLGElement realiseObjectWHInterrogative(String keyword, PhraseElement phrase, ListElement realisedElement, NLGFactory phraseFactory) {
		NLGElement splitVerb = null;
		realiseInterrogativeKeyWord(keyword, LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
				phraseFactory);

		// if (!Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE)) &&
		// !copular) {
		if(!hasAuxiliary(phrase) && !phrase.getVerbPhraseHelper().isCopular(phrase)) {
			addDoAuxiliary(phrase, phraseFactory, realisedElement);

		} else if(!phrase.getFeatureAsBoolean(Feature.PASSIVE).booleanValue()) {
			splitVerb = realiseSubjects(phrase);
		}

		return splitVerb;
	}

	/*
	 * Check if a sentence has an auxiliary (needed to relise questions
	 * correctly)
	 */
	private boolean hasAuxiliary(PhraseElement phrase) {
		return phrase.hasFeature(Feature.MODAL) || phrase.getFeatureAsBoolean(Feature.PERFECT)
				|| phrase.getFeatureAsBoolean(Feature.PROGRESSIVE)
				|| Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE));
	}

	/**
	 * Controls the realisation of <em>what</em> questions.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 * @return an <code>NLGElement</code> representing a subject that should
	 *         split the verb
	 */
	protected NLGElement realiseWhatInterrogative(PhraseElement phrase,
			ListElement realisedElement, NLGFactory phraseFactory) {
		NLGElement splitVerb = null;

		realiseInterrogativeKeyWord("what", LexicalCategory.PRONOUN, realisedElement, //$NON-NLS-1$
				phraseFactory);
		if (!Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE))) {
			addDoAuxiliary(phrase, phraseFactory, realisedElement);
		} else {
			if (!phrase.getFeatureAsBoolean(Feature.PASSIVE).booleanValue()) {
				splitVerb = realiseSubjects(phrase);
			}
		}
		return splitVerb;
	}

	/**
	 * Adds a <em>do</em> verb to the realisation of this clause.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 */
	protected void addDoAuxiliary(PhraseElement phrase,	NLGFactory phraseFactory,
			ListElement realisedElement) {

		PhraseElement doPhrase = phraseFactory.createVerbPhrase("do"); //$NON-NLS-1$
		doPhrase.setFeature(Feature.TENSE,phrase.getFeature(Feature.TENSE));
		doPhrase.setFeature(Feature.PERSON, phrase.getFeature(Feature.PERSON));
		doPhrase.setFeature(Feature.NUMBER, phrase.getFeature(Feature.NUMBER));
		realisedElement.addComponent(doPhrase.realiseSyntax());
	}

	/**
	 * Realises the key word of the interrogative. For example, <em>who</em>,
	 * <em>what</em>
	 *  @param keyWord
	 *            the key word of the interrogative.
	 * @param pronoun
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 */
	protected void realiseInterrogativeKeyWord(String keyWord,
											   LexicalCategory pronoun, ListElement realisedElement, NLGFactory phraseFactory) {

		if (keyWord != null) {
			NLGElement question = phraseFactory.createWord(keyWord,
					LexicalCategory.NOUN);
			NLGElement currentElement = question.realiseSyntax();
			if (currentElement != null) {
				realisedElement.addComponent(currentElement);
			}
		}
	}

	/**
	 * Performs the realisation for YES/NO types of questions. This may involve
	 * adding an optional <em>do</em> auxiliary verb to the beginning of the
	 * clause. The method also determines if there is a subject that will split
	 * the verb group of the clause. For example, the clause
	 * <em>the man <b>should give</b> the woman the flower</em> has the verb
	 * group indicated in <b>bold</b>. The phrase is rearranged as yes/no
	 * question as
	 * <em><b>should</b> the man <b>give</b> the woman the flower</em> with the
	 * subject <em>the man</em> splitting the verb group.
	 * 
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 * @return an <code>NLGElement</code> representing a subject that should
	 *         split the verb
	 */
	protected NLGElement realiseYesNo(PhraseElement phrase,
			NLGElement verbElement,
			NLGFactory phraseFactory, ListElement realisedElement) {

		NLGElement splitVerb = null;

		if (!(verbElement instanceof VPPhraseSpec && phrase.getVerbPhraseHelper()
				.isCopular(((VPPhraseSpec) verbElement).getVerb()))
				&& !phrase.getFeatureAsBoolean(Feature.PROGRESSIVE)
						.booleanValue()
				&& !phrase.hasFeature(Feature.MODAL)
				&& !Tense.FUTURE.equals(phrase.getFeature(Feature.TENSE))
				&& !phrase.getFeatureAsBoolean(Feature.NEGATED)
				&& !phrase.getFeatureAsBoolean(Feature.PASSIVE).booleanValue()) {
			addDoAuxiliary(phrase, phraseFactory, realisedElement);
		} else {
			splitVerb = realiseSubjects(phrase);
		}
		return splitVerb;
	}

	/**
	 * Add a modifier to a clause Use heuristics to decide where it goes
	 * 
	 * code moved from simplenl.phrasespec.SPhraseSpec.addModifier(Object modifier)
	 * by vaudrypl
	 * 
	 * @param clause
	 * @param modifier
	 * 
	 */
	@Override
	public void addModifier(SPhraseSpec clause, Object modifier) {
		// adverb is frontModifier if sentenceModifier
		// otherwise adverb is preModifier
		// string which is one lexicographic word is looked up in lexicon,
		// above rules apply if adverb
		// Everything else is postModifier

		if (modifier == null)
			return;

		// get modifier as NLGElement if possible
		NLGElement modifierElement = null;
		if (modifier instanceof NLGElement)
			modifierElement = (NLGElement) modifier;
		else if (modifier instanceof String) {
			String modifierString = (String) modifier;
			if (modifierString.length() > 0 && !modifierString.contains(" "))
				modifierElement = clause.getFactory().createWord(modifier,
						LexicalCategory.ANY);
		}

		// if no modifier element, must be a complex string
		if (modifierElement == null) {
			clause.addPostModifier((String) modifier);
			return;
		}

		// AdvP is premodifer (probably should look at head to see if
		// sentenceModifier)
		if (modifierElement instanceof AdvPhraseSpec) {
			clause.addPreModifier(modifierElement);
			return;
		}

		// extract WordElement if modifier is a single word
		WordElement modifierWord = null;
		if (modifierElement != null && modifierElement instanceof WordElement)
			modifierWord = (WordElement) modifierElement;
		else if (modifierElement != null
				&& modifierElement instanceof InflectedWordElement)
			modifierWord = ((InflectedWordElement) modifierElement)
					.getBaseWord();

		if (modifierWord != null
				&& modifierWord.getCategory() == LexicalCategory.ADVERB) {
			// adverb rules
			if (modifierWord
					.getFeatureAsBoolean(LexicalFeature.SENTENCE_MODIFIER))
				clause.addFrontModifier(modifierWord);
			else
				clause.addPreModifier(modifierWord);
			return;
		}

		// default case
		clause.addPostModifier(modifierElement);
	}
		
}
